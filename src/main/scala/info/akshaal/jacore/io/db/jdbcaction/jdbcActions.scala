/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbcaction

/**
 * A type that JDBC can possibly handle.
 */
sealed abstract class JdbcAction (val statement : String, val validate : Boolean) {
    /**
     * Type of result of this action.
     */
    type Result
}

/**
 * Some arbitrary JDBC operation.
 *
 * @param statement sql statement
 * @param validate validate sql statement if true
 */
final case class JdbcCommand (override val statement : String,
                              override val validate : Boolean = true)
                    extends JdbcAction (statement = statement, validate = validate)
{
    type Result = Boolean
}

/**
 * JDBC query.
 *
 * @param statement sql statement
 * @param validate validate sql statement if true
 */
final case class JdbcQuery (override val statement : String,
                            override val validate : Boolean = true)
                    extends JdbcAction (statement = statement, validate = validate)
{
    type Result = java.sql.ResultSet
}

/**
 * JDBC update.
 *
 * @param statement sql statement
 * @param validate validate sql statement if true
 */
final case class JdbcUpdate (override val statement : String,
                             override val validate : Boolean = true)
                    extends JdbcAction (statement = statement, validate = validate)
{
    type Result = Int
}