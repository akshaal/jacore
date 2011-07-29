/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package statement

import `type`.AbstractJdbcType


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Companion object for Statement class.
 */
object Statement {
    /**
     * Immutable collection type used to keep placeholders/parameters/values.
     * It is important to have fast random access to the values in the collection,
     *
     * @tparam T type of elements in collection
     */
    type Collection [T] = Vector [T]

    /**
     * Value for empty immutable collection that is used in statement representation.
     * Should be used to hide underlying collection implementation and make it possible
     * to replace it without changing code in Statement* classes.
     *
     * @tparam T type of elements in collection
     * @return empty collection value
     */
    @inline
    def emptyCollection [T] : Collection [T] = Vector.empty

    // - - - - - - - - - - - - Placeholder - - - - - - - - - - -

    /**
     * The type that represents placeholder in a statement.
     *
     * @tparam JdbcType JDBC type accepted by placeholder
     * @param jdbcType case object representing the given JDBC type
     * @param position placeholder's position in a statement (starting from 1)
     */
    final case class Placeholder [JdbcType <: AbstractJdbcType [_]] (
                            jdbcType : JdbcType,
                            position : Int)

    /**
     * Colection of placeholders.
     */
    private[statement] type Placeholders = Collection [Placeholder [_]]

    // - - - - - - - - - - - - ProvidedValue - - - - - - - - - - -

    /**
     * The type that represents value in a statement. It is something like a placeholder
     * but with a value already provided during construction of placeholder.
     *
     * @tparam Value type of value for the JDBC parameter
     * @tparam JdbcType type that represents JDBC type of the JDBC parameter
     * @param jdbcType case object representing the given jdbc type
     * @param value provided value object
     * @param position placeholder position in a statement (starting from 1)
     */
    final case class ProvidedValue [Value, JdbcType <: AbstractJdbcType [Value]] (
                            jdbcType : JdbcType,
                            value : Value,
                            position : Int)

    /**
     * Collection of provided values.
     */
    type ProvidedValues = Collection [ProvidedValue [Value, AbstractJdbcType [Value]] forSome {type Value}]

    // - - - - - - - - - - - - Parameter - - - - - - - - - - -

    /**
     * Type that describes SQL statement parameter. SQL statement parameter is defined
     * by its AbstractJdbcType and might have value provided.
     *
     * @tparam Value type of value parameter accepts
     * @param jdbcType case object representing the given JDBC type
     * @param optionalValue optional value. If not provided then this parameter is a placeholder
     */
    private[statement] final case class Parameter [Value] (
                            jdbcType : AbstractJdbcType [Value],
                            optionalValue : Option [Value])

    /**
     * Collection of SQL statement parameters.
     */
    private[statement] type Parameters = Collection [Parameter [_]]
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////

import Statement._

/**
 * Abstract SQL statement. Contains sql statement string which might be parametrized with placeholders.
 *
 * @define ShouldBeLazy
 *    Should be lazy value for it is only used by Statement implementation when
 *    it needs to get a detailed information. For intermediate statements,
 *    which used to construct a final statement, this value is never calculated.
 */
sealed abstract class Statement {
    /**
     * Statement as SQL string suitable for JDBC
     */
    val sql : String

    /**
     * Collection of provided values.
     *
     * $ShouldBeLazy
     */
    final lazy val providedValues : ProvidedValues = {
        def mkYield [Value] (parameter : Parameter [Value], idx : Int) =
            ProvidedValue [Value, AbstractJdbcType [Value]] (
                    parameter.jdbcType,
                    parameter.optionalValue.get,
                    idx + 1)

        for ((parameter, idx) <- zippedParameters if parameter.optionalValue.isDefined)
            yield mkYield (parameter, idx)
    }

    final def ++ (thatSql : String) : this.type = sameType (thisSqlWith (thatSql), parameters)

    final def ++ (stmt : Statement0) : this.type = sameType (thisSqlWith (stmt.sql), parameters ++ stmt.parameters)

    final def ++ [Value] (jdbcType : AbstractJdbcType [Value], value : Value) : this.type =
                    sameType (thisSqlWithArg, thisParametersWith (jdbcType, value))

    // - - - -  - - - - - - - - Protected and private part - - - - - - - - - - - - - - -

    /**
     * All parameters of the statement. This is a mix of placeholders and provided values.
     */
    protected val parameters : Parameters

    /**
     * Construct a new Statement of the same type as this one but with different set of
     * parameters. This is just a way to keep type information while cloning object.
     *
     * @param newSql new sql string for the new statement
     * @param newParameters new set of parameters
     */
    protected def sameType (newSql : String, newParameters : Parameters) : this.type

    /**
     * Collection of placeholders.
     *
     * Placeholder doesn't have value at the moment of definition.
     * Values for placeholders are provided later during invokation of prepared action.
     * 
     * $ShouldBeLazy
     */
    protected final lazy val placeholders : Placeholders =
        for ((parameter, idx) <- zippedParameters if parameter.optionalValue.isEmpty)
            yield Placeholder (parameter.jdbcType, idx + 1)

    /**
     * The same as {parameters} collection but with indexes (starting from 0).
     *
     * $ShouldBeLazy
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

    /**
     * Returns this set of parameters with the additional placeholder of the given JDBC type.
     * This method is used to construct a new set of parameters for a new Statement of higher
     * arity (number of placeholders) than the current one.
     *
     * @param jdbcType JDBC type object that represent type of placeholder
     */
    @inline
    protected final def thisParametersWith (jdbcType : AbstractJdbcType [_]) : Parameters =
            parameters :+ Parameter (jdbcType, None)

    /**
     * Returns this set of parameters with the additional provided value of the given JDBC type.
     * This method is used to construct a new set of parameters for a new Statement with same
     * arity (number of placeholders) as the current one.
     *
     * @tparam Value type of value
     * @param jdbcType JDBC type object that represent type of placeholder
     */
    @inline
    protected final def thisParametersWith [Value] (jdbcType : AbstractJdbcType [Value], value : Value) : Parameters =
            parameters :+ Parameter (jdbcType, Some (value))

    /**
     * Get placeholder by its index, This method is not type safe and should only
     * be used when it is 100% clear that placeholder at the given position is actually
     * one of the given type.
     *
     * @param idx index of placeholder, starting from 0
     */
    @inline
    protected final def getPlaceholder [ThisPlaceholder <: Placeholder [_]] (idx : Int) : ThisPlaceholder =
            placeholders (idx).asInstanceOf [ThisPlaceholder]
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


final case class Statement0 private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters = emptyCollection)
                    extends Statement
{
    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement0 (newSql, newParameters).asInstanceOf [this.type]

    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType) : Statement1 [JdbcType] =
                    Statement1 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


final case class Statement1 [JdbcType <: AbstractJdbcType [_]] private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters)
                    extends Statement
{
    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement1 (newSql, newParameters).asInstanceOf [this.type]

    lazy val placeholder : Placeholder [JdbcType] = getPlaceholder (0)
}
