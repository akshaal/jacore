/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.fs

import java.io.{File, FileReader, BufferedReader, BufferedWriter, FileWriter}

import unit.UnitTestHelper._

class TextFileServiceTest extends JacoreSpecWithJUnit ("TextFileService specification") {
    "TextFileService" should {
        "support write operation using result matchers" in {
            val file = File.createTempFile ("jacore", "writeTest")
            file.deleteOnExit

            writeByActor (file, "Hi")  must_==  Success (())
            readLine (file)  must_==  "Hi"

            writeByActor (file, "Bye")  must_==  Success (())
            readLine (file)  must_==  "Bye"

            writeByActor (new File ("/oops/oops/ooopsss!!"), "Oooops")  must haveClass[Failure[Unit]]
        }

        "support read operation using result matchers" in {
            val file = File.createTempFile ("jacore", "readTest")
            file.deleteOnExit

            writeLine (file, "Hi")
            readByActor (file)  must_==  Success ("Hi")
            
            writeLine (file, "Bye")
            readByActor (file)  must_==  Success ("Bye")

            readByActor (new File ("/ook/ooook/ooooooook"))  must haveClass[Failure[String]]
        }
    }

    private def writeByActor (file : File, content : String) : Result [Unit] = {
        TestModule.textFileService.writeFileOperation (file, content).runWithFutureAsy.get
    }

    private def readByActor (file : File) : Result [String] = {
        TestModule.textFileService.readFileOperation (file).runWithFutureAsy.get
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