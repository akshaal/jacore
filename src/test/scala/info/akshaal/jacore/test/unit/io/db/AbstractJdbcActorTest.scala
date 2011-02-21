/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db

import java.sql.Connection

import unit.UnitTestHelper._
import io.db.AbstractJdbcActor
import io.db.jdbctype._
import io.db.jdbcaction._

class AbstractJdbcActorTest extends JacoreSpecWithJUnit ("AbstractJdbcActor specification") {
    import AbstractJdbcActorTest._

    "AbstractJdbcActor" should {
        "must validate prepared statements parameter count" in {
            class MockedJdbc extends AbstractJdbcActor (
                                lowPriorityActorEnv = TestModule.lowPriorityActorEnv)
            {
                override protected def getConnection () : Connection = null
            }

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
