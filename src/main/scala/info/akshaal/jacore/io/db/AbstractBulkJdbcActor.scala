/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db

import java.sql.Connection

import actor.LowPriorityActorEnv
import utils.io.db.ConnectionProvider

/**
 * Template for all actors that are interested in working with JDBC in bulk mode.
 * It means that all requests to this actor are done in one transaction.
 * This is useful for an actor which makes a small update for each processed message.
 *
 * If processing of one message fails during bulk processing, then the whole transaction
 * is rolled back.
 *
 * @param connectionProvider connection provider to use to access connections
 * @param lowPriorityActorEnv low priority environment for this actor
 */
abstract class AbstractBulkJdbcActor (connectionProvider : ConnectionProvider,
                                      lowPriorityActorEnv : LowPriorityActorEnv)
                    extends AbstractJdbcActor (lowPriorityActorEnv = lowPriorityActorEnv)
{
    // Currently opened connection. Must not be used directly from method except
    // from getConnection or afterActs
    private[this] var currentConnection : Option[Connection] = None

    // TODO: !!!!!!!!!!!!!! What if something fails? Should we reprocess everything?
    //       !!!!!!!!!!!!!!!!!!!!!!!!!!!! We do this in one transaction.

    /**
     * {@InheritDoc}
     */
    protected override final def getConnection () : Connection =
        currentConnection match {
            case Some (connection) => connection
            case None =>
                // Open connection
                val connection = connectionProvider.open ()

                // This MUST BE first action we do. We must remember opened connection
                // so it could be closed later even if an exception occurs later in this method
                currentConnection = Some (connection)

                // Give a chance to subclasses to customize connection
                prepareConnection (connection)

                // Setup autocommit feature. It must be turned OFF because everything
                // within one bulk update is supposed to be done in one transaction.
                connectionProvider.setAutoCommit (connection, false)

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
                // TODO: !!!!!!!!!!!!!!!!! Commit
            } finally {
                connectionProvider.close (connection)
            }
        }

        // TODO !!!!!!!! Should we call rollback in case of failure???
}
