/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package statement

import `type`.JdbcType


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Companion object for Statement class.
 */
object Statement {
    /**
     * Immutable collection type used to keep placeholders/parameters/values.
     *
     * @tparam T type of elements in collection
     */
    type Collection [T] = Vector [T]

    /**
     * Value for empty immutable collection that is used in statement representation.
     * Should be used to hide underlying collection implementation and make it possible
     * to replace it without changing code in Statement* classes.
     *
     * @tparam type of elements in collection
     * @return empty collection value
     */
    @inline
    def emptyCollection [T] : Collection [T] = Vector.empty

    /**
     * Type that describes SQL statement parameter. SQL statement parameter is defined
     * by its JdbcType and might have value provided.
     *
     * @tparam Value type of value parameter accepts
     */
    private[statement] type Parameter [Value] = (JdbcType [Value], Option [Value])

    /**
     * Collection of SQL statement parameters.
     */
    private[statement] type Parameters = Collection [Parameter [_]]

    /**
     * This type represents placeholder in a statement. Placeholder is represented by its JdbcType
     * and its position (starting from 1).
     *
     * @tparam [PlaceholderJdbcType] JdbcType accepted by placeholder
     */
    type Placeholder [PlaceholderJdbcType <: JdbcType [_]] = (PlaceholderJdbcType, Int)

    /**
     * Colection of placeholders.
     */
    type Placeholders = Collection [Placeholder [_]]
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////

import Statement._

/**
 * Abstract SQL statement. Contains sql statement string which might be parametrized with placeholders.
 */
sealed abstract class Statement {
    /**
     * Statement as SQL string suitable for JDBC
     */
    val sql : String

    /**
     * Collection of placeholders.
     *
     * Placeholder doesn't have value at the moment of definition
     * and provided later during invokation for prepared action.
     *
     * Should be lazy for it is only used by Statement implementation when
     * it needs to get a particular placeholder. For intermediate statements
     * which used to construct a final statement this value is never calculated.
     */
    final lazy val placeholders : Placeholders =
        for ((parameter, idx) <- zippedParameters if parameter._2.isEmpty)
            yield (parameter._1, idx + 1)

    // - - - -  - - - - - - - - Protected and private part - - - - - - - - - - - - - - -

    /**
     * All parameters of the statement. This is a mix of placeholders and values.
     */
    protected val parameters : Parameters

    /**
     * The same as {parameters} collection but with indexes (starting from 0).
     * Should be lazy for the same reason as for {placeholders} value!
     */
    private lazy val zippedParameters : Collection [(Parameter [_], Int)] = parameters.zipWithIndex

    /**
     * This SQL string with additional parameter. This value should be used during
     * construction of new Statement from this one when extra parameter is added to this
     * statement.
     */
    @inline
    protected final def thisSqlWithArg : String = sql + " ?"

    /**
     * Returns this SQL string concatenated with the one given as argument to this method.
     * Should be used during construction of new statements based on this one.
     */
    @inline
    protected final def thisSqlWith (thatSql : String) : String = sql + " " + thatSql

    @inline
    protected final def thisParametersWith (jdbcType : JdbcType [_]) : Parameters =
            parameters :+ ((jdbcType, None))

    @inline
    protected final def getPlaceholder [ThisPlaceholder <: Placeholder [_]] (idx : Int) : ThisPlaceholder =
            placeholders (idx).asInstanceOf [ThisPlaceholder]
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


final case class Statement0 (override val sql : String) extends Statement {
    protected override val parameters = emptyCollection

    def + (thatSql : String) : Statement0 = Statement0 (thisSqlWith (thatSql))

    def + (stmt : Statement0) : Statement0 = Statement0 (thisSqlWith (stmt.sql))

    def + [JdbcType1 <: JdbcType[_]] (jdbcType1 : JdbcType1) : Statement1 [JdbcType1] =
                    Statement1 (thisSqlWithArg, thisParametersWith (jdbcType1))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


final case class Statement1 [JdbcType1 <: JdbcType [_]] private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters) extends Statement
{
    lazy val placeholder : Placeholder [JdbcType1] = getPlaceholder (0)
}
