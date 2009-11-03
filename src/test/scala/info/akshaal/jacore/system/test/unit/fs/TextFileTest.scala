/*
 * ActorTest.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit.fs

import collection.immutable.List
import org.testng.annotations.Test
import org.testng.Assert._
import java.io.{File, FileReader, BufferedReader, BufferedWriter, FileWriter}

import system.test.unit.{BaseUnitTest, UnitTestModule, HiPriorityActor}

import system.fs.{WriteFileDone, WriteFileFailed, ReadFileDone, ReadFileFailed}

class TextFileTest extends BaseUnitTest {
    @Test (groups=Array("unit"))
    def testWrite () = {
        WriteTestActor.start

        val file = File.createTempFile ("jacore", "writeTest")
        file.deleteOnExit

        assertNull (WriteTestActor.payload)
        WriteTestActor ! (file, "Hi", "1x")

        sleep ()
        assertEquals (readLine (file), "Hi")
        assertEquals (WriteTestActor.payload, "1x")
        assertEquals (WriteTestActor.done, 1)
        assertEquals (WriteTestActor.excs, 0)

        WriteTestActor ! (file, "Bye", "2x")
        sleep ()
        assertEquals (readLine (file), "Bye")
        assertEquals (WriteTestActor.payload, "2x")
        assertEquals (WriteTestActor.done, 2)
        assertEquals (WriteTestActor.excs, 0)

        WriteTestActor ! (new File ("/oops/oops/ooopsss!!"), "Ooops", "3x")
        sleep ()
        assertEquals (WriteTestActor.done, 2)
        assertEquals (WriteTestActor.payload, "3x")
        assertEquals (WriteTestActor.excs, 1)

        WriteTestActor.stop
    }

    @Test (groups=Array("unit"))
    def testRead () = {
        ReadTestActor.start

        val file = File.createTempFile ("jacore", "readTest")
        file.deleteOnExit

        assertNull (ReadTestActor.payload)

        writeLine (file, "Hi")
        ReadTestActor ! (file, "1x")
        sleep
        assertEquals (ReadTestActor.payload, "1x")
        assertEquals (ReadTestActor.done, 1)
        assertEquals (ReadTestActor.excs, 0)
        assertEquals (ReadTestActor.content, "Hi")

        writeLine (file, "Bye")
        ReadTestActor ! (file, "2x")
        sleep
        assertEquals (ReadTestActor.payload, "2x")
        assertEquals (ReadTestActor.done, 2)
        assertEquals (ReadTestActor.excs, 0)
        assertEquals (ReadTestActor.content, "Bye")

        ReadTestActor ! (new File ("/ook/ooook/ooooooook"), "3x")
        sleep
        assertEquals (ReadTestActor.payload, "3x")
        assertEquals (ReadTestActor.done, 2)
        assertEquals (ReadTestActor.excs, 1)
        assertEquals (ReadTestActor.content, "Bye")

        ReadTestActor.stop
    }

    private def sleep () = Thread.sleep (1000)

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

object WriteTestActor extends HiPriorityActor {
    var done = 0
    var excs = 0
    var payload : Any = null

    override def act () = {
        case msg @ (file : File, content : String, payl : Any) => {
            debug ("Received message: " + msg)
            UnitTestModule.textFile.writeFile (file, content, payl)
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

object ReadTestActor extends HiPriorityActor {
    var done = 0
    var excs = 0
    var payload : Any = null
    var content : String = null

    override def act () = {
        case msg @ (file : File, payl) => {
            debug ("Received message: " + msg)
            UnitTestModule.textFile.readFile (file, payl)
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
