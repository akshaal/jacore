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

import system.fs.{WriteFile, WriteFileDone, WriteFileFailed}

class FileActorTest extends BaseUnitTest {
    @Test (groups=Array("unit"))
    def testWrite () = {
        WriteTestActor.start

        val file = File.createTempFile ("jacore", "test")
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

    // TODO: Write reading test

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
        case msg @ (file : File, content : String, payl) => {
            debug ("Received message: " + msg)
            UnitTestModule.fileActor ! (WriteFile (file, content, payl))
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
