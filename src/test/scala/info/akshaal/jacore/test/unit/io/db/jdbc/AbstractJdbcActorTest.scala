/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import java.sql.{Connection, ResultSet, PreparedStatement}
import java.util.Date
import java.lang.{Object => JavaObject}
import java.math.BigDecimal

import org.specs.mock.Mockito
import scala.runtime.{RichInt, RichLong, RichBoolean}

import unit.UnitTestHelper._
import io.db.jdbc.AbstractJdbcActor
import io.db.jdbc.`type`._
import io.db.jdbc.action._
import io.db.jdbc.statement._

class AbstractJdbcActorTest extends JacoreSpecWithJUnit ("AbstractJdbcActor specification")
                               with Mockito
{
    // Empty deriviations of standard types
    class MyDate extends Date
    class MyObject extends JavaObject
    class MyDecimal extends BigDecimal (0)

    // Abbreviations
    type JC = JdbcCommand
    type JQ = JdbcQuery
    type JU = JdbcUpdate
    type JB = JdbcBatch

    // JdbcActor without connection (null)
    class MockedJdbc extends AbstractJdbcActor (lowPriorityActorEnv = TestModule.lowPriorityActorEnv) {
        override protected def getConnection () : Connection = null
    }

    // Just tells that we should reach this invokation, otherwise test is going to be ignored
    def mustPass () {
        true must_== true
    }

    "AbstractJdbcActor" should {
        // =================================================================================
        // =================================================================================
        // =================================================================================

        "must have correct variance for domainless PreparedAction0" in {
            new MockedJdbc {
                val command1 : PreparedAction0 [JC] = prepare (JdbcCommand, "")
                val command2 : PreparedAction0 [AbstractJdbcAction] = command1

                val query1 : PreparedAction0 [JQ] = prepare (JdbcQuery, "")
                val query2 : PreparedAction0 [AbstractJdbcAction] = query1

                val update1 : PreparedAction0 [JU] = prepare (JdbcUpdate, "")
                val update2 : PreparedAction0 [AbstractJdbcAction] = update1

                val batch1 : PreparedAction0 [JB] = prepare (JdbcBatch, "")
                val batch2 : PreparedAction0 [AbstractJdbcAction] = batch1
            }

            mustPass ()
        }
    }

/*
        "must have correct variance for PreparedActions" in {
            // - - -- - -- --- - - - 0 arguments


            // - - -- - -- --- - - - 1 argument

            new MockedJdbc {
                // command
                val command1 : PreparedAction1 [Date, JC] = prepare (JdbcCommand ("?"), JdbcDate)
                val command2 : PreparedAction1 [MyDate, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction1 [JavaObject, JQ] =
                            prepare (JdbcQuery ("?"), JdbcObject)

                val query2 : PreparedAction1 [MyObject, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction1 [BigDecimal, JU] =
                            prepare (JdbcUpdate ("?"), JdbcBigDecimal)

                val update2 : PreparedAction1 [MyDecimal, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction1 [JavaObject, JB] =
                            prepare (JdbcBatch ("?"), JdbcObject)

                val batch2 : PreparedAction1 [MyObject, AbstractJdbcAction] = batch1
            }

            // - - -- - -- --- - - - 2 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction2 [Date, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ?"), JdbcDate, JdbcBigDecimal)

                val command2 : PreparedAction2 [MyDate, MyDecimal, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction2 [JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ?"), JdbcObject, JdbcDate)

                val query2 : PreparedAction2 [MyObject, MyDate, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction2 [Date, BigDecimal, JU] =
                            prepare (JdbcUpdate ("? ?"), JdbcDate, JdbcBigDecimal)

                val update2 : PreparedAction2 [MyDate, MyDecimal, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction2 [JavaObject, BigDecimal, JB] =
                            prepare (JdbcBatch ("? ?"), JdbcObject, JdbcBigDecimal)

                val batch2 : PreparedAction2 [MyObject, MyDecimal, AbstractJdbcAction] = batch1
            }

            // - - -- - -- --- - - - 3 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction3 [Date, JavaObject, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ?"), JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedAction3 [MyDate, MyObject, MyDecimal, AbstractJdbcAction] =
                            command1

                // query
                val query1 : PreparedAction3 [JavaObject, JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ? ?"), JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedAction3 [MyObject, MyObject, MyDate, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction3 [Date, BigDecimal, Date, JU] =
                            prepare (JdbcUpdate ("? ? ?"), JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction3 [MyDate, MyDecimal, MyDate, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction3 [JavaObject, BigDecimal, Int, JB] =
                            prepare (JdbcBatch ("? ? ?"), JdbcObject, JdbcBigDecimal, JdbcInt)

                val batch2 : PreparedAction3 [MyObject, MyDecimal, Int, AbstractJdbcAction] = batch1
            }

            // - - -- - -- --- - - - 4 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction4 [Date, Date, JavaObject, BigDecimal, JC] =
                            prepare (JdbcCommand ("? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal)

                val command2 : PreparedAction4 [MyDate, MyDate, MyObject,
                                                MyDecimal, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction4 [Date, JavaObject, JavaObject, Date, JQ] =
                            prepare (JdbcQuery ("? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate)

                val query2 : PreparedAction4 [MyDate, MyObject, MyObject,
                                              MyDate, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction4 [JavaObject, Date, BigDecimal, Date, JU] =
                            prepare (JdbcUpdate ("? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction4 [MyObject, MyDate, MyDecimal,
                                               MyDate, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction4 [JavaObject, BigDecimal, Int, Date, JB] =
                            prepare (JdbcBatch ("? ? ? ?"), JdbcObject, JdbcBigDecimal, JdbcInt,
                                     JdbcDate)

                val batch2 : PreparedAction4 [MyObject, MyDecimal, Int, MyDate,
                                              AbstractJdbcAction] = batch1
            }

            // - - -- - -- --- - - - 5 arguments

            new MockedJdbc {
                // command
                val command1 : PreparedAction5 [Date, Date, JavaObject,
                                                BigDecimal, Date, JC] =
                            prepare (JdbcCommand ("? ? ? ? ?"),
                                     JdbcDate, JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate)

                val command2 : PreparedAction5 [MyDate, MyDate, MyObject,
                                                MyDecimal, MyDate, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction5 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              JQ] =
                            prepare (JdbcQuery ("? ? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject)

                val query2 : PreparedAction5 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction5 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               JU] =
                            prepare (JdbcUpdate ("? ? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate, JdbcBigDecimal)

                val update2 : PreparedAction5 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction5 [JavaObject, BigDecimal, Int, Date, Date, JB] =
                            prepare (JdbcBatch ("? ? ? ? ?"), JdbcObject, JdbcBigDecimal, JdbcInt,
                                     JdbcDate, JdbcDate)

                val batch2 : PreparedAction5 [MyObject, MyDecimal, Int, MyDate, MyDate,
                                              AbstractJdbcAction] = batch1
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
                                                MyDecimal, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction6 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JQ] =
                            prepare (JdbcQuery ("? ? ? ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate)

                val query2 : PreparedAction6 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, MyDate, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction6 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               Date, JU] =
                            prepare (JdbcUpdate ("? ? ? ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate)

                val update2 : PreparedAction6 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction6 [JavaObject, BigDecimal, Int, Date, Date, JavaObject,
                                              JB] =
                            prepare (JdbcBatch ("? ? ? ? ? ?"), JdbcObject, JdbcBigDecimal, JdbcInt,
                                     JdbcDate, JdbcObject, JdbcObject)

                val batch2 : PreparedAction6 [MyObject, MyDecimal, Int, MyDate, MyDate,
                                              MyObject, AbstractJdbcAction] = batch1
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
                                                AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction7 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JavaObject, JQ] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject)

                val query2 : PreparedAction7 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, MyDate,
                                              MyObject, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction7 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               Date, JavaObject, JU] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject)

                val update2 : PreparedAction7 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate,
                                               MyObject, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction7 [JavaObject, BigDecimal, Int, Date, Date, JavaObject,
                                              Date, JB] =
                            prepare (JdbcBatch ("? ? ?  ? ? ?  ?"), JdbcObject, JdbcBigDecimal,
                                     JdbcInt, JdbcDate, JdbcObject, JdbcObject, JdbcDate)

                val batch2 : PreparedAction7 [MyObject, MyDecimal, Int, MyDate, MyDate,
                                              MyObject, MyDate, AbstractJdbcAction] = batch1
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
                                                   MyObject, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction8 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JavaObject, BigDecimal, JQ] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject, JdbcBigDecimal)

                val query2 : PreparedAction8 [MyDate, MyObject, MyObject,
                                                 MyDate, MyObject, MyDate,
                                                 MyObject, MyDecimal, AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction8 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                                  Date, JavaObject, Date, JU] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject, JdbcDate)

                val update2 : PreparedAction8 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate,
                                               MyObject, MyDate, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction8 [JavaObject, BigDecimal, Int, Date, Date, JavaObject,
                                              Date, BigDecimal, JB] =
                            prepare (JdbcBatch ("? ? ?  ? ? ?  ? ?"), JdbcObject, JdbcBigDecimal,
                                     JdbcInt, JdbcDate, JdbcObject, JdbcObject, JdbcDate,
                                     JdbcBigDecimal)

                val batch2 : PreparedAction8 [MyObject, MyDecimal, Int, MyDate, MyDate,
                                              MyObject, MyDate, MyDecimal, AbstractJdbcAction] = batch1
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
                                                MyObject, MyDate, AbstractJdbcAction] = command1

                // query
                val query1 : PreparedAction9 [Date, JavaObject, JavaObject, Date, JavaObject,
                                              Date, JavaObject, BigDecimal, Date, JQ] =
                            prepare (JdbcQuery ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcDate, JdbcObject, JdbcObject, JdbcDate, JdbcObject,
                                     JdbcDate, JdbcObject, JdbcBigDecimal, JdbcDate)

                val query2 : PreparedAction9 [MyDate, MyObject, MyObject,
                                              MyDate, MyObject, MyDate,
                                              MyObject, MyDecimal, MyDate,
                                              AbstractJdbcAction] = query1

                // update
                val update1 : PreparedAction9 [JavaObject, Date, BigDecimal, Date, BigDecimal,
                                               Date, JavaObject, Date, JavaObject, JU] =
                            prepare (JdbcUpdate ("? ? ?  ? ? ?  ? ? ?"),
                                     JdbcObject, JdbcDate, JdbcBigDecimal, JdbcDate,
                                     JdbcBigDecimal, JdbcDate, JdbcObject, JdbcDate, JdbcObject)

                val update2 : PreparedAction9 [MyObject, MyDate, MyDecimal,
                                               MyDate, MyDecimal, MyDate,
                                               MyObject, MyDate, MyObject, AbstractJdbcAction] = update1

                // batch
                val batch1 : PreparedAction9 [JavaObject, BigDecimal, Int, Date, Date, JavaObject,
                                              Date, BigDecimal, Date, JB] =
                            prepare (JdbcBatch ("? ? ?  ? ? ?  ? ? ?"), JdbcObject, JdbcBigDecimal,
                                     JdbcInt, JdbcDate, JdbcObject, JdbcObject, JdbcDate,
                                     JdbcBigDecimal, JdbcDate)

                val batch2 : PreparedAction9 [MyObject, MyDecimal, Int, MyDate, MyDate,
                                              MyObject, MyDate, MyDecimal, MyDate,
                                              AbstractJdbcAction] = batch1
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
                val prep = prepare (JdbcBatch ("select * from x where y=?", validate = false))
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
                val prep = prepare (JdbcBatch ("select * from x where y=?"), JdbcShort)
            }

            // 2 parameters  - - - - - - - - -
            illegal (new MockedJdbc {
                    val prep = prepare (JdbcBatch ("select * from x where z=?"), JdbcShort, JdbcBoolean)
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
                val prep = prepare (JdbcBatch ("? ? ? ?"),
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
                val prep = prepare (JdbcCommand ("? ? ? ? ? ?"),
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
                val prep = prepare (JdbcBatch ("? ?",
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
            val mockedPS3 = mock [PreparedStatement]
            val mockedConn = mock [Connection]

            mockedPS1  must_!=  mockedPS2
            mockedPS1  must_!=  mockedPS3
            mockedPS2  must_!=  mockedPS3

            mockedConn.prepareStatement ("?") returns mockedPS1 thenReturns mockedPS2 thenReturns mockedPS3

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
    */
}
