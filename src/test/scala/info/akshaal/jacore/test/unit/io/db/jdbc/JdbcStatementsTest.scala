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

    val s2_int_blob = s1_int ++ JdbcBlob
    val s2_int_vstr_str = s1_int_vstr ++ JdbcString
    val s2_vint_str_blob = s1_vint_str ++ JdbcBlob

    val s3_int_blob_short_vstr = s2_int_blob ++ "test" ++ JdbcShort ++ (JdbcString, "x")
    val s3_int_vstr_str_ref = s2_int_vstr_str ++ JdbcRef ++ "m2"

    val s4 = s3_int_blob_short_vstr ++ "blah" ++ JdbcBytes

    // Two objects should be equals regardless their type
    def checkArgs (manifest : Manifest [_], args : Manifest [_]*) : Unit = {
        manifest.typeArguments  must_==  args.toList
    }

    // Check statement
    def checkStmt (stmt : Statement, sql : String, pvs : Any*) : Unit = {
        stmt.sql                    must_==  sql
        stmt.providedValues.toList  must_==  pvs.toList
    }

    // Typesafe check for paceholders
    def checkPlaceholder [T <: AbstractJdbcType [_], P <: Hdr [T]] (
                        placeholder : P, jdbcType : T, pos : Int) : Unit =
    {
        placeholder  must_==  Hdr (jdbcType, pos)
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

            s1_int  must haveClass [Statement1 [_]]
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
            checkPlaceholder (s.placeholder,  JdbcInt, 1)
        }

        "support concatenation with Statement0 objects and provided values" in {
            val ls1 = s1_string ++ s0_2
            checkStmt (ls1, "select 1 ? set xxx")
            checkPlaceholder (ls1.placeholder, JdbcString, 1)

            val ls2 = "a" ++ JdbcBytes ++ (JdbcInt, 100) ++ s0_2
            checkStmt (ls2, "a ? ? set xxx", Prov (JdbcInt, 100, 2))
            checkPlaceholder (ls2.placeholder, JdbcBytes, 1)

            val ls3 = "a" ++ (JdbcString, "xyz") ++ "x" ++ JdbcBytes ++ (JdbcInt, 50) ++ s0_2
            checkStmt (ls3, "a ? x ? ? set xxx", Prov (JdbcString, "xyz", 1), Prov (JdbcInt, 50, 3))
            checkPlaceholder (ls3.placeholder, JdbcBytes, 2)
        }

        "construct Statement2 objects" in {
            checkStmt (s2_int_blob,      "select 1 ? ?")
            checkStmt (s2_int_vstr_str,  "insert into x values ( ? , ? ) ?", Prov (JdbcString, "x", 2))
            checkStmt (s2_vint_str_blob, "insert into x values ( ? , ? ) ?", Prov (JdbcInt, 10, 1))

            checkPlaceholder (s2_int_blob.placeholder1, JdbcInt, 1)
            checkPlaceholder (s2_int_blob.placeholder2, JdbcBlob, 2)

            checkPlaceholder (s2_int_vstr_str.placeholder1, JdbcInt, 1)
            checkPlaceholder (s2_int_vstr_str.placeholder2, JdbcString, 3)

            checkPlaceholder (s2_vint_str_blob.placeholder1, JdbcString, 2)
            checkPlaceholder (s2_vint_str_blob.placeholder2, JdbcBlob, 3)
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement2" should {
        "construct complex Statement3 object" in {
            checkStmt (s3_int_blob_short_vstr,
                       "select 1 ? ? test ? ?",
                       Prov (JdbcString, "x", 4))

            checkPlaceholder (s3_int_blob_short_vstr.placeholder1, JdbcInt, 1)
            checkPlaceholder (s3_int_blob_short_vstr.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s3_int_blob_short_vstr.placeholder3, JdbcShort, 3)

            // - - -  - - -

            checkStmt (s3_int_vstr_str_ref,
                      "insert into x values ( ? , ? ) ? ? m2",
                      Prov (JdbcString, "x", 2))

            checkPlaceholder (s3_int_vstr_str_ref.placeholder1, JdbcInt, 1)
            checkPlaceholder (s3_int_vstr_str_ref.placeholder2, JdbcString, 3)
            checkPlaceholder (s3_int_vstr_str_ref.placeholder3, JdbcRef, 4)
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement3" should {
        "construct complex Statement4 object" in {
            checkStmt (s4,
                       "select 1 ? ? test ? ? blah ?",
                       Prov (JdbcString, "x", 4))

            checkPlaceholder (s4.placeholder1, JdbcInt, 1)
            checkPlaceholder (s4.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s4.placeholder3, JdbcShort, 3)
            checkPlaceholder (s4.placeholder4, JdbcBytes, 5)
        }
    }
}
