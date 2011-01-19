/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.io

import java.io.PrintWriter

import unit.UnitTestHelper._

import utils.io.CustomLineWriter

class CustomLineWriterTest extends JacoreSpecWithJUnit ("CustomLineWriter class specification") {
    "CustomLineWriter" should {
        "work" in {
            var chunks : List[String] = Nil

            val lineWriter = new CustomLineWriter ((s : String) => chunks ::= s)
            val pw = new PrintWriter (lineWriter)
            pw.print (5)
            pw.print ("xxxx")
            pw.println ()
            pw.println ()
            pw.println ("ee")

            pw.println ("1\n\n3")
            pw.print ("x")

            chunks  must_==  List ("3", "1\n", "ee", "", "5xxxx")

            pw.println ("g")

            chunks  must_==  List ("xg", "3", "1\n", "ee", "", "5xxxx")
        }
    }
}
