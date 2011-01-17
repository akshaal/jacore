/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package fs

import java.nio.channels.AsynchronousFileChannel
import java.io.{File, IOException}
import java.nio.file.StandardOpenOption.{READ, WRITE, CREATE, TRUNCATE_EXISTING}
import java.nio.ByteBuffer
import java.nio.channels.CompletionHandler

import actor.{Actor, LowPriorityActorEnv, Operation}
import logger.Logging

/**
 * Fast async actor to read/write files.
 * @param lowPriorityActorEnv environment for the low priority actor
 */
@Singleton
private[jacore] class FileServiceActor @Inject() (lowPriorityActorEnv : LowPriorityActorEnv)
                    extends Actor (actorEnv = lowPriorityActorEnv)
                       with FileService
{
    import FileServiceActor._

    /**
     * Write file operation.
     * @param file file to write
     * @param content context to write
     */
    override def writeFileOperation (file : File, content : ByteBuffer) : Operation.WithResult [Unit] =
        new AbstractOperation [Result[Unit]] {
            override def processRequest () {
                doWriteFile (file, content, yieldResult)
            }
        }

    /**
     * Open file and initiate reading from the file.
     *
     * @param file file to read from
     * @param size maximum number of bytes to read, if not given, then the whole file will be read
     *        and some time will be spent to obtain file size
     * @return byte buffer with data read from file
     */
    override def readFileOperation (file : File, size : Option[Int] = None) : Operation.WithResult [ByteBuffer] =
        new AbstractOperation [Result[ByteBuffer]] {
            override def processRequest () {
                doReadFile (file, size, yieldResult)
            }
        }

    /**
     * Write data to the given file from the given buffer. When writing is finished
     * 'yieldResult' method is invoked. This is helper method and always invoked from
     * actor's method during operation processing.
     * @param file file to write data to
     * @param buf buffer with data
     * @param yieldResult method to call when writing is finished
     */
    private def doWriteFile (file : File,
                             buf : ByteBuffer,
                             yieldResult : Result[Unit] => Unit) : Unit =
    {
        val handler = new WriteCompletionHandler (buf, yieldResult)

        try {
            val ch = AsynchronousFileChannel.open (file.toPath,
                                                   WRITE,
                                                   CREATE,
                                                   TRUNCATE_EXISTING)
            handler.channel = ch

            ch.write (buf, 0, null, handler)
        } catch {
            case exc : IOException => handler.failed (exc, null)
        }
    }

    /**
     * Read data from the given file. When reading is finished
     * 'yieldResult' method is invoked. This is helper method and always invoked from
     * actor's method during operation processing.
     * @param file file to write data to
     * @param sizeOption number of bytes to read or None if read all data from the file
     * @param yieldResult method to call when writing is finished
     */
    private def doReadFile (file : File,
                            sizeOption : Option[Int],
                            yieldResult : Result[ByteBuffer] => Unit) : Unit =
    {
        var handler = new ReadCompletionHandler (yieldResult)

        try {
            val ch = AsynchronousFileChannel.open (file.toPath, READ)
            val size = sizeOption.getOrElse (ch.size.asInstanceOf[Int])
            val buf = ByteBuffer.allocate (size)
            handler.channel = ch
            handler.buf = buf

            ch.read (buf, 0, null, handler)
        } catch {
            case exc : IOException => handler.failed (exc, null)
        }
    }

    // - - -- - - - - -  - - - - -- -- - - - - - - - - - - - - - - -  - - - - - - - - - - -
    // Completion handlers

    /**
     * Common completion handler code for both read and write completion handler.
     */
    private[this] abstract class AbstractCompletionHandler
                                    extends CompletionHandler [java.lang.Integer, Object]
    {
        var channel : AsynchronousFileChannel = null

        protected def closeChannelAfter (code : => Unit) : Unit = {
            try {
                code
            } finally {
                if (channel != null) {
                    logIgnoredException ("unable to close channel of file") {
                        channel.close ()
                    }
                }
            }
        }
    }

    /**
     * Write completion handler
     */
    private[this] final class WriteCompletionHandler (buf : ByteBuffer, yieldResult : Result[Unit] => Unit)
                      extends AbstractCompletionHandler
    {
        private val bufLen = buf.remaining

        /**
         * Called when write operation is finished.
         */
        override def completed (bytes : java.lang.Integer, ignored : Object) : Unit = {
            closeChannelAfter {
                if (bufLen == bytes) {
                    yieldResult (Success [Unit] ())
                } else {
                    val msg = "Only " + bytes + " number of bytes out of " + bufLen + " has been written"

                    yieldResult (Failure [Unit] (msg))
                }
            }
        }

        /**
         * Called when write operation failed.
         */
        override def failed (exc : Throwable, ignored : Object) : Unit = {
            closeChannelAfter {
                val optExc = if (isUseless (exc)) None else Some(exc)
                yieldResult (Failure [Unit] ("Unable to write into the file", optExc))
            }
        }
    }

    /**
     * Write completion handler
     */
    private [this] final class ReadCompletionHandler (yieldResult : Result[ByteBuffer] => Unit)
                        extends AbstractCompletionHandler
    {
        var buf : ByteBuffer = null

        /**
         * Called when write operation is finished.
         */
        override def completed (bytes : java.lang.Integer, ignored : Object) : Unit = {
            closeChannelAfter {
                buf.rewind ()
                buf.limit (bytes.asInstanceOf[Int])

                yieldResult (Success (buf))
            }
        }

        /**
         * Called when write operation failed.
         */
        override def failed (exc : Throwable, ignored : Object) : Unit = {
            closeChannelAfter {
                val optExc = if (isUseless (exc)) None else Some(exc)
                yieldResult (Failure [ByteBuffer] ("Unable to read from the file", optExc))
            }
        }
    }
}

/**
 * Helper stuff for FileServiceActor class.
 */
private[fs] object FileServiceActor {
    /**
     * Regex pattern for useless IOException messages.
     */
    val UselessIOExceptionRegex = """(Invalid argument|No such file or directory)""".r

    /**
     * Method to check whether exception is useless or not.
     * @return true if exception's stacktrace is completelly useless
     */
    private def isUseless (exc : Throwable) : Boolean = {
        def isUselessIOExceptionMsg (msg : String) : Boolean =
            msg match {
                case UselessIOExceptionRegex (_) => true
                case _ => false
            }

        exc match {
            case e : IOException if isUselessIOExceptionMsg (e.getMessage) => true
            case _ => false
        }
    }
}