/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit
package fs

import org.specs.SpecificationWithJUnit
import java.io.{File, FileReader, BufferedReader, BufferedWriter, FileWriter}

import Predefs._
import UnitTestHelper._
import system.fs.{WriteFileDone, WriteFileFailed, ReadFileDone, ReadFileFailed}

class TextFileTest extends SpecificationWithJUnit ("TextFile specification") {
    import TextFileTest._

    "TextFile" should {
        "support write operation" in {
            withStartedActor [WriteTestActor] (actor => {
                val file = File.createTempFile ("jacore", "writeTest")
                file.deleteOnExit

                actor.payload  must beNull
                actor.done       must_==  0
                actor.excs       must_==  0

                waitForMessageBatchesAfter (actor, 2) {actor ! (file, "Hi", "1x")}

                readLine (file)  must_==  "Hi"
                actor.payload    must_==  "1x"
                actor.done       must_==  1
                actor.excs       must_==  0

                waitForMessageBatchesAfter (actor, 2) {actor ! (file, "Bye", "2x")}

                readLine (file)  must_==  "Bye"
                actor.payload    must_==  "2x"
                actor.done       must_==  2
                actor.excs       must_==  0

                waitForMessageBatchesAfter (actor, 2) {
                    actor ! (new File ("/oops/oops/ooopsss!!"), "Ooops", "3x")
                }

                actor.payload    must_==  "3x"
                actor.done       must_==  2
                actor.excs       must_==  1
            })
        }

        "support read operation" in {
            withStartedActor [ReadTestActor] (actor => {
                val file = File.createTempFile ("jacore", "readTest")
                file.deleteOnExit

                actor.payload  must beNull
                actor.done     must_==  0
                actor.excs     must_==  0

                writeLine (file, "Hi")
                waitForMessageBatchesAfter (actor, 2) {actor ! (file, "1x")}

                actor.payload  must_==  "1x"
                actor.done     must_==  1
                actor.excs     must_==  0
                actor.content  must_==  "Hi"

                writeLine (file, "Bye")
                waitForMessageBatchesAfter (actor, 2) {actor ! (file, "2x")}

                actor.payload  must_==  "2x"
                actor.done     must_==  2
                actor.excs     must_==  0
                actor.content  must_==  "Bye"

                waitForMessageBatchesAfter (actor, 2) {
                    actor ! (new File ("/ook/ooook/ooooooook"), "3x")
                }

                actor.payload  must_==  "3x"
                actor.done     must_==  2
                actor.excs     must_==  1
                actor.content  must_==  "Bye"
            })
        }
    }

    private def readLine (file : File) : String = {
        val reader = new BufferedReader (new FileReader (file))
        try {
            reader.readLine()
        } finally {
            reader.close ()
        }
    }

    private def writeLine (file : File, s : String) : Unit = {
        val writer = new BufferedWriter (new FileWriter (file))
        try {
            writer.write(s, 0, s.length)
        } finally {
            writer.close ()
        }
    }
}

object TextFileTest {
    class WriteTestActor extends TestActor {
        var done = 0
        var excs = 0
        var payload : Any = null

        override def act () = {
            case msg @ (file : File, content : String, payl : Any) => {
                debug ("Received message: " + msg)
                TestModule.textFile.writeFile (file, content, payl)
            }

            case msg @ WriteFileDone (file, payl) => {
                done = done + 1
                this.payload = payl
                debug ("Received message: " + msg)
            }

            case msg @ WriteFileFailed (file, exc, payl) => {
                this.payload = payl
                excs = excs + 1
                debug ("Received message: " + msg)
            }
        }
    }

    class ReadTestActor extends TestActor {
        var done = 0
        var excs = 0
        var payload : Any = null
        var content : String = null

        override def act () = {
            case msg @ (file : File, payl) => {
                debug ("Received message: " + msg)
                TestModule.textFile.readFile (file, payl)
            }

            case msg @ ReadFileDone (file, cont, payl) => {
                done = done + 1
                this.payload = payl
                this.content = cont
                debug ("Received message: " + msg)
            }

            case msg @ ReadFileFailed (file, exc, payl) => {
                this.payload = payl
                excs = excs + 1
                debug ("Received message: " + msg)
            }
        }
    }
}
