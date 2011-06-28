/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.io.db

import org.specs.mock.Mockito
import java.sql.{Connection, SQLException}

import unit.UnitTestHelper._

import utils.io.db.ConnectionProvider

class ConnectionProviderTest extends JacoreSpecWithJUnit ("ConnectionProvider class specification")
                                with Mockito
{
    "ConnectionProvider" should {
        // =================================================================================
        // =================================================================================
        // =================================================================================
        "close connection without throwing SQLException" in {
            val conn = mock [Connection]
            conn.close () throws new SQLException ("blah")

            val connProv = new ConnectionProvider {
                override def open () : Connection = conn
            }

            connProv.close (conn)
            false must_== false
            there was one(conn).close ()
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "provide withConnection method that closes connection on exception" in {
            val conn = mock [Connection]
            val connProv = new ConnectionProvider {
                override def open () : Connection = conn
            }

            (connProv.withConnection (c => {
                    c  must_==  conn
                    c.setClientInfo ("a", "b")
                    throw new SQLException ("blah")
                }) : Unit) must throwA[SQLException]

            there was one(conn).setClientInfo ("a", "b")         then
                      one(conn).close ()                         orderedBy (conn)
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "ignore requests to setAutoCommit to false if autoCommit is already set to false" in {
            val conn = mock [Connection]
            conn.getAutoCommit () returns false

            val connProv = new ConnectionProvider {
                override def open () : Connection = conn
            }

            connProv.setAutoCommit (conn, false)

            there was one(conn).getAutoCommit ()
            there was no(conn).setAutoCommit (false)
            there was no(conn).setAutoCommit (true)
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "ignore requests to setAutoCommit to true if autoCommit is already set to true" in {
            val conn = mock [Connection]
            conn.getAutoCommit () returns true

            val connProv = new ConnectionProvider {
                override def open () : Connection = conn
            }

            connProv.setAutoCommit (conn, true)

            there was one(conn).getAutoCommit ()
            there was no(conn).setAutoCommit (false)
            there was no(conn).setAutoCommit (true)
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "not ignore requests to setAutoCommit to false if autoCommit is already set to true" in {
            val conn = mock [Connection]
            conn.getAutoCommit () returns true

            val connProv = new ConnectionProvider {
                override def open () : Connection = conn
            }

            connProv.setAutoCommit (conn, false)

            there was one(conn).getAutoCommit ()
            there was one(conn).setAutoCommit (false)
            there was no(conn).setAutoCommit (true)
        }

        // =================================================================================
        // =================================================================================
        // =================================================================================
        "not ignore requests to setAutoCommit to true if autoCommit is already set to false" in {
            val conn = mock [Connection]
            conn.getAutoCommit () returns false

            val connProv = new ConnectionProvider {
                override def open () : Connection = conn
            }

            connProv.setAutoCommit (conn, true)

            there was one(conn).getAutoCommit ()
            there was one(conn).setAutoCommit (true)
            there was no(conn).setAutoCommit (false)
        }
    }
}
