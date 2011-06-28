/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

import java.sql.{Connection, SQLException}

/**
 * Abstract connection provider. This trait is supposed to provide methods
 * for obtaining and handling of one connection.
 */
trait ConnectionProvider {
    /**
     * Open database connection.
     *
     * @return opened connection
     */
    def open () : Connection

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
     * automatically after 'f' invocation finished.
     *
     * @param f function to run connection with
     */
    def withConnection [T] (f : Connection => T) : T = {
        val connection = open ()

        try {
            f (connection)
        } finally {
            close (connection)
        }
    }

    /**
     * Set autocommit for the connection. This is supposed to be effecient and customizable
     * way to set auto commit. For some DB it is faster to check current value for autocommit,
     * before setting new value.
     *
     * @param connection connection for setting autocommit option
     * @param value new value for autocommit option
     */
    def setAutoCommit (connection : Connection, value : Boolean) : Unit = {
        if (connection.getAutoCommit != value) {
            connection.setAutoCommit (value)
        }
    }
}
