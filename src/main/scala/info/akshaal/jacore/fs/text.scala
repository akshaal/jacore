/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package fs
package text

import java.nio.channels.AsynchronousFileChannel
import java.io.{File, IOException}
import java.nio.charset.CharacterCodingException
import java.nio.file.StandardOpenOption.{READ, WRITE, CREATE, TRUNCATE_EXISTING}
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset

import actor.{Actor, NormalPriorityActorEnv, Operation}
import logger.Logging

// ///////////////////////////////////////////////////////////////////////
// TextFile interface

/**
 * Fast synchronous file reader/writer.
 */
trait TextFile {
    /**
     * Write a given content into the file providing a way to handle result.
     *
     * @param file write content to this file
     * @param content string content to write into the file
     */
    def opWriteFile (file : File, content : String) : Operation.WithResult [Unit]

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

    def opWriteFile (file : File, content : String) : Operation.WithResult [Unit] = {
        new AbstractOperation [Result[Unit]] {
            override def processRequest () {
                doWriteFile (file, content, yieldResult)
            }
        }
    }

    private def doWriteFile (file : File,
                             content : String,
                             yieldResult : Result[Unit] => Unit) : Unit =
    {
        val buf = encoder.encode(CharBuffer.wrap (content))
        val handler = new WriteCompletionHandler (buf, yieldResult)

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

    override def opReadFile (file : File, size : Option[Int] = None) : Operation.WithResult [String] =
    {
        new AbstractOperation [Result[String]] {
            override def processRequest () {
                doReadFile (file, size, yieldResult)
            }
        }
    }

    private def doReadFile (file : File,
                            sizeOption : Option[Int],
                            yieldResult : Result[String] => Unit) : Unit =
    {
        var handler = new ReadCompletionHandler (yieldResult)

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

    private def isUseless (exc : Throwable) : Boolean = {
        exc match {
            case e : IOException if e.getMessage contains ("No such file or directory") => true
            case _ => false
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Write completion hander

    /**
     * Write completion handler
     */
    private [fs] final class WriteCompletionHandler (buf : ByteBuffer,
                                                     yieldResult : Result[Unit] => Unit)
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
            closeChannelAfter {
                if (bufLen != bytes) {
                    val msg = "Only " + bytes + " number of bytes out of " + bufLen + " has been written"
                    
                    yieldResult (Failure [Unit] (msg))
                } else {
                    yieldResult (Success [Unit] ())
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

        private def closeChannelAfter (code : => Unit) : Unit = {
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

    // //////////////////////////////////////////////////////////////////////
    // Read completion hander

    /**
     * Write completion handler
     */
    private [fs] final class ReadCompletionHandler (yieldResult : Result[String] => Unit)
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
            closeChannelAfter {
                buf.rewind ()
                buf.limit (bytes.asInstanceOf[Int])

                try {
                    val content : String = decoder.decode (buf).toString
                    yieldResult (Success (content))
                } catch {
                    case exc : CharacterCodingException =>
                        yieldResult (Failure [String] ("Unable to decode string from file", None))
                }
            }
        }

        /**
         * Called when write operation failed.
         */
        override def failed (exc : Throwable, ignored : Object) : Unit = {
            closeChannelAfter {
                val optExc = if (isUseless (exc)) None else Some(exc)
                yieldResult (Failure [String] ("Unable to read from the file", optExc))
            }
        }

        private def closeChannelAfter (code : => Unit) : Unit = {
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
}
