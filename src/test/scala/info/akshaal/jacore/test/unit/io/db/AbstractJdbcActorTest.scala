/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db

import java.sql.{Connection, ResultSet}
import scala.runtime.{RichInt, RichLong, RichBoolean}
import java.util.Date
import java.lang.{Object => JavaObject}
import java.math.BigDecimal

import unit.UnitTestHelper._
import io.db.AbstractJdbcActor
import io.db.jdbctype._
import io.db.jdbcaction._

class AbstractJdbcActorTest extends JacoreSpecWithJUnit ("AbstractJdbcActor specification") {
    import AbstractJdbcActorTest._

    // Empty deriviations of standard types
    class MyDate extends Date
    class MyObject extends JavaObject
    class MyDecimal extends BigDecimal (0)

    // JdbcActor without connection (null)
    class MockedJdbc extends AbstractJdbcActor (
        lowPriorityActorEnv = TestModule.lowPriorityActorEnv)
    {
        override protected def getConnection () : Connection = null
    }

    "AbstractJdbcActor" should {
        // =================================================================================
        // =================================================================================
        // =================================================================================
        "must have correct variance for PreparedStatements" in {
            // - - -- - -- --- - - - 0 arguments

            new MockedJdbc {
                val command1 : PreparedStatement0 [Boolean] = prepare (JdbcCommand ("..."))
                val command2 : PreparedStatement0 [AnyVal] = command1

                val query1 : PreparedStatement0 [ResultSet] = prepare (JdbcQuery ("..."))
                val query2 : PreparedStatement0 [Object] = query1

                val update1 : PreparedStatement0 [Int] = prepare (JdbcUpdate ("..."))
                val update2 : PreparedStatement0 [AnyVal] = update1
            }

            // - - -- - -- --- - - - 1 argument

            new MockedJdbc {
                // command
                val command1 : PreparedStatement1 [Date, Boolean] =
                            prepare (JdbcCommand ("?"), JdbcDate)
                
                val command2 : PreparedStatement1 [MyDate, AnyVal] = command1

                // query
                val query1 : PreparedStatement1 [JavaObject, ResultSet] =
                            prepare (JdbcQuery ("?"), JdbcObject)

                val query2 : PreparedStatement1 [MyObject, Object] = query1

                // update
                val update1 : PreparedStatement1 [BigDecimal, Int] =
                            prepare (JdbcUpdate ("?"), JdbcBigDecimal)
                        
                val update2 : PreparedStatement1 [MyDecimal, AnyVal] = update1
            }

            // - - -- - -- --- - - - 2 arguments
            
            new MockedJdbc {
                // command
                val command1 : PreparedStatement2 [Date, BigDecimal, Boolean] =
                            prepare (JdbcCommand ("? ?"), JdbcDate, JdbcBigDecimal)

                val command2 : PreparedStatement2 [MyDate, MyDecimal, Any] = command1

                // query
                val query1 : PreparedStatement2 [JavaObject, Date, ResultSet] =
                            prepare (JdbcQuery ("? ?"), JdbcObject, JdbcDate)

                val query2 : PreparedStatement2 [MyObject, MyDate, Object] = query1

                // update
                val update1 : PreparedStatement2 [Date, BigDecimal, Int] =
                            prepare (JdbcUpdate ("? ?"), JdbcDate, JdbcBigDecimal)

                val update2 : PreparedStatement2 [MyDate, MyDecimal, AnyVal] = update1
            }

            // - - -- - -- --- - - - 3 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement3 [Date, JavaObject, BigDecimal, Boolean] =
                            prepare (JdbcCommand ("? ? ?"), JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedStatement3 [MyDate, MyObject, MyDecimal, Any] = command1

                // query
                val query1 : PreparedStatement3 [JavaObject, JavaObject, Date, ResultSet] =
                            prepare (JdbcQuery ("? ? ?"), JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedStatement3 [MyObject, MyObject, MyDate, Object] = query1

                // update
                val update1 : PreparedStatement3 [Date, BigDecimal, Date, Int] =
                            prepare (JdbcUpdate ("? ? ?"), JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedStatement3 [MyDate, MyDecimal, MyDate, AnyVal] = update1
            }

            // - - -- - -- --- - - - 3 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement3 [Date, JavaObject, BigDecimal, Boolean] =
                            prepare (JdbcCommand ("? ? ?"), JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedStatement3 [MyDate, MyObject, MyDecimal, Any] = command1

                // query
                val query1 : PreparedStatement3 [JavaObject, JavaObject, Date, ResultSet] =
                            prepare (JdbcQuery ("? ? ?"), JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedStatement3 [MyObject, MyObject, MyDate, Object] = query1

                // update
                val update1 : PreparedStatement3 [Date, BigDecimal, Date, Int] =
                            prepare (JdbcUpdate ("? ? ?"), JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedStatement3 [MyDate, MyDecimal, MyDate, AnyVal] = update1
            }

            // - - -- - -- --- - - - 4 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement4 [Date, Date, JavaObject, BigDecimal, Boolean] =
                            prepare (JdbcCommand ("? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedStatement4 [MyDate, MyDate, MyObject,
                                                   MyDecimal, Any] = command1

                // query
                val query1 : PreparedStatement4 [Date, JavaObject, JavaObject, Date, ResultSet] =
                            prepare (JdbcQuery ("? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedStatement4 [MyDate, MyObject, MyObject,
                                                 MyDate, Object] = query1

                // update
                val update1 : PreparedStatement4 [JavaObject, Date, BigDecimal, Date, Int] =
                            prepare (JdbcUpdate ("? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedStatement4 [MyObject, MyDate, MyDecimal,
                                                  MyDate, AnyVal] = update1
            }

            // - - -- - -- --- - - - 5 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement5 [Date, Date, JavaObject,
                                                   BigDecimal, Date, Boolean] =
                            prepare (JdbcCommand ("? ? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate)

                val command2 : PreparedStatement5 [MyDate, MyDate, MyObject,
                                                   MyDecimal, MyDate, Any] = command1

                // query
                val query1 : PreparedStatement5 [Date, JavaObject, JavaObject, Date, JavaObject,
                                                 ResultSet] =
                            prepare (JdbcQuery ("? ? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject)

                val query2 : PreparedStatement5 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, Object] = query1

                // update
                val update1 : PreparedStatement5 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Int] =
                            prepare (JdbcUpdate ("? ? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate, JdbcBigDecimal)

                val update2 : PreparedStatement5 [MyObject, MyDate, MyDecimal,
                                                  MyDate, MyDecimal, AnyVal] = update1
            }

            // - - -- - -- --- - - - 6 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement6 [Date, Date, JavaObject,
                                                   BigDecimal, Date, BigDecimal, Boolean] =
                            prepare (JdbcCommand ("? ? ? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal)

                val command2 : PreparedStatement6 [MyDate, MyDate, MyObject,
                                                   MyDecimal, MyDate, MyDecimal, Any] = command1

                // query
                val query1 : PreparedStatement6 [Date, JavaObject, JavaObject, Date, JavaObject,
                                                 Date, ResultSet] =
                            prepare (JdbcQuery ("? ? ? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate)

                val query2 : PreparedStatement6 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, MyDate, Object] = query1

                // update
                val update1 : PreparedStatement6 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Date, Int] =
                            prepare (JdbcUpdate ("? ? ? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate)

                val update2 : PreparedStatement6 [MyObject, MyDate, MyDecimal,
                                                  MyDate, MyDecimal, MyDate, AnyVal] = update1
            }

            // - - -- - -- --- - - - 7 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement7 [JavaObject, Date, Date, JavaObject,
                                                   BigDecimal, Date, BigDecimal, Boolean] =
                            prepare (JdbcCommand ("? ? ?  ? ? ?  ?"),
                                     JdbcObject, JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal,
                                     JdbcDate, JdbcBigDecimal)

                val command2 : PreparedStatement7 [MyObject, MyDate, MyDate, MyObject,
                                                   MyDecimal, MyDate, MyDecimal, Any] = command1

                // query
                val query1 : PreparedStatement7 [Date, JavaObject, JavaObject, Date, JavaObject,
                                                 Date, JavaObject, ResultSet] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject)

                val query2 : PreparedStatement7 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, MyDate,
                                                 MyObject, Object] = query1

                // update
                val update1 : PreparedStatement7 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Date, JavaObject, Int] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject)

                val update2 : PreparedStatement7 [MyObject, MyDate, MyDecimal,
                                                  MyDate, MyDecimal, MyDate,
                                                  MyObject, AnyVal] = update1
            }

            // - - -- - -- --- - - - 8 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement8 [JavaObject, Date, Date, JavaObject,
                                                   BigDecimal, Date, BigDecimal,
                                                   JavaObject, Boolean] =
                            prepare (JdbcCommand ("? ? ?  ? ? ?  ? ?"),
                                     JdbcObject, JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal,
                                     JdbcDate, JdbcBigDecimal, JdbcObject)

                val command2 : PreparedStatement8 [MyObject, MyDate, MyDate, MyObject,
                                                   MyDecimal, MyDate, MyDecimal,
                                                   MyObject, Any] = command1

                // query
                val query1 : PreparedStatement8 [Date, JavaObject, JavaObject, Date, JavaObject,
                                                 Date, JavaObject, BigDecimal, ResultSet] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject, JdbcBigDecimal)

                val query2 : PreparedStatement8 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, MyDate,
                                                 MyObject, MyDecimal, Object] = query1

                // update
                val update1 : PreparedStatement8 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Date, JavaObject, Date, Int] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject, JdbcDate)

                val update2 : PreparedStatement8 [MyObject, MyDate, MyDecimal,
                                                  MyDate, MyDecimal, MyDate,
                                                  MyObject, MyDate, AnyVal] = update1
            }

            // - - -- - -- --- - - - 9 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedStatement9 [JavaObject, Date, Date, JavaObject,
                                                   BigDecimal, Date, BigDecimal,
                                                   JavaObject, Date, Boolean] =
                            prepare (JdbcCommand ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal,
                                     JdbcDate, JdbcBigDecimal, JdbcObject, JdbcDate)

                val command2 : PreparedStatement9 [MyObject, MyDate, MyDate, MyObject,
                                                   MyDecimal, MyDate, MyDecimal,
                                                   MyObject, MyDate, Any] = command1

                // query
                val query1 : PreparedStatement9 [Date, JavaObject, JavaObject, Date, JavaObject,
                                                 Date, JavaObject, BigDecimal, Date, ResultSet] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate)

                val query2 : PreparedStatement9 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, MyDate,
                                                 MyObject, MyDecimal, MyDate, Object] = query1

                // update
                val update1 : PreparedStatement9 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Date, JavaObject, Date, JavaObject, Int] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject, JdbcDate, JdbcObject)

                val update2 : PreparedStatement9 [MyObject, MyDate, MyDecimal,
                                                  MyDate, MyDecimal, MyDate,
                                                  MyObject, MyDate, MyObject, AnyVal] = update1
            }

            // This test is just compilation-time test. So we have to give something
            // useful here to indicate to the testing system that this test is passed.
            true must_== true
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "must validate prepared statements parameter count" in {
            def illegal (actor : => MockedJdbc) : Unit = {
                actor must throwA (new IllegalArgumentException).like {
                    case exc : Exception => exc.getMessage.contains ("expected to have")
                }
            }

            // = = = = = = = = = = = Tests

            // 0 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("select * from x where y=?"))
                })

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("select * from x where y=?", validate = false))
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("select * from x"))
            }

            // 1 parameter  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("select * from x where y between (?, ?)"), JdbcShort)
                })

            new MockedJdbc {
                val prep = prepare (JdbcUpdate ("select * from x where y between (?, ?)",
                                                validate = false),
                                    JdbcShort)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("select * from x where y=?"), JdbcShort)
            }

            // 2 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("select * from x where z=?"), JdbcShort, JdbcBoolean)
                })

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("select * from x where z=?",
                                               validate = false),
                                    JdbcShort, JdbcBoolean)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("insert into z values (?, ?)"), JdbcShort, JdbcBoolean)
            }

            // 3 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("select * from x where z=? ? ? ? ? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob)
                })

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("select * from x where z=?",
                                               validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("insert into z values (?, ?, ?)"),
                                    JdbcShort, JdbcBoolean, JdbcClob)
            }

            // 4 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcUpdate ("? ? ? ? ? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort)
                })

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ? ? ?",
                                               validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ? ?"),
                                    JdbcShort, JdbcBoolean, JdbcClob, JdbcShort)
            }

            // 5 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("? ? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                        JdbcBoolean)
                })

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ? ? ? ?",
                                               validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                    JdbcBoolean)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ? ? ?"),
                                    JdbcShort, JdbcBoolean, JdbcClob, JdbcShort,
                                    JdbcBoolean)
            }

            // 6 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                        JdbcBoolean, JdbcClob)
                })

            new MockedJdbc {
                val prep = prepare (JdbcUpdate ("? ?",
                                                validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                    JdbcBoolean, JdbcClob)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ? ? ? ?"),
                                    JdbcShort, JdbcBoolean, JdbcClob, JdbcShort,
                                    JdbcBoolean, JdbcClob)
            }

            // 7 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcQuery ("? ? ? ? ? ? ? ? ? ? ? ? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                        JdbcBoolean, JdbcClob, JdbcInt)
                })

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ?",
                                               validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                    JdbcBoolean, JdbcClob, JdbcLong)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ?  ? ? ?  ?"),
                                    JdbcShort, JdbcBoolean, JdbcClob, JdbcShort,
                                    JdbcBoolean, JdbcClob, JdbcBigDecimal)
            }

            // 8 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcUpdate ("? ? ? ? ? ? ? ? ? ? ? ? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                        JdbcBoolean, JdbcClob, JdbcInt, JdbcLong)
                })

            new MockedJdbc {
                val prep = prepare (JdbcUpdate ("? ? ?  ? ? ?  ?",
                                                validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                    JdbcBoolean, JdbcClob, JdbcLong, JdbcInt)
            }

            new MockedJdbc {
                val prep = prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ?"),
                                    JdbcShort, JdbcBoolean, JdbcClob, JdbcShort,
                                    JdbcBoolean, JdbcClob, JdbcBigDecimal, JdbcInt)
            }

            // 9 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcCommand ("? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?"),
                                        JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                        JdbcBoolean, JdbcClob, JdbcInt, JdbcLong, JdbcInt)
                })

            new MockedJdbc {
                val prep = prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ?",
                                                validate = false),
                                    JdbcShort, JdbcBoolean, JdbcBlob, JdbcShort,
                                    JdbcBoolean, JdbcClob, JdbcLong, JdbcInt, JdbcInt)
            }

            new MockedJdbc {
                val prep = prepare (JdbcQuery ("? ? ?  ? ? ?  ? ? ?"),
                                    JdbcShort, JdbcBoolean, JdbcClob, JdbcShort,
                                    JdbcBoolean, JdbcClob, JdbcBigDecimal, JdbcInt, JdbcInt)
            }
        }
    }
}

object AbstractJdbcActorTest {
}
