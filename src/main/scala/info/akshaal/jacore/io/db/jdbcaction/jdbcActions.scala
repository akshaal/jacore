/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbcaction

/**
 * A type that JDBC can possibly handle.
 *
 * @param T type of action result
 */
sealed abstract class JdbcAction [+T] (val statement : String, val validate : Boolean)

/**
 * Some arbitrary JDBC operation.
 *
 * @param statement sql statement
 * @param validate validate sql statement if true
 */
final case class JdbcCommand (override val statement : String,
                              override val validate : Boolean = true)
                    extends JdbcAction [Boolean] (
                                statement = statement,
                                validate = validate)

/**
 * JDBC query.
 *
 * @param statement sql statement
 * @param validate validate sql statement if true
 */
final case class JdbcQuery (override val statement : String,
                            override val validate : Boolean = true)
                    extends JdbcAction [java.sql.ResultSet] (
                                statement = statement,
                                validate = validate)

/**
 * JDBC update.
 *
 * @param statement sql statement
 * @param validate validate sql statement if true
 */
final case class JdbcUpdate (override val statement : String,
                             override val validate : Boolean = true)
                    extends JdbcAction [Int] (
                                statement = statement,
                                validate = validate)
