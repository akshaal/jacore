/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db

import java.sql.Connection

import actor.LowPriorityActorEnv
import utils.io.db.Db

/**
 * Template for all actors that are interested in working with JDBC in bulk mode.
 * It means that all requests to this actor are done in one transaction.
 * This is useful for an actor which makes a small update for each processed message.
 *
 * @param db database to use for connections
 * @param lowPriorityActorEnv low priority environment for this actor
 */
abstract class AbstractBulkJdbcActor (db : Db,
                                      lowPriorityActorEnv : LowPriorityActorEnv)
                    extends AbstractJdbcActor (db = db,
                                               lowPriorityActorEnv = lowPriorityActorEnv)
{
    // Currently opened connection. Must not be used directly from method except
    // from getConnection or afterActs
    private[this] var currentConnection : Option[Connection] = None

    /**
     * {@InheritDoc}
     */
    protected override final def getConnection () : Connection =
        currentConnection match {
            case Some (connection) => connection
            case None =>
                // Open connection
                val connection = db.open ()

                // This MUST BE first action we do. We must remember opened connection
                // so it could be closed later even if an exception occurs later in this method
                currentConnection = Some (connection)

                // Setup autocommit feature. This can be expensive operation, so do
                // it only when needed.
                if (! connection.getAutoCommit) {
                    connection.setAutoCommit (false)
                }

                // Give a chance to subclasses to customize connection
                prepareConnection (connection)

                connection
        }

    /**
     * {@InheritDoc}
     *
     * Commit transaction if any and close connection.
     */
    protected final override def afterActs () : Unit =
        // Do job only if connection is opened
        for (connection <- currentConnection) {
            try {
                
            } finally {
                db.close (connection)
            }
        }
}
