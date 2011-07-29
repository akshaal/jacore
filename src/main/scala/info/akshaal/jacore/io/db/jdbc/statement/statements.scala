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
 *
 * @define JdbcTypeXStart type of object that defines a way in which a value for the
 * @define JdbcTypeXEnd placeholder should be handled by JDBC
 * @define StatementX
 *    SQL statement with $placeholdersCount placeholders.
 *
 *    @param JdbcType1  $JdbcTypeXStart first $JdbcTypeXEnd
 *    @param JdbcType2  $JdbcTypeXStart second $JdbcTypeXEnd
 *    @param JdbcType3  $JdbcTypeXStart third $JdbcTypeXEnd
 *    @param JdbcType5  $JdbcTypeXStart fifth $JdbcTypeXEnd
 *    @param JdbcType6  $JdbcTypeXStart sixth $JdbcTypeXEnd
 *    @param JdbcType7  $JdbcTypeXStart seventh $JdbcTypeXEnd
 *    @param JdbcType8  $JdbcTypeXStart 8th $JdbcTypeXEnd
 *    @param JdbcType9  $JdbcTypeXStart 9th $JdbcTypeXEnd
 *    @param JdbcType10 $JdbcTypeXStart 10th $JdbcTypeXEnd
 *    @param JdbcType11 $JdbcTypeXStart 11th $JdbcTypeXEnd
 *    @param JdbcType12 $JdbcTypeXStart 12th $JdbcTypeXEnd
 *    @param JdbcType13 $JdbcTypeXStart 13th $JdbcTypeXEnd
 *    @param JdbcType14 $JdbcTypeXStart 14th $JdbcTypeXEnd
 *    @param JdbcType15 $JdbcTypeXStart 15th $JdbcTypeXEnd
 *    @param JdbcType16 $JdbcTypeXStart 16th $JdbcTypeXEnd
 *    @param JdbcType17 $JdbcTypeXStart 17th $JdbcTypeXEnd
 *    @param JdbcType18 $JdbcTypeXStart 18th $JdbcTypeXEnd
 *    @param JdbcType19 $JdbcTypeXStart 19th $JdbcTypeXEnd
 *    @param JdbcType20 $JdbcTypeXStart 20th $JdbcTypeXEnd
 *    @param JdbcType21 $JdbcTypeXStart 21th $JdbcTypeXEnd
 *    @param JdbcType22 $JdbcTypeXStart 22th $JdbcTypeXEnd
 *    @param JdbcType23 $JdbcTypeXStart 23th $JdbcTypeXEnd
 *    @param JdbcType24 $JdbcTypeXStart 24th $JdbcTypeXEnd
 *    @param JdbcType25 $JdbcTypeXStart 25th $JdbcTypeXEnd
 *
 * @define PlusPlus
 *    Constructs a new $HigherStat object from this one and the given {jdbcType} placeholder.
 * 
 *    @tparam JdbcType type of JDBC type case object
 *    @param jdbcType case object that defined JDBC type of the placeholder
 *    @return the new statement object which SQL string is {this.sql + " ?"}, all parameters
 *            are copied from {this} statement with an extra parameter defined by {jdbcType}
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

    /**
     * Construct a new statement object with additional SQL string appended to it.
     *
     * @param sql SQL string to append at the end of the SQL string of this statement
     * @return the new statement object ending with the given SQL string, parameters are preserved
     */
    final def ++ (sql : String) : this.type = sameType (thisSqlWith (sql), parameters)

    /**
     * Construct a new statement object by appending SQL string from the given statement
     * to the SQL string of this statement. All values provided for the given statement
     * are copied to the new statement along with the values provided for this statement.
     *
     * @param stmt statement to add to {this}
     * @return the new statement object which SQL string is {this.sql + stmt.sql} and
     *         paramaters are (this.parameters ++ stmt.parameters) 
     */
    final def ++ (stmt : Statement0) : this.type = sameType (thisSqlWith (stmt.sql), parameters ++ stmt.parameters)

    /**
     * Construct a new statement object from this one by appending the given predefined value.
     *
     * @tparam Value type of value
     * @param jdbcType JDBC type case object
     * @param value value to be set
     * @return the new statement object which SQL string is {this.sql + " ?"}, all parameters
     *         are copied from {this} statement with an extra parameter defined by {jdbcType}
     *         and {value} pair
     */
    final def ++ [Value] (jdbcType : AbstractJdbcType [Value], value : Value) : this.type =
                    sameType (thisSqlWithArg, thisParametersWith (jdbcType, value))

    // - - - -  - - - - - - - - Protected and private part - - - - - - - - - - - - - - -

    /**
     * All parameters of the statement. This is a mix of placeholders and provided values.
     */
    protected val parameters : Parameters

    /**
     * Construct a new Statement of the same type as one of {this} object but with different set
     * of class parameters. This is just a way to keep type information while cloning object.
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


/**
 * SQL statement without placeholders.
 *
 * @define HigherStat Statement1
 */
final case class Statement0 private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters = emptyCollection)
                    extends Statement
{
    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement0 (newSql, newParameters).asInstanceOf [this.type]

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType) : Statement1 [JdbcType] =
                    Statement1 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * SQL statement with one placeholder.
 *
 * @param JdbcType1 type of object that defines a way in which a value for the placeholder is
 *                  passed to DB
 * @define HigherStat Statement2
 */
final case class Statement1 [JdbcType1 <: AbstractJdbcType [_]] private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters)
                    extends Statement
{
    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement1 (newSql, newParameters).asInstanceOf [this.type]

    /**
     * Placeholder of the statement.
     */
    lazy val placeholder : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType) : Statement2 [JdbcType1, JdbcType] =
                    Statement2 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount two
 * @define HigherStat Statement3
 */
final case class Statement2 [JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement
{
    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement2 (newSql, newParameters).asInstanceOf [this.type]

    /**
     * First placeholder of the statement.
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * Second placeholder of the statement.
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)
}

