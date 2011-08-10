/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import org.specs.mock.Mockito
import java.sql.Connection

import unit.UnitTestHelper._
import actor.Operation
import io.db.jdbc.AbstractBulkJdbcActor
import utils.io.db.ConnectionProvider


class AbstractBulkJdbcActorTest extends JacoreSpecWithJUnit ("AbstractBulkJdbcActor specification")
                                   with Mockito
{
    import AbstractBulkJdbcActorTest._

    "AbstractBulkJdbcActor" should {
        // =================================================================================
        // =================================================================================
        // =================================================================================
        "prepare connection properly" in {
            val conn = mock [Connection]
            val connProv = mock [ConnectionProvider]

            connProv.open () returns conn

            val actor = new AbstractBulkJdbcActor (
                                connectionProvider = connProv,
                                lowPriorityActorEnv = TestModule.lowPriorityActorEnv)
            {
                def testOperation : Operation.WithComplexResult [Unit] =
                    new AbstractOperation [Unit] {
                        override protected def processRequest () = {
                            // We should make sure that this doesn't create a new connection
                            // on each call
                            getConnection ().setClientInfo ("o1", "z")
                            getConnection ().setClientInfo ("o4", "x")
                            yieldResult (null)
                        }
                    }

                override protected def prepareConnection (connection : Connection) : Unit =
                    connection.setClientInfo ("abc", "xxx2")
            }

            try {
                val f1 = actor.testOperation.runWithFutureAsy ()
                val f2 = actor.testOperation.runWithFutureAsy ()

                actor.start ()

                f1.get ()
                f2.get ()
            } finally {
                actor.stop ()
            }

            there was one(connProv).open ()                      then
                      one(conn).setClientInfo ("abc", "xxx2")    then
                      one(connProv).setAutoCommit (conn, false)  then
                      one(conn).setClientInfo ("o1", "z")        then
                      one(conn).setClientInfo ("o4", "x")        then
                      one(conn).setClientInfo ("o1", "z")        then
                      one(conn).setClientInfo ("o4", "x")        then
                      one(connProv).close (conn)                 orderedBy (conn, connProv)

            there was no(conn).commit () // Nothing to commit
            there was no(conn).rollback () // No reason for rollback
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "make it possible to run any SQL supported by DB" in {
        }
    }
}

object AbstractBulkJdbcActorTest {
}
