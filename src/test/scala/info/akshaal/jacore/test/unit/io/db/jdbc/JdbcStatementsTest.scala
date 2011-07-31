/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import io.db.jdbc.`type`._
import io.db.jdbc.statement._

import Statement.{Placeholder => Hdr}
import Statement.{ProvidedValue => Prov}

class JdbcStatementsTest extends JacoreSpecWithJUnit ("Statement specification") {
    val s0_1 : Statement0 [Nothing] = "select 1"
    val s0_2 : Statement0 [Nothing] = "set xxx"

    case class X (x : Int, y : String)

    // println ((s0_1 +++ (JdbcInt, ((_ : X).x))).domainPlaceholders)
    // println ((classOf [X] /: s0_1 +++ (JdbcInt, _.x) +++ (JdbcString, _.y)).domainPlaceholders)
    // println ((classOf [X] /: classOf [X] /: s0_1 +++ (JdbcInt, _.x) +++ (JdbcString, _.y)).domainPlaceholders)

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
    val s5 = s4 ++ JdbcLong
    val s6 = s5 ++ (JdbcString, "test") ++ JdbcInt
    val s7 = s6 ++ JdbcString
    val s8 = s7 ++ JdbcInt
    val s9 = s8 ++ JdbcBytes
    val s10 = s9 ++ (JdbcLong, 10L) ++ JdbcInt ++ "x" ++ (JdbcFloat, 1.0f)

    // Check statement
    def checkStmt (stmt : Statement [_], sql : String, pvs : Any*) : Unit = {
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

        "support concatenation with provided values" in {
            checkStmt (s0_vint,     "select 1 ?",  Prov (JdbcInt, 4, 1))
            checkStmt (s0_vstring,  "set xxx ?",   Prov (JdbcString, "xxx", 1))

            s0_vint     must haveClass [Statement0 [_]]
            s0_vstring  must haveClass [Statement0 [_]]
        }

        "construct Statement1 objects" in {
            def checkArgs (manifest : Manifest [_], args : Manifest [_]*) : Unit = {
                manifest.typeArguments.drop (1)  must_==  args.toList
            }

            def check [T <: AbstractJdbcType [_], S <: Statement1 [_, T]]
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

            s1_int  must haveClass [Statement1 [_, _]]
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

        "support concatenation with provided values" in {
            val ls2 = "a" ++ JdbcBytes ++ (JdbcInt, 100)
            checkStmt (ls2, "a ? ?", Prov (JdbcInt, 100, 2))
            checkPlaceholder (ls2.placeholder, JdbcBytes, 1)

            val ls3 = "a" ++ (JdbcString, "xyz") ++ "x" ++ JdbcBytes ++ (JdbcInt, 50)
            checkStmt (ls3, "a ? x ? ?", Prov (JdbcString, "xyz", 1), Prov (JdbcInt, 50, 3))
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

            s2_int_blob  must haveClass [Statement2 [_, _, _]]
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

            s3_int_vstr_str_ref  must haveClass [Statement3 [_, _, _, _]]
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

            s4  must haveClass [Statement4 [_, _, _, _, _]]
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement4" should {
        "construct complex Statement5 object" in {
            checkStmt (s5,
                       "select 1 ? ? test ? ? blah ? ?",
                       Prov (JdbcString, "x", 4))

            checkPlaceholder (s5.placeholder1, JdbcInt, 1)
            checkPlaceholder (s5.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s5.placeholder3, JdbcShort, 3)
            checkPlaceholder (s5.placeholder4, JdbcBytes, 5)
            checkPlaceholder (s5.placeholder5, JdbcLong, 6)

            s5  must haveClass [Statement5 [_, _, _, _, _, _]]
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement5" should {
        "construct complex Statement6 object" in {
            val s = s6

            checkStmt (s,
                       "select 1 ? ? test ? ? blah ? ? ? ?",
                       Prov (JdbcString, "x", 4),
                       Prov (JdbcString, "test", 7))

            checkPlaceholder (s.placeholder1, JdbcInt, 1)
            checkPlaceholder (s.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s.placeholder3, JdbcShort, 3)
            checkPlaceholder (s.placeholder4, JdbcBytes, 5)
            checkPlaceholder (s.placeholder5, JdbcLong, 6)
            checkPlaceholder (s.placeholder6, JdbcInt, 8)

            s  must haveClass [Statement6 [_, _, _, _, _, _, _]]
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement6" should {
        "construct complex Statement7 object" in {
            val s = s7

            checkStmt (s,
                       "select 1 ? ? test ? ? blah ? ? ? ? ?",
                       Prov (JdbcString, "x", 4),
                       Prov (JdbcString, "test", 7))

            checkPlaceholder (s.placeholder1, JdbcInt, 1)
            checkPlaceholder (s.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s.placeholder3, JdbcShort, 3)
            checkPlaceholder (s.placeholder4, JdbcBytes, 5)
            checkPlaceholder (s.placeholder5, JdbcLong, 6)
            checkPlaceholder (s.placeholder6, JdbcInt, 8)
            checkPlaceholder (s.placeholder7, JdbcString, 9)

            s  must haveClass [Statement7 [_, _, _, _, _, _, _, _]]
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement7" should {
        "construct complex Statement8 object" in {
            val s = s8

            checkStmt (s,
                       "select 1 ? ? test ? ? blah ? ? ? ? ? ?",
                       Prov (JdbcString, "x", 4),
                       Prov (JdbcString, "test", 7))

            checkPlaceholder (s.placeholder1, JdbcInt, 1)
            checkPlaceholder (s.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s.placeholder3, JdbcShort, 3)
            checkPlaceholder (s.placeholder4, JdbcBytes, 5)
            checkPlaceholder (s.placeholder5, JdbcLong, 6)
            checkPlaceholder (s.placeholder6, JdbcInt, 8)
            checkPlaceholder (s.placeholder7, JdbcString, 9)
            checkPlaceholder (s.placeholder8, JdbcInt, 10)

            s  must haveClass [Statement8 [_, _, _, _, _, _, _, _, _]]
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement8" should {
        "construct complex Statement9 object" in {
            val s = s9

            checkStmt (s,
                       "select 1 ? ? test ? ? blah ? ? ? ? ? ? ?",
                       Prov (JdbcString, "x", 4),
                       Prov (JdbcString, "test", 7))

            checkPlaceholder (s.placeholder1, JdbcInt, 1)
            checkPlaceholder (s.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s.placeholder3, JdbcShort, 3)
            checkPlaceholder (s.placeholder4, JdbcBytes, 5)
            checkPlaceholder (s.placeholder5, JdbcLong, 6)
            checkPlaceholder (s.placeholder6, JdbcInt, 8)
            checkPlaceholder (s.placeholder7, JdbcString, 9)
            checkPlaceholder (s.placeholder8, JdbcInt, 10)
            checkPlaceholder (s.placeholder9, JdbcBytes, 11)

            s  must haveClass [Statement9 [_, _, _, _, _, _, _, _, _, _]]
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement9" should {
        "construct complex Statement10 object" in {
            val s = s10

            checkStmt (s,
                       "select 1 ? ? test ? ? blah ? ? ? ? ? ? ? ? ? x ?",
                       Prov (JdbcString, "x", 4),
                       Prov (JdbcString, "test", 7),
                       Prov (JdbcLong, 10L, 12),
                       Prov (JdbcFloat, 1.0f, 14))

            checkPlaceholder (s.placeholder1, JdbcInt, 1)
            checkPlaceholder (s.placeholder2, JdbcBlob, 2)
            checkPlaceholder (s.placeholder3, JdbcShort, 3)
            checkPlaceholder (s.placeholder4, JdbcBytes, 5)
            checkPlaceholder (s.placeholder5, JdbcLong, 6)
            checkPlaceholder (s.placeholder6, JdbcInt, 8)
            checkPlaceholder (s.placeholder7, JdbcString, 9)
            checkPlaceholder (s.placeholder8, JdbcInt, 10)
            checkPlaceholder (s.placeholder9, JdbcBytes, 11)
            checkPlaceholder (s.placeholder10, JdbcInt, 13)

            s  must haveClass [Statement10 [_, _, _, _, _, _, _, _, _, _, _]]
        }
    }
}
