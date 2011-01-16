/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package dao

import actor.Operation

/**
 * Interface for data inserter.
 */
trait DataInserter[T] {
    /**
     * Insert data asynchronously.
     * @param data data to insert
     */
    def insertOperation (data : T) : Operation.WithResult [Unit]
}