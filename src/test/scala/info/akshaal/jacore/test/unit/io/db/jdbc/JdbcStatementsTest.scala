/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import io.db.jdbc.`type`._
import io.db.jdbc.statement._

import Statement.{Placeholder => Hdr}
import Statement.{ProvidedValue => Prov}

class JdbcStatementsTest extends JacoreSpecWithJUnit ("Statement specification") {
    case class X (x : Int, y : String)
    case class Y (a : String, b : Int)

    val s0_1 : Statement0 [Domainless] = "select 1"
    val s0_2 : Statement0 [Domainless] = "set xxx"

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

    val sd0_1 = classOf [X] /:: s0_1 +++ (JdbcInt, ((_ : X).x))
    val sd0_2 = classOf [Y] /:: s0_2 +++ (JdbcString, _.a) +++ (JdbcInt, _.b)
    val sd0_3 = classOf [X] /:: classOf [X] /:: s0_1 +++ (JdbcInt, _.x) ++ "abc" +++ (JdbcString, _.y)

    val sd1 = sd0_2 ++ JdbcRef
    val sd10 = classOf[X] /:: s10 +++ (JdbcString, _.y)

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

    // Get manifest of variable
    def manifestOf [M] (v : M) (implicit m : Manifest [M]) : Manifest [M] = m

    // Check type arguments
    def checkArgs (manifest : Manifest [_], args : Manifest [_]*) : Unit = {
        manifest.typeArguments  must_==  args.toList
    }

    // Check type arguments of value
    def checkArgs [V] (v : V, args : Manifest [_]*) (implicit manifest : Manifest [V]) : Unit = {
        manifest.typeArguments  must_==  args.toList
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement0" should {
        "be constructable from strings" in {
            checkStmt (s0_2, "set xxx")
            checkArgs (s0_2, manifest [Domainless])
            s0_2.domainPlaceholders  must_==  Vector ()

            checkStmt (s0_1, "select 1")
            checkArgs (s0_1, manifest [Domainless])
            s0_1.domainPlaceholders  must_==  Vector ()
        }

        "append other Statement0" in {
            val z1 = s0_1 ++ s0_2
            checkStmt (z1, "select 1 set xxx")
            checkArgs (z1, manifest [Domainless])
            z1.domainPlaceholders  must_==  Vector ()
            z1  must haveClass [Statement0 [_]]

            val x = X (x = 555, y = "asd")
            val z2 = sd0_1 ++ s0_1
            checkStmt (z2, "select 1 ? select 1")
            checkArgs (z2, manifest [X])
            val z2_dps = z2.domainPlaceholders
            z2_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            z2_dps.map (_.f (x))     must_==  Vector (555)
            z2_dps.map (_.position)  must_==  Vector (1)
            z1  must haveClass [Statement0 [_]]
        }

        "append Statement1" in {
            val z = s0_1 ++ s1_string
            checkStmt (z, "select 1 select 1 ?")
            checkArgs (z, manifest [Domainless], manifestOf (JdbcString))
            z.domainPlaceholders  must_==  Vector ()
            z  must haveClass [Statement1 [_, _]]
            checkPlaceholder (z.placeholder, JdbcString, 1)
        }

        "append Statement2" in {
            val z = s0_1 ++ s2_int_vstr_str
            checkStmt (z,  "select 1 insert into x values ( ? , ? ) ?", Prov (JdbcString, "x", 2))
            checkArgs (z, manifest [Domainless], manifestOf (JdbcInt), manifestOf (JdbcString))
            z.domainPlaceholders  must_==  Vector ()
            z  must haveClass [Statement2 [_, _, _]]
            checkPlaceholder (z.placeholder1, JdbcInt, 1)
            checkPlaceholder (z.placeholder2, JdbcString, 3)
        }

        "append Statement3" in {
            val z = s0_2 ++ s3_int_blob_short_vstr

            checkStmt (z,
                       "set xxx select 1 ? ? test ? ?",
                       Prov (JdbcString, "x", 4))

            z  must haveClass [Statement3 [_, _, _, _]]
            checkArgs (z, manifest [Domainless], manifestOf (JdbcInt), manifestOf (JdbcBlob), manifestOf (JdbcShort))

            checkPlaceholder (z.placeholder1, JdbcInt, 1)
            checkPlaceholder (z.placeholder2, JdbcBlob, 2)
            checkPlaceholder (z.placeholder3, JdbcShort, 3)
            z.domainPlaceholders  must_==  Vector ()
        }

        "append Statement4" in {
            val z = sd0_1 ++ s4

            checkStmt (z,
                       "select 1 ? select 1 ? ? test ? ? blah ?",
                       Prov (JdbcString, "x", 5))

            checkPlaceholder (z.placeholder1, JdbcInt, 2)
            checkPlaceholder (z.placeholder2, JdbcBlob, 3)
            checkPlaceholder (z.placeholder3, JdbcShort, 4)
            checkPlaceholder (z.placeholder4, JdbcBytes, 6)

            s4  must haveClass [Statement4 [_, _, _, _, _]]
            checkArgs (z,
                       manifest [X],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes))

            val x = X (x = 15, y = "gom")
            val z_dps = z.domainPlaceholders
            z_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            z_dps.map (_.f (x))     must_==  Vector (15)
            z_dps.map (_.position)  must_==  Vector (1)
        }

        "append Statement5" in {
            val z = sd0_1 ++ s5

            checkStmt (z,
                       "select 1 ? select 1 ? ? test ? ? blah ? ?",
                       Prov (JdbcString, "x", 5))

            checkPlaceholder (z.placeholder1, JdbcInt, 2)
            checkPlaceholder (z.placeholder2, JdbcBlob, 3)
            checkPlaceholder (z.placeholder3, JdbcShort, 4)
            checkPlaceholder (z.placeholder4, JdbcBytes, 6)
            checkPlaceholder (z.placeholder5, JdbcLong, 7)

            z  must haveClass [Statement5 [_, _, _, _, _, _]]
            checkArgs (z,
                       manifest [X],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong))

            val x = X (x = 666, y = "gom")
            val z_dps = z.domainPlaceholders
            z_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            z_dps.map (_.f (x))     must_==  Vector (666)
            z_dps.map (_.position)  must_==  Vector (1)
        }

        "append Statement6" in {
            val s = sd0_1 ++ s6

            checkStmt (s,
                       "select 1 ? select 1 ? ? test ? ? blah ? ? ? ?",
                       Prov (JdbcString, "x", 5),
                       Prov (JdbcString, "test", 8))

            checkPlaceholder (s.placeholder1, JdbcInt, 2)
            checkPlaceholder (s.placeholder2, JdbcBlob, 3)
            checkPlaceholder (s.placeholder3, JdbcShort, 4)
            checkPlaceholder (s.placeholder4, JdbcBytes, 6)
            checkPlaceholder (s.placeholder5, JdbcLong, 7)
            checkPlaceholder (s.placeholder6, JdbcInt, 9)

            s  must haveClass [Statement6 [_, _, _, _, _, _, _]]
            checkArgs (s,
                       manifest [X],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong),
                       manifestOf (JdbcInt))

            val x = X (x = 13, y = "gom")
            val s_dps = s.domainPlaceholders
            s_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            s_dps.map (_.f (x))     must_==  Vector (13)
            s_dps.map (_.position)  must_==  Vector (1)
        }

        "have domain orianted statements" in {
            val x = X (x = 100500, y = "asd")
            val sd0_1_dps = sd0_1.domainPlaceholders
            checkStmt (sd0_1, "select 1 ?")
            checkArgs (sd0_1, manifest [X])
            sd0_1_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            sd0_1_dps.map (_.f (x))     must_==  Vector (100500)
            sd0_1_dps.map (_.position)  must_==  Vector (1)

            val y = Y (a = "a", b = 3)
            val sd0_2_dps = sd0_2.domainPlaceholders
            checkStmt (sd0_2, "set xxx ? ?")
            checkArgs (sd0_2, manifest [Y])
            sd0_2_dps.map (_.jdbcType)  must_==  Vector (JdbcString, JdbcInt)
            sd0_2_dps.map (_.f (y))     must_==  Vector ("a", 3)
            sd0_2_dps.map (_.position)  must_==  Vector (1, 2)

            val sd0_3_dps = sd0_3.domainPlaceholders
            checkStmt (sd0_3, "select 1 ? abc ?")
            checkArgs (sd0_3, manifest [X])
            sd0_3_dps.map (_.jdbcType)  must_==  Vector (JdbcInt, JdbcString)
            sd0_3_dps.map (_.f (x))     must_==  Vector (100500, "asd")
            sd0_3_dps.map (_.position)  must_==  Vector (1, 2)
        }

        "provide domain type narrowing method" in {
            val xs1 = classOf [Object] /:: "hello"
            val xs2 = classOf [X] /:: xs1

            checkArgs (xs1, manifest [Object])
            checkArgs (xs2, manifest [X])
        }

        "support concatenation with strings for domain orianted statements" in {
            val s = sd0_1 ++ "blah"

            val x = X (x = 99, y = "omg")
            val s_dps = s.domainPlaceholders
            checkStmt (s, "select 1 ? blah")
            checkArgs (s, manifest [X])
            s_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            s_dps.map (_.f (x))     must_==  Vector (99)
            s_dps.map (_.position)  must_==  Vector (1)
        }

        "support concatenation with strings" in {
            checkStmt (s0_1 ++ "from A",      "select 1 from A")
            checkStmt (s0_2 ++ "1",           "set xxx 1")
            checkStmt (s0_vstring ++ "abc",   "set xxx ? abc",  Prov (JdbcString, "xxx", 1))

            checkArgs (s0_vstring, manifest [Domainless])
            s0_vstring.domainPlaceholders  must_==  Vector ()
        }

        "support concatenation with provided values" in {
            checkStmt (s0_vint,     "select 1 ?",  Prov (JdbcInt, 4, 1))
            checkStmt (s0_vstring,  "set xxx ?",   Prov (JdbcString, "xxx", 1))

            s0_vint     must haveClass [Statement0 [_]]
            s0_vstring  must haveClass [Statement0 [_]]

            checkArgs (s0_vint, manifest [Domainless])
            s0_vint.domainPlaceholders  must_==  Vector ()
        }

        "support concatenation with provided values for domain orianted statements" in {
            val s = sd0_1 ++ (JdbcLong, 30L)

            val x = X (x = 5, y = "ox")
            val s_dps = s.domainPlaceholders
            checkStmt (s, "select 1 ? ?", Prov (JdbcLong, 30L, 2))
            checkArgs (s, manifest [X])
            s_dps.map (_.jdbcType)  must_==  Vector (JdbcInt)
            s_dps.map (_.f (x))     must_==  Vector (5)
            s_dps.map (_.position)  must_==  Vector (1)
        }

        "construct Statement1 objects" in {
            def check [T <: AbstractJdbcType [_], S <: Statement1 [_, T]]
                        (dm : Manifest [_], s : S, t : T, p : Int = 1) (
                        implicit sm : Manifest [S], tm : Manifest [T]) : Unit =
            {
                checkArgs (sm, dm, tm)

                (s.placeholder : Hdr [T])  must_==  Hdr (t, p)
            }

            val domainless = manifest [Domainless]

            check (domainless, s1_int,      JdbcInt)
            check (domainless, s1_string,   JdbcString)
            check (domainless, s1_blob,     JdbcBlob)
            check (domainless, s1_clob,     JdbcClob)
            check (domainless, s1_int_vstr, JdbcInt)
            check (domainless, s1_vint_str, JdbcString, 2)

            checkStmt (s1_int,      "select 1 ?")
            checkStmt (s1_string,   "select 1 ?")
            checkStmt (s1_blob,     "select 1 ?")
            checkStmt (s1_clob,     "set xxx ?")
            checkStmt (s1_int_vstr, "insert into x values ( ? , ? )", Prov (JdbcString, "x", 2))
            checkStmt (s1_vint_str, "insert into x values ( ? , ? )", Prov (JdbcInt, 10, 1))

            s1_int  must haveClass [Statement1 [_, _]]

            s1_int.domainPlaceholders       must_==  Vector ()
            s1_string.domainPlaceholders    must_==  Vector ()
            s1_blob.domainPlaceholders      must_==  Vector ()
            s1_clob.domainPlaceholders      must_==  Vector ()
            s1_int_vstr.domainPlaceholders  must_==  Vector ()
            s1_vint_str.domainPlaceholders  must_==  Vector ()

            // sd1
            val y = Y (a = "aa", b = 33)
            val sd1_dps = sd1.domainPlaceholders
            check (manifest [Y], sd1, JdbcRef, p = 3)
            checkStmt (sd1, "set xxx ? ? ?")
            checkArgs (sd1, manifest [Y], manifestOf (JdbcRef))
            sd1 must haveClass [Statement1 [_, _]]
            sd1_dps.map (_.jdbcType)  must_==  Vector (JdbcString, JdbcInt)
            sd1_dps.map (_.f (y))     must_==  Vector ("aa", 33)
            sd1_dps.map (_.position)  must_==  Vector (1, 2)
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
            checkArgs (s, manifest [Domainless], manifestOf (JdbcInt))
            s.domainPlaceholders  must_==  Vector ()
        }

        "support concatenation with provided values" in {
            val ls2 = "a" ++ JdbcBytes ++ (JdbcInt, 100)
            checkStmt (ls2, "a ? ?", Prov (JdbcInt, 100, 2))
            checkPlaceholder (ls2.placeholder, JdbcBytes, 1)
            ls2.domainPlaceholders  must_==  Vector ()

            val ls3 = "a" ++ (JdbcString, "xyz") ++ "x" ++ JdbcBytes ++ (JdbcInt, 50)
            checkStmt (ls3, "a ? x ? ?", Prov (JdbcString, "xyz", 1), Prov (JdbcInt, 50, 3))
            checkPlaceholder (ls3.placeholder, JdbcBytes, 2)
            ls3.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s2_int_blob, manifest [Domainless], manifestOf (JdbcInt), manifestOf (JdbcBlob))
            s2_int_blob.domainPlaceholders  must_==  Vector ()
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
            s3_int_blob_short_vstr.domainPlaceholders  must_==  Vector ()

            // - - -  - - -

            checkStmt (s3_int_vstr_str_ref,
                      "insert into x values ( ? , ? ) ? ? m2",
                      Prov (JdbcString, "x", 2))

            checkPlaceholder (s3_int_vstr_str_ref.placeholder1, JdbcInt, 1)
            checkPlaceholder (s3_int_vstr_str_ref.placeholder2, JdbcString, 3)
            checkPlaceholder (s3_int_vstr_str_ref.placeholder3, JdbcRef, 4)

            s3_int_vstr_str_ref  must haveClass [Statement3 [_, _, _, _]]
            checkArgs (s3_int_vstr_str_ref,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcString),
                       manifestOf (JdbcRef))
            s3_int_vstr_str_ref.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s4,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes))
            s4.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s5,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong))
            s5.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong),
                       manifestOf (JdbcInt))
            s.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcString))
            s.domainPlaceholders  must_==  Vector ()
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
            s.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcString),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBytes))
            s.domainPlaceholders  must_==  Vector ()
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
            checkArgs (s,
                       manifest [Domainless],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcString),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcInt))
            s.domainPlaceholders  must_==  Vector ()
        }
    }

    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================
    // ===============================================================================================

    "Statement10" should {
        "work with domain objects" in {
            val s = sd10
            val dps = s.domainPlaceholders

            checkStmt (s,
                       "select 1 ? ? test ? ? blah ? ? ? ? ? ? ? ? ? x ? ?",
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
            checkArgs (s,
                       manifest [X],
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBlob),
                       manifestOf (JdbcShort),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcLong),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcString),
                       manifestOf (JdbcInt),
                       manifestOf (JdbcBytes),
                       manifestOf (JdbcInt))

            val x = X (x = 91, y = "yyyy")
            dps.map (_.jdbcType)  must_==  Vector (JdbcString)
            dps.map (_.f (x))     must_==  Vector ("yyyy")
            dps.map (_.position)  must_==  Vector (15)
        }
    }
}
