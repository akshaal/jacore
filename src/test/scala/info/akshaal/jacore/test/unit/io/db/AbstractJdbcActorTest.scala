/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db

import java.sql.{Connection, ResultSet, PreparedStatement}
import java.util.Date
import java.lang.{Object => JavaObject}
import java.math.BigDecimal

import org.specs.mock.Mockito
import scala.runtime.{RichInt, RichLong, RichBoolean}

import unit.UnitTestHelper._
import io.db.AbstractJdbcActor
import io.db.jdbctype._
import io.db.jdbcaction._

class AbstractJdbcActorTest extends JacoreSpecWithJUnit ("AbstractJdbcActor specification")
                               with Mockito
{
    import AbstractJdbcActorTest._

    // Empty deriviations of standard types
    class MyDate extends Date
    class MyObject extends JavaObject
    class MyDecimal extends BigDecimal (0)

    // Abbreviations
    type JC = JdbcCommand
    type JQ = JdbcQuery
    type JU = JdbcUpdate

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
        "must have correct variance for PreparedActions" in {
            // - - -- - -- --- - - - 0 arguments

            new MockedJdbc {
                val command1 : PreparedAction0 [JC] = prepare (JdbcCommand ("..."))
                val command2 : PreparedAction0 [JdbcAction] = command1

                val query1 : PreparedAction0 [JQ] = prepare (JdbcQuery ("..."))
                val query2 : PreparedAction0 [JdbcAction] = query1

                val update1 : PreparedAction0 [JU] = prepare (JdbcUpdate ("..."))
                val update2 : PreparedAction0 [JdbcAction] = update1
            }

            // - - -- - -- --- - - - 1 argument

            new MockedJdbc {
                // command
                val command1 : PreparedAction1 [Date, JC] = prepare (JdbcCommand ("?"), JdbcDate)
                val command2 : PreparedAction1 [MyDate, JdbcAction] = command1

                // query
                val query1 : PreparedAction1 [JavaObject, JQ] =
                            prepare (JdbcQuery ("?"), JdbcObject)

                val query2 : PreparedAction1 [MyObject, JdbcAction] = query1

                // update
                val update1 : PreparedAction1 [BigDecimal, JU] =
                            prepare (JdbcUpdate ("?"), JdbcBigDecimal)
                        
                val update2 : PreparedAction1 [MyDecimal, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 2 arguments
            
            new MockedJdbc {
                // command
                val command1 : PreparedAction2 [Date, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ?"), JdbcDate, JdbcBigDecimal)

                val command2 : PreparedAction2 [MyDate, MyDecimal, JdbcAction] = command1

                // query
                val query1 : PreparedAction2 [JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ?"), JdbcObject, JdbcDate)

                val query2 : PreparedAction2 [MyObject, MyDate, JdbcAction] = query1

                // update
                val update1 : PreparedAction2 [Date, BigDecimal, JU] =
                            prepare (JdbcUpdate ("? ?"), JdbcDate, JdbcBigDecimal)

                val update2 : PreparedAction2 [MyDate, MyDecimal, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 3 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction3 [Date, JavaObject, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ?"), JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedAction3 [MyDate, MyObject, MyDecimal, JdbcAction] =
                            command1

                // query
                val query1 : PreparedAction3 [JavaObject, JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ? ?"), JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedAction3 [MyObject, MyObject, MyDate, JdbcAction] = query1

                // update
                val update1 : PreparedAction3 [Date, BigDecimal, Date, JU] =
                            prepare (JdbcUpdate ("? ? ?"), JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction3 [MyDate, MyDecimal, MyDate, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 3 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction3 [Date, JavaObject, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ?"), JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedAction3 [MyDate, MyObject, MyDecimal, JdbcAction] =
                            command1

                // query
                val query1 : PreparedAction3 [JavaObject, JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ? ?"), JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedAction3 [MyObject, MyObject, MyDate, JdbcAction] = query1

                // update
                val update1 : PreparedAction3 [Date, BigDecimal, Date, JU] =
                            prepare (JdbcUpdate ("? ? ?"), JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction3 [MyDate, MyDecimal, MyDate, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 4 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction4 [Date, Date, JavaObject, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedAction4 [MyDate, MyDate, MyObject,
                                                MyDecimal, JdbcAction] = command1

                // query
                val query1 : PreparedAction4 [Date, JavaObject, JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedAction4 [MyDate, MyObject, MyObject,
                                              MyDate, JdbcAction] = query1

                // update
                val update1 : PreparedAction4 [JavaObject, Date, BigDecimal, Date, JU] =
                            prepare (JdbcUpdate ("? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction4 [MyObject, MyDate, MyDecimal,
                                               MyDate, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 5 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction5 [Date, Date, JavaObject,
                                                BigDecimal, Date, JC] =
                            prepare (JdbcCommand ("? ? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate)

                val command2 : PreparedAction5 [MyDate, MyDate, MyObject,
                                                MyDecimal, MyDate, JdbcAction] = command1

                // query
                val query1 : PreparedAction5 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              JQ] =
                            prepare (JdbcQuery ("? ? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject)

                val query2 : PreparedAction5 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, JdbcAction] = query1

                // update
                val update1 : PreparedAction5 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               JU] =
                            prepare (JdbcUpdate ("? ? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate, JdbcBigDecimal)

                val update2 : PreparedAction5 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 6 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction6 [Date, Date, JavaObject,
                                                BigDecimal, Date, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal)

                val command2 : PreparedAction6 [MyDate, MyDate, MyObject, MyDecimal, MyDate,
                                                MyDecimal, JdbcAction] = command1

                // query
                val query1 : PreparedAction6 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JQ] =
                            prepare (JdbcQuery ("? ? ? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate)

                val query2 : PreparedAction6 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, MyDate, JdbcAction] = query1

                // update
                val update1 : PreparedAction6 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               Date, JU] =
                            prepare (JdbcUpdate ("? ? ? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction6 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 7 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction7 [JavaObject, Date, Date, JavaObject,
                                                BigDecimal, Date, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ?  ? ? ?  ?"),
                                     JdbcObject, JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal,
                                     JdbcDate, JdbcBigDecimal)

                val command2 : PreparedAction7 [MyObject, MyDate, MyDate, MyObject,
                                                MyDecimal, MyDate, MyDecimal,
                                                JdbcAction] = command1

                // query
                val query1 : PreparedAction7 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JavaObject, JQ] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject)

                val query2 : PreparedAction7 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, MyDate,
                                              MyObject, JdbcAction] = query1

                // update
                val update1 : PreparedAction7 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               Date, JavaObject, JU] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject)

                val update2 : PreparedAction7 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate,
                                               MyObject, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 8 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction8 [JavaObject, Date, Date, JavaObject,
                                                BigDecimal, Date, BigDecimal,
                                                JavaObject, JC] =
                            prepare (JdbcCommand ("? ? ?  ? ? ?  ? ?"),
                                     JdbcObject, JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal,
                                     JdbcDate, JdbcBigDecimal, JdbcObject)

                val command2 : PreparedAction8 [MyObject, MyDate, MyDate, MyObject,
                                                   MyDecimal, MyDate, MyDecimal,
                                                   MyObject, JdbcAction] = command1

                // query
                val query1 : PreparedAction8 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JavaObject, BigDecimal, JQ] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject, JdbcBigDecimal)

                val query2 : PreparedAction8 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, MyDate,
                                                 MyObject, MyDecimal, JdbcAction] = query1

                // update
                val update1 : PreparedAction8 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Date, JavaObject, Date, JU] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject, JdbcDate)

                val update2 : PreparedAction8 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate,
                                               MyObject, MyDate, JdbcAction] = update1
            }

            // - - -- - -- --- - - - 9 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction9 [JavaObject, Date, Date, JavaObject,
                                                BigDecimal, Date, BigDecimal,
                                                JavaObject, Date, JC] =
                            prepare (JdbcCommand ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal,
                                     JdbcDate, JdbcBigDecimal, JdbcObject, JdbcDate)

                val command2 : PreparedAction9 [MyObject, MyDate, MyDate, MyObject,
                                                MyDecimal, MyDate, MyDecimal,
                                                MyObject, MyDate, JdbcAction] = command1

                // query
                val query1 : PreparedAction9 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JavaObject, BigDecimal, Date, JQ] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate)

                val query2 : PreparedAction9 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, MyDate,
                                              MyObject, MyDecimal, MyDate,
                                              JdbcAction] = query1

                // update
                val update1 : PreparedAction9 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               Date, JavaObject, Date, JavaObject, JU] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject, JdbcDate, JdbcObject)

                val update2 : PreparedAction9 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate,
                                               MyObject, MyDate, MyObject, JdbcAction] = update1
            }

            // This test is just compilation-time test. So we have to give something
            // useful here to indicate to the testing system that this test is passed.
            true must_== true
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "must validate prepared actions parameter count" in {
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

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "allow access to underlying JDBC PreparedStatement" in {
            val mockedPS1 = mock [PreparedStatement]
            val mockedPS2 = mock [PreparedStatement]
            val mockedConn = mock [Connection]

            mockedConn.prepareStatement ("?") returns mockedPS1 thenReturns mockedPS2

            new AbstractJdbcActor (lowPriorityActorEnv = TestModule.lowPriorityActorEnv) {
                override protected def getConnection () : Connection = mockedConn

                val prep1 = prepare (JdbcUpdate ("?"), JdbcBoolean)
                val prep2 = prepare (JdbcUpdate ("?"), JdbcBoolean)

                // Should not create new prepared statement unless asked explicitly to do so
                prep1.getPreparedStatementIfAny  must_==  None
                prep2.getPreparedStatementIfAny  must_==  None
                prep1.getPreparedStatementIfAny  must_==  None
                prep2.getPreparedStatementIfAny  must_==  None

                // Create and get new prepared statement
                prep1.getPreparedStatement  must_==  mockedPS1
                prep2.getPreparedStatement  must_==  mockedPS2
                prep1.getPreparedStatement  must_==  mockedPS1
                prep2.getPreparedStatement  must_==  mockedPS2

                prep1.getPreparedStatementIfAny  must_==  Some (mockedPS1)
                prep2.getPreparedStatementIfAny  must_==  Some (mockedPS2)
                prep1.getPreparedStatementIfAny  must_==  Some (mockedPS1)
                prep2.getPreparedStatementIfAny  must_==  Some (mockedPS2)

            }
        }
    }
}

object AbstractJdbcActorTest {
}
