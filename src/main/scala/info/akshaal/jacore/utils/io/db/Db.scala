/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

/**
 * Abstract database.
 */
trait Db extends ConnectionProvider {
    /**
     * Close database (release all resources except open connections).
     */
    def close () : Unit
}
