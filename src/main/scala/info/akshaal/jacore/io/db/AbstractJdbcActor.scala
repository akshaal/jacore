/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db

import java.sql.Connection

import actor.{Actor, LowPriorityActorEnv}
import utils.io.db.Db

/**
 * Template for all actors that are interested in working with JDBC.
 *
 * @param db database to use for connections
 * @param lowPriorityActorEnv low priority environment for this actor
 */
abstract class AbstractJdbcActor (db : Db,
                                  lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{
    /**
     * Get connection to use for running statements.
     *
     * @return connection
     */
    protected def getConnection () : Connection

    /**
     * Setup connection. Called right after a connection is opened.
     * Default implementation does nothing.
     *
     * @param connection connection to be prepared
     */
    protected def prepareConnection (connection : Connection) : Unit = {}

}
