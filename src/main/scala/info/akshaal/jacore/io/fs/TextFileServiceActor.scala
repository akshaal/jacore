/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package fs

import java.io.File
import java.nio.charset.CharacterCodingException
import java.nio.CharBuffer
import java.nio.charset.Charset

import actor.{Actor, NormalPriorityActorEnv, Operation}

/**
 * Fast async actor to read/write text files.
 * @param normalPriorityActorEnv environment for an actor with normal priority
 * @param fileService file service
 * @param echoding encoding to use for files
 */
@Singleton
private[jacore] class TextFileServiceActor @Inject() (
                                normalPriorityActorEnv : NormalPriorityActorEnv,
                                fileService : FileService,
                                @Named ("jacore.os.file.encoding") encoding : String)
                    extends Actor (actorEnv = normalPriorityActorEnv)
                       with TextFileService
{
    private val encoder = Charset.forName (encoding).newEncoder ()
    private val decoder = Charset.forName (encoding).newDecoder ()

    /**
     * Write file operation.
     * @param file file to write
     * @param content context to write
     */
    override def writeFileOperation (file : File, content : String) : Operation.WithResult [Unit] =
        new AbstractOperation [Result[Unit]] {
            override def processRequest () {
                val buf = encoder.encode (CharBuffer.wrap (content))
                fileService.writeFileOperation (file, buf) runMatchingResultAsy yieldResult
            }
        }

    /**
     * Open file and initiate reading from the file.
     *
     * @param file file to read from
     * @param size maximum number of bytes to read, if not given, then the whole file will be read
     *        and some time will be spent to obtain file size
     * @return content of the file
     */
    override def readFileOperation (file : File, size : Option[Int] = None) : Operation.WithResult [String] =
        new AbstractOperation [Result [String]] {
            override def processRequest () {
                fileService.readFileOperation (file, size = size) runMatchingResultAsy {
                    case Failure (msg, excOpt) =>
                        yieldResult (Failure [String] (msg, excOpt))

                    case Success (buf) =>
                        try {
                            val content : String = decoder.decode (buf).toString
                            yieldResult (Success (content))
                        } catch {
                            case exc : CharacterCodingException =>
                            yieldResult (Failure [String] ("Unable to decode string from file", None))
                        }
                }
            }
        }
}
