/*
 * FileActor.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package fs

import java.nio.channels.AsynchronousFileChannel
import java.io.{File, IOException}
import java.nio.file.StandardOpenOption.{READ, WRITE, CREATE, TRUNCATE_EXISTING}
import java.nio.{ByteBuffer, CharBuffer}
import java.nio.channels.CompletionHandler
import java.nio.charset.Charset

import com.google.inject.{Inject, Singleton}

import Predefs._
import actor.{Actor, NormalPriorityActorEnv}
import logger.Logging
import annotation.CallByMessage

// ///////////////////////////////////////////////////////////////////////
// Messages

abstract sealed class FileMessage extends NotNull

final case class WriteFileDone (file : File,
                                payload : Any)
                            extends FileMessage

final case class WriteFileFailed (file : File,
                                  exc : Throwable,
                                  payload : Any)
                            extends FileMessage

final case class ReadFileDone (file : File,
                               content : String,
                               payload : Any)
                            extends FileMessage

final case class ReadFileFailed (file : File,
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
    def writeFile (file : File, content : String, payload : Any) : Unit

    /**
     * Open file and initiate reading from the file. When reading is done a message will sended
     * to the caller actor. The possible result messages are <code>ReadFileDone</code>
     * and <code>ReadFileFailed</code>.
     * @param file file to read from
     * @param payload payload to passed back in messsage when reading is done
     */
    def readFile (file : File, payload : Any) : Unit
}

// ///////////////////////////////////////////////////////////////////////
// TextFile actor

/**
 * Fast async file reader/writer.
 */
@Singleton
private[system] class TextFileActor @Inject() (
                       normalPriorityActorEnv : NormalPriorityActorEnv,
                       prefs : Prefs)
                    extends Actor (actorEnv = normalPriorityActorEnv)
                       with TextFile
{
    private val encoding = prefs.getString ("jacore.os.file.encoding")
    private val encoder = Charset.forName (encoding).newEncoder ()
    private val decoder = Charset.forName (encoding).newDecoder ()

    @CallByMessage
    override def writeFile (file : File, content : String, payload : Any) : Unit =
    {
        val buf = encoder.encode(CharBuffer.wrap (content))
        val handler = new WriteCompletionHandler (buf, file, sender, payload)

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

    @CallByMessage
    override def readFile (file : File, payload : Any) : Unit =
    {
        var handler = new ReadCompletionHandler (file, sender, payload)

        try {
            val ch = AsynchronousFileChannel.open (file.toPath, READ)
            val buf = ByteBuffer.allocate (ch.size.asInstanceOf[Int])
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
                                                     file : File,
                                                     sender : Option[Actor],
                                                     payload : Any)
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
                                         + bufLen + " has been written to file "
                                         + file + " with payload " + payload),
                        null)
            } else {
                try {
                    sender.foreach (_ ! (WriteFileDone (file, payload)))
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
                sender match {
                    case None =>
                        error ("Failed to write to file " + file
                               + " with payload " + payload,
                               exc)

                    case Some (actor) =>
                        actor ! (WriteFileFailed (file, exc, payload))
                }
            } finally {
                closeChannel ()
            }
        }

        /**
         * Called when write operation is canceled. This must not happen.
         */
        override def cancelled (ignored : Object) : Nothing = {
            throw new RuntimeException ("Impossible")
        }

        def closeChannel () = {
            if (channel != null) {
                logIgnoredException ("unable to close channel of file "
                                     + file + " with payload " + payload) {
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
    private [fs] final class ReadCompletionHandler (file : File,
                                                    sender : Option[Actor],
                                                    payload : Any)
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
                val content : String = decoder.decode (buf).toString

                sender.foreach (_ ! (ReadFileDone (file, content, payload)))
            } finally {
                closeChannel ()
            }
        }

        /**
         * Called when write operation failed.
         */
        override def failed (exc : Throwable, ignored : Object) : Unit = {
            try {
                sender match {
                    case None =>
                        error ("Failed to read from file: " + file
                               + " with payliad " + payload, exc)

                    case Some (actor) =>
                        actor ! (ReadFileFailed (file, exc, payload))
                }
            } finally {
                closeChannel ()
            }
        }

        /**
         * Called when read operation is canceled. This must not happen.
         */
        override def cancelled (ignored : Object) : Nothing = {
            throw new RuntimeException ("Impossible")
        }

        def closeChannel () = {
            if (channel != null) {
                logIgnoredException ("unable to close channel of file: "
                                     + file + " with payliad " + payload) {
                    channel.close ()
                }
            }
        }
    }
}

