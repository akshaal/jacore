/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package fs
package text

import java.nio.channels.AsynchronousFileChannel
import java.io.{File, IOException}
import java.nio.file.StandardOpenOption.{READ, WRITE, CREATE, TRUNCATE_EXISTING}
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset

import actor.{Actor, NormalPriorityActorEnv, Operation}
import logger.Logging

// ///////////////////////////////////////////////////////////////////////
// Messages

abstract sealed class FileMessage extends NotNull

case class WriteFileDone (file : File,
                          payload : Any)
                          extends FileMessage

case class WriteFileFailed (file : File,
                            exc : Throwable,
                            payload : Any)
                            extends FileMessage

case class ReadFileDone (file : File,
                         content : String,
                         payload : Any)
                            extends FileMessage

case class ReadFileFailed (file : File,
                           exc : Throwable,
                           payload : Any)
                            extends FileMessage

// ///////////////////////////////////////////////////////////////////////
// TextFile interface

/**
 * Fast synchronous file reader/writer.
 */
trait TextFile {
    /**
     * Write a given content into the file. When writing is done a message will be issued
     * to the caller actor. The possible result messages are <code>WriteFileDone</code>
     * and <code>WriteFileFailed</code>.
     *
     * @param file write content to this file
     * @param content string content to write into the file
     * @param payload payload to passed back in messsage when writing is done
     */
    def writeFileAsy (file : File, content : String, payload : Any) : Unit

    /**
     * Write a given content into the file providing a way to handle result.
     *
     * @param file write content to this file
     * @param content string content to write into the file
     */
    def opWriteFile (file : File, content : String) : Operation.WithResult [Unit]

    /**
     * Open file and initiate reading from the file. When reading is done a message will sended
     * to the caller actor. The possible result messages are <code>ReadFileDone</code>
     * and <code>ReadFileFailed</code>.
     *
     * @param file file to read from
     * @param payload payload to passed back in messsage when reading is done
     * @param size if size is not given, then the whole file is requested for read
     */
    def readFileAsy (file : File, payload : Any, size : Option[Int] = None) : Unit

    /**
     * Open file and initiate reading from the file.
     * 
     * @param file file to read from
     */
    def opReadFile (file : File, size : Option[Int] = None) : Operation.WithResult [String]
}

// ///////////////////////////////////////////////////////////////////////
// TextFile actor

/**
 * Fast async file reader/writer.
 */
@Singleton
private[jacore] class TextFileActor @Inject() (
                       normalPriorityActorEnv : NormalPriorityActorEnv,
                       @Named ("jacore.os.file.encoding") encoding : String)
                    extends Actor (actorEnv = normalPriorityActorEnv)
                       with TextFile
{
    private val encoder = Charset.forName (encoding).newEncoder ()
    private val decoder = Charset.forName (encoding).newDecoder ()

    override def writeFileAsy (file : File, content : String, payload : Any) : Unit =
    {
        postponed {
            val currentSender = sender
            def whenDone = currentSender.foreach (_ ! (WriteFileDone (file, payload)))
            def whenException (exc : Throwable) =
                currentSender match {
                    case None =>
                        error ("Failed to write to file " + file
                               + " with payload " + payload,
                               exc)

                    case Some (actor) =>
                        actor ! (WriteFileFailed (file, exc, payload))
                }

            doWriteFile (file, content, whenDone, whenException)
        }
    }

    def opWriteFile (file : File, content : String) : Operation.WithResult [Unit] = {
        new AbstractOperation [Result[Unit]] {
            override def processRequest () {
                doWriteFile (file,
                             content,
                             yieldResult (Success [Unit] ()),
                             exc => yieldResult (Failure [Unit] (exc)))
            }
        }
    }

    private def doWriteFile (file : File,
                             content : String,
                             whenDone : => Unit,
                             whenException : Throwable => Unit) : Unit =
    {
        val buf = encoder.encode(CharBuffer.wrap (content))
        val handler = new WriteCompletionHandler (buf,
                                                  whenDone,
                                                  whenException)

        try {
            val ch = AsynchronousFileChannel.open (file.toPath,
                                                   WRITE,
                                                   CREATE,
                                                   TRUNCATE_EXISTING)
            handler.setChannel (ch)

            ch.write (buf, 0, null, handler)
        } catch {
            case exc : IOException => handler.failed (exc, null)
        }
    }

    override def readFileAsy (file : File, payload : Any, size : Option[Int] = None) : Unit =
    {
        postponed {
            val currentSender = sender

            def whenDone (content : String) =
                    currentSender.foreach (_ ! (ReadFileDone (file, content, payload)))

            def whenException (exc : Throwable) =
                 currentSender match {
                    case None =>
                        error ("Failed to read from file: " + file
                               + " with payliad " + payload, exc)

                    case Some (actor) =>
                        actor ! (ReadFileFailed (file, exc, payload))
                }

            doReadFile (file, size, whenDone, whenException)
        }
    }

    override def opReadFile (file : File, size : Option[Int] = None) : Operation.WithResult [String] =
    {
        new AbstractOperation [Result[String]] {
            override def processRequest () {
                doReadFile (file,
                            size,
                            cont => yieldResult (Success [String] (cont)),
                            exc => yieldResult (Failure [String] (exc)))
            }
        }
    }

    private def doReadFile (file : File,
                            sizeOption : Option[Int],
                            whenDone : String => Unit,
                            whenException : Throwable => Unit) : Unit =
    {
        var handler = new ReadCompletionHandler (whenDone, whenException)

        try {
            val ch = AsynchronousFileChannel.open (file.toPath, READ)
            val size = sizeOption.getOrElse (ch.size.asInstanceOf[Int])
            val buf = ByteBuffer.allocate (size)
            handler.setChannelAndBuffer (ch, buf)

            ch.read (buf, 0, null, handler)
        } catch {
            case exc : IOException => handler.failed (exc, null)
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Write completion hander

    /**
     * Write completion handler
     */
    private [fs] final class WriteCompletionHandler (buf : ByteBuffer,
                                                     whenDone : => Unit,
                                                     whenException : Throwable => Unit)
                      extends CompletionHandler [java.lang.Integer, Object]
                      with Logging
    {
        private val bufLen = buf.remaining
        private var channel : AsynchronousFileChannel = null

        /**
         * Associate a channel this handler serves.
         */
        def setChannel (ch : AsynchronousFileChannel) = channel = ch

        /**
         * Called when write operation is finished.
         */
        override def completed (bytes : java.lang.Integer,
                                ignored : Object) : Unit = {
            if (bufLen != bytes) {
                failed (new IOException ("Only " + bytes
                                         + " number of bytes out of "
                                         + bufLen + " has been written"),
                        null)
            } else {
                try {
                    whenDone
                } finally {
                    closeChannel ()
                }
            }
        }

        /**
         * Called when write operation failed.
         */
        override def failed (exc : Throwable, ignored : Object) : Unit = {
            try {
                whenException (exc)
            } finally {
                closeChannel ()
            }
        }

        def closeChannel () = {
            if (channel != null) {
                logIgnoredException ("unable to close channel of file") {
                    channel.close ()
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Read completion hander

    /**
     * Write completion handler
     */
    private [fs] final class ReadCompletionHandler (whenDone : String => Unit,
                                                    whenException : Throwable => Unit)
                        extends CompletionHandler [java.lang.Integer, Object]
                        with Logging
    {
        private var channel : AsynchronousFileChannel = null
        private var buf : ByteBuffer = null

        /**
         * Associate a channel this handler serves.
         */
        def setChannelAndBuffer (ch : AsynchronousFileChannel, buffer : ByteBuffer) = {
            channel = ch
            this.buf = buffer
        }

        /**
         * Called when write operation is finished.
         */
        override def completed (bytes : java.lang.Integer,
                                ignored : Object) : Unit = {
            try {
                buf.rewind ()
                buf.limit (bytes.asInstanceOf[Int])

                val content : String = decoder.decode (buf).toString
                whenDone (content)
            } finally {
                closeChannel ()
            }
        }

        /**
         * Called when write operation failed.
         */
        override def failed (exc : Throwable, ignored : Object) : Unit = {
            try {
                whenException (exc)
            } finally {
                closeChannel ()
            }
        }

        def closeChannel () = {
            if (channel != null) {
                logIgnoredException ("unable to close channel of file") {
                    channel.close ()
                }
            }
        }
    }
}

