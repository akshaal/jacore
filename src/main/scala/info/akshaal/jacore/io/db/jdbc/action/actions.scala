/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package action

/**
 * Abstract action that JDBC can possible perform.
 */
sealed abstract class AbstractJdbcAction {
    /**
     * Type of result of this action.
     */
    type Result
}


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Command

/**
 * Some arbitrary JDBC operation.
 */
sealed case class JdbcCommand () extends AbstractJdbcAction {
    type Result = Boolean
}

/**
 * Some arbitrary JDBC operation with default action parameters.
 */
object JdbcCommand extends JdbcCommand ()


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Query

/**
 * JDBC query.
 */
sealed case class JdbcQuery () extends AbstractJdbcAction {
    type Result = java.sql.ResultSet
}

/**
 * JDBC query with default action parameters.
 */
object JdbcQuery extends JdbcQuery ()


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Update

/**
 * JDBC update.
 */
sealed case class JdbcUpdate () extends AbstractJdbcAction {
    type Result = Int
}

/**
 * JDBC update with default action parameters.
 */
object JdbcUpdate extends JdbcUpdate ()


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
// Batch

/**
 * JDBC batch.
 */
sealed case class JdbcBatch () extends AbstractJdbcAction {
    type Result = Unit
}

/**
 * JDBC batch with default action parameters.
 */
object JdbcBatch extends JdbcBatch ()
