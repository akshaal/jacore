/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import io.db.jdbc.`type`._
import io.db.jdbc.statement._

import Statement.{Placeholder => Hdr}
import Statement.{ProvidedValue => Prov}

class JdbcStatementsTest extends JacoreSpecWithJUnit ("Statement specification") {
    val s0_1 : Statement0 = "select 1"
    val s0_2 : Statement0 = "set xxx"

    val s0_vint = s0_1 ++ (JdbcInt, 4)
    val s0_vstring = s0_2 ++ (JdbcString, "xxx")

    val s1_int = s0_1 ++ JdbcInt
    val s1_string = s0_1 ++ JdbcString
    val s1_blob = s0_1 ++ JdbcBlob
    val s1_clob = s0_2 ++ JdbcClob
    val s1_int_vstr = "insert into x values (" ++ JdbcInt ++ "," ++ (JdbcString, "x") ++ ")"
    val s1_vint_str = "insert into x values (" ++ (JdbcInt, 10) ++ "," ++ JdbcString ++ ")"

    // Two objects should be equals regardless their type
    def checkArgs (manifest : Manifest [_], args : Manifest [_]*) : Unit = {
        manifest.typeArguments  must_==  args.toList
    }

    // Check statement
    def checkStmt (stmt : Statement, sql : String, pvs : Any*) : Unit = {
        stmt.sql                    must_==  sql
        stmt.providedValues.toList  must_==  pvs.toList
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement0" should {
        "be constructable from strings" in {
            checkStmt (s0_2, "set xxx")
            checkStmt (s0_1, "select 1")
        }

        "support concatenation with strings" in {
            checkStmt (s0_1 ++ "from A",      "select 1 from A")
            checkStmt (s0_2 ++ "1",           "set xxx 1")
            checkStmt (s0_vstring ++ "abc",   "set xxx ? abc",  Prov (JdbcString, "xxx", 1))
        }

        "support concatenation with Statement0 objects" in {
            checkStmt (s0_1 ++ s0_2,           "select 1 set xxx")
            checkStmt (s0_2 ++ s0_1,           "set xxx select 1")
            checkStmt (s0_1 ++ s0_1,           "select 1 select 1")
            checkStmt (s0_1 ++ s0_2 ++ s0_1,   "select 1 set xxx select 1")
            checkStmt (s0_vint ++ s0_1,        "select 1 ? select 1", Prov (JdbcInt, 4, 1))

            (s0_1 ++ s0_2)  must haveClass [Statement0]
        }

        "support concatenation with provided values" in {
            checkStmt (s0_vint,     "select 1 ?",  Prov (JdbcInt, 4, 1))
            checkStmt (s0_vstring,  "set xxx ?",   Prov (JdbcString, "xxx", 1))

            checkStmt (s0_vint ++ s0_vstring,  "select 1 ? set xxx ?",
                       Prov (JdbcInt, 4, 1), Prov (JdbcString, "xxx", 2))

            checkStmt (s0_vstring ++ s0_vint,  "set xxx ? select 1 ?",
                       Prov (JdbcString, "xxx", 1), Prov (JdbcInt, 4, 2))

            s0_vint     must haveClass [Statement0]
            s0_vstring  must haveClass [Statement0]
        }

        "construct Statement1 objects" in {
            def check [T <: AbstractJdbcType [_], S <: Statement1 [T]]
                        (s : S, t : T, p : Int = 1) (implicit sm : Manifest [S], tm : Manifest [T]) : Unit =
            {
                checkArgs (sm, tm)

                (s.placeholder : Hdr [T])  must_==  Hdr (t, p)
            }

            check (s1_int,      JdbcInt)
            check (s1_string,   JdbcString)
            check (s1_blob,     JdbcBlob)
            check (s1_clob,     JdbcClob)
            check (s1_int_vstr, JdbcInt)
            check (s1_vint_str, JdbcString, 2)

            checkStmt (s1_int,      "select 1 ?")
            checkStmt (s1_string,   "select 1 ?")
            checkStmt (s1_blob,     "select 1 ?")
            checkStmt (s1_clob,     "set xxx ?")
            checkStmt (s1_int_vstr, "insert into x values ( ? , ? )", Prov (JdbcString, "x", 2))
            checkStmt (s1_vint_str, "insert into x values ( ? , ? )", Prov (JdbcInt, 10, 1))
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement1" should {
        "support concatenation with strings" in {
            val s = s1_int ++ "from ABC"

            checkStmt (s, "select 1 ? from ABC")
            s.placeholder  must_==  Hdr (JdbcInt, 1)
        }

        "support concatenation with Statement0 objects and provided values" in {
            val ls1 = s1_string ++ s0_2
            checkStmt (ls1, "select 1 ? set xxx")
            ls1.placeholder  must_==  Hdr (JdbcString, 1)

            val ls2 = "a" ++ JdbcBytes ++ (JdbcInt, 100) ++ s0_2
            checkStmt (ls2, "a ? ? set xxx", Prov (JdbcInt, 100, 2))
            ls2.placeholder  must_==  Hdr (JdbcBytes, 1)

            val ls3 = "a" ++ (JdbcString, "xyz") ++ "x" ++ JdbcBytes ++ (JdbcInt, 50) ++ s0_2
            checkStmt (ls3, "a ? x ? ? set xxx", Prov (JdbcString, "xyz", 1), Prov (JdbcInt, 50, 3))
            ls3.placeholder  must_==  Hdr (JdbcBytes, 2)
        }
    }
}
