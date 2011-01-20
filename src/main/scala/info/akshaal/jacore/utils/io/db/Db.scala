/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

import java.sql.{Connection, SQLException}

/**
 * Abstract database.
 */
trait Db {
    /**
     * Open database connection.
     * @return opened connection
     */
    def open () : Connection

    /**
     * Close database (release all resources except open connections).
     */
    def close () : Unit

    /**
     * Close database connection. Default implementation invokes close on 'connection'
     * object ignoring 'SQLException'.
     *
     * @param connection connection to close
     */
    def close (connection : Connection) : Unit = {
        try {
            connection.close ()
        } catch {
            case _ : SQLException =>
        }
    }

    /**
     * Execute function 'f' with opened connection. Connection is closed
     * automatically after 'f' invokation finished.
     */
    def withConnection [T] (f : Connection => T) : T = {
        val connection = open ()

        try {
            f (connection)
        } finally {
            close (connection)
        }
    }
}
