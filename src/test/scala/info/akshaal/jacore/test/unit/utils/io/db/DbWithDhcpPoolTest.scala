/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.io.db

import java.sql.Connection
import unit.UnitTestHelper._

import utils.io.db.{H2Url, DbWithDbcpPool}

class DbWithDhcpPoolTest extends JacoreSpecWithJUnit ("DbWithDhcpPool class specification") {
    import H2Url._

    var idx = 0
    def mkMemoryLocation () = {
        idx += 1
        Memory (this.getClass.getName.replace(".", "_") + "_" + idx)
    }

    "DbWithDhcpPool" should {
        "have reasonable defaults" in {
            val db = new DbWithDbcpPool (H2Url (mkMemoryLocation))
            db.maxOpenConnections  must beGreaterThan (1)
            db.close ()
        }

        "be pooling even with only url parameter" in {
            val db = new DbWithDbcpPool (H2Url (mkMemoryLocation))

            // Open connections first time
            val connections : List[Connection] =
                repeatToList (db.maxOpenConnections) {
                    val conn = db.open
                    conn  must  notBeNull
                    conn
                }
            val connectionHashcodes = connections.map (_.hashCode)
            connections.foreach (db.close (_))


            // Open connections again
            val connections2 : List[Connection] =
                repeatToList (db.maxOpenConnections) {
                    val conn = db.open
                    conn  must  notBeNull
                    conn
                }

            // Check that connections are the same as opened before
            for (connection <- connections2) {
                connection.hashCode  must beIn (connectionHashcodes)
            }

            // Close connections
            connections2.foreach (db.close (_))

            // Close pool
            db.close ()
        }

        "close db when requested" in {
            val db = new DbWithDbcpPool (H2Url (mkMemoryLocation))
            val conn = db.open ()
            db.close ()

            db.open () must throwA[IllegalStateException]
            conn.isClosed  must beFalse
            conn.close ()
            conn.isClosed  must beTrue
        }

        "provide valid connections" in {
            val sqls = List("CREATE TABLE x (i INT)")
            val db = new DbWithDbcpPool (H2Url (mkMemoryLocation), connectionInitSqls = sqls)
            val conn = db.open ()

            val st = conn.createStatement ()
            st.execute ("INSERT INTO x VALUES (13)")

            val q = conn.createStatement ()
            val qrs = q.executeQuery ("SELECT * FROM x")
            qrs.next ()
            qrs.getInt (1) must_== 13
            
            db.close (conn)
            db.close ()
        }
    }
}
