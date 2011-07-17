/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import io.db.jdbc.`type`._
import io.db.jdbc.statement._

class JdbcStatementsTest extends JacoreSpecWithJUnit ("Statement specification") {
    val s0_1 : Statement0 = "select 1"
    val s0_2 : Statement0 = "set xxx"

    val s1_int = s0_1 + JdbcInt
    val s1_string = s0_1 + JdbcString
    val s1_blob = s0_1 + JdbcBlob
    val s1_clob = s0_2 + JdbcClob

    "Statement0" should {
        "be constructable from strings" in {
            s0_2.sql  must_==  "set xxx"
            s0_1.sql  must_==  "select 1"
        }

        "support concatenation with strings" in {
            (s0_1 + "from A").sql  must_==  "select 1 from A"
            (s0_2 + "1").sql       must_==  "set xxx 1"
        }

        "support concatenation with Statement0 objects" in {
            (s0_1 + s0_2).sql         must_==  "select 1 set xxx"
            (s0_2 + s0_1).sql         must_==  "set xxx select 1"
            (s0_1 + s0_1).sql         must_==  "select 1 select 1"
            (s0_1 + s0_2 + s0_1).sql  must_==  "select 1 set xxx select 1"

            (s0_1 + s0_2)  must haveClass [Statement0]
        }

        "construct Statement1 objects" in {
            def check [S, T] (s : S, t : T) (implicit sm : Manifest [S], tm : Manifest [T]) : Unit = {
                s    must haveClass [Statement1 [_]]
                s    must haveClass [S]
                t    must haveSuperClass [JdbcType [_]]
                t    must haveClass [T]

                sm.typeArguments.head.asInstanceOf [Object]  must_==  tm.asInstanceOf [Object]
            }

            check (s1_int, JdbcInt)
            check (s1_string, JdbcString)
            check (s1_blob, JdbcBlob)
            check (s1_clob, JdbcClob)

            s1_int.sql     must_==  "select 1 ?"
            s1_string.sql  must_==  "select 1 ?"
            s1_blob.sql    must_==  "select 1 ?"
            s1_clob.sql    must_==  "set xxx ?"
        }
    }
}
