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
     * @param jdbcType case object representing the given JDBC type
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
    type ProvidedValues =
            Collection [ProvidedValue [Value, AbstractJdbcType [Value]] forSome {type Value}]


    // - - - - - - - - - - - - DomainPlaceholder - - - - - - - - - - -


    /**
     * Domain placeholder is used to pass a value that corresponds to a field in a domain object.
     * Access to domain object field is done through the provided function.
     *
     * Domain placeholder doesn't have value at the moment of definition.
     * The value for domain placeholder is supposed to be obtained at a time of
     * action execution from an object of the domain.
     *
     * @tparam Domain type of domain object that is possible to use for this placeholder
     * @tparam Value type of value for the JDBC parameter
     * @tparam JdbcType type that represents JDBC type of the JDBC parameter
     * @param jdbcType case object representing the given JDBC type
     * @param f function which returns a value from the domain object
     * @param position placeholder position in a statement (starting from 1)
     */
    final case class DomainPlaceholder [Domain, Value, JdbcType <: AbstractJdbcType [Value]] (
                            jdbcType : JdbcType,
                            f : Domain => Value,
                            position : Int)

    /**
     * Collection of provided values.
     */
    type DomainPlaceholders [Domain] =
            Collection [DomainPlaceholder [Domain, Value, AbstractJdbcType [Value]] forSome {type Value}]


    // - - - - - - - - - - - - Parameter - - - - - - - - - - -

    /**
     * Type that describes SQL statement parameter (a things that is represented
     * by ? character in a prepared SQL statement).
     *
     * Parameters are different from {DomainPlaceholder} / {Placeholder} / {ProvidedValue}
     * in a way that they don't have position value, their position is defined
     * by index in {parameters} collection.
     *
     * @tparam JdbcType type that represents JDBC type of the JDBC parameter
     * @tparam Value type of value parameter accepts
     * @param jdbcType case object representing the given JDBC type
     */
    private[statement] abstract sealed class Parameter [Value]

    private[statement] final case class PlaceholderParameter [Value] (jdbcType : AbstractJdbcType [Value])
                                    extends Parameter [Value]

    private[statement] final case class ProvidedParameter [Value] (
                                        jdbcType : AbstractJdbcType [Value], value : Value)
                                    extends Parameter [Value]

    private[statement] final case class DomainParameter [Domain, Value] (
                                        jdbcType : AbstractJdbcType [Value], f : Domain => Value)
                                    extends Parameter [Value]

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
 * @param Domain type of objects that this statement operates on using accessor functions.
 *               This type is Nothing if Statement doesn't use accessor functions to get values
 *               for its parameters.
 *
 * @define ShouldBeLazy
 *    Should be lazy (or def) value for it is only used by Statement implementation when
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
 *    @param JdbcType4  $JdbcTypeXStart fourth $JdbcTypeXEnd
 *    @param JdbcType5  $JdbcTypeXStart fifth $JdbcTypeXEnd
 *    @param JdbcType6  $JdbcTypeXStart sixth $JdbcTypeXEnd
 *    @param JdbcType7  $JdbcTypeXStart seventh $JdbcTypeXEnd
 *    @param JdbcType8  $JdbcTypeXStart eighth $JdbcTypeXEnd
 *    @param JdbcType9  $JdbcTypeXStart ninth $JdbcTypeXEnd
 *    @param JdbcType10 $JdbcTypeXStart tenth $JdbcTypeXEnd
 *
 * @define PlusPlus
 *    Constructs a new $HigherStat object from this one and the given {jdbcType} placeholder.
 * 
 *    @tparam JdbcType type of JDBC type case object
 *    @param jdbcType case object that defined JDBC type of the placeholder
 *    @return the new statement object which SQL string is {this.sql + " ?"}, all parameters
 *            are copied from {this} statement with an extra parameter defined by {jdbcType}
 *
 * @define placeholderOfSt placeholder of the statement.
 * @define AccPlaceholder1 First $placeholderOfSt
 * @define AccPlaceholder2 Second $placeholderOfSt
 * @define AccPlaceholder3 Third $placeholderOfSt
 * @define AccPlaceholder4 Fourth $placeholderOfSt
 * @define AccPlaceholder5 Fifth $placeholderOfSt
 * @define AccPlaceholder6 Sixth $placeholderOfSt
 * @define AccPlaceholder7 Seventh $placeholderOfSt
 * @define AccPlaceholder8 eighth $placeholderOfSt
 * @define AccPlaceholder9 ninth $placeholderOfSt
 * @define AccPlaceholder10 tenth $placeholderOfSt
 */
sealed abstract class Statement [Domain] {
    /**
     * Statement as SQL string suitable for JDBC
     */
    val sql : String

    /**
     * Collection of provided values.
     *
     * $ShouldBeLazy
     */
    final def providedValues : ProvidedValues = parametersByKind._2

    /**
     * Collection of domain placeholders.
     *
     * $ShouldBeLazy
     */
    final def domainPlaceholders : DomainPlaceholders [Domain] = parametersByKind._3

    /**
     * Construct a new statement object with additional SQL string appended to it.
     *
     * @param sql SQL string to append at the end of the SQL string of this statement
     * @return the new statement object ending with the given SQL string, parameters are preserved
     */
    final def ++ (sql : String) : ThisWith [Domain] = sameType (this.sql + " " + sql, parameters)

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
    final def ++ [Value] (jdbcType : AbstractJdbcType [Value], value : Value) : ThisWith [Domain] =
        sameType (thisSqlWithArg, parameters :+ ProvidedParameter (jdbcType, value))

    /**
     * Construct a new statement object from this one by introducting a new parameter
     * of the given JDBC type and the given function which will be used to get value from
     * domain object.
     *
     * Note that Statement must have Domain type properly specified for the statement before using
     * this method. Use {/:} method to specify type of domain object for statement.
     */
    final def +++ [Ret] (jdbcType : AbstractJdbcType [Ret], f : Domain => Ret) : ThisWith [Domain] =
        sameType (thisSqlWithArg, parameters :+ DomainParameter (jdbcType, f))

    /**
     * Specifies domain object type for the statement. Must be used before using {+++} method.
     * This method is only available for statements for which no domain type set yet
     * (i.e. domain type set to Nothing).
     *
     * @param clazz fully typed class, only type information from the given class is used
     * @return statement with domain object type set to the type of the given class
     * @example classOf [User] /: "INSERT INTO user SET name=" +++ (JdbcString, _.name)
     */
    final def /: [NewDomain] (clazz : Class [NewDomain])
                             (implicit v : NewDomainVerified [Domain, NewDomain]) 
                        : ThisWith [NewDomain] =
        this.asInstanceOf [ThisWith [NewDomain]]

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - --
    // - - - -  - - - - - - - - Protected and private part - - - - - - - - - - - - - - - - - - -  - - - -

    /**
     * Type of {this} object with variable domain part of it.
     * This type allows us to specify correct type in a subclass of Statement class.
     */
    protected type ThisWith [CustomDomain] <: Statement [CustomDomain]

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
    protected def sameType (newSql : String, newParameters : Parameters) : ThisWith [Domain]

    /**
     * Collection of placeholders.
     *
     * Placeholder doesn't have value at the moment of definition.
     * Values for placeholders are provided later during invokation of prepared action.
     *
     * $ShouldBeLazy
     */
    protected final lazy val placeholders : Placeholders = parametersByKind._1

    /**
     * Parameters separated by kind.
     *
     * $ShouldBeLazy
     */
    private lazy val parametersByKind : (Placeholders, ProvidedValues, DomainPlaceholders [Domain]) = {
        // Imperative implementation for performance
        var phs : Placeholders = emptyCollection
        var pvs : ProvidedValues = emptyCollection
        var dps : DomainPlaceholders [Domain] = emptyCollection
        var pos = 0

        def handle [Value] (parameter : Parameter [Value]) : Unit = {
            pos += 1

            parameter match {
                case PlaceholderParameter (jdbcType) =>
                    phs :+= Placeholder (jdbcType = jdbcType, position = pos)

                case ProvidedParameter (jdbcType, value) =>
                    pvs :+= ProvidedValue [Value, AbstractJdbcType [Value]] (
                                jdbcType = jdbcType,
                                value    = value,
                                position = pos)

                case DomainParameter (jdbcType, f) =>
                    dps :+= DomainPlaceholder [Domain, Value, AbstractJdbcType [Value]] (
                                jdbcType = jdbcType,
                                f        = f,
                                position = pos)
            }
        }

        for (parameter <- parameters) {
            handle (parameter)
        }

        // Result
        (phs, pvs, dps)
    }


    /**
     * This SQL string with additional parameter. This value should be used during
     * construction of new Statement from this one when extra parameter is added to this
     * statement.
     */
    @inline
    protected final def thisSqlWithArg : String = sql + " ?"

    /**
     * Returns this set of parameters with the additional placeholder of the given JDBC type.
     * This method is used to construct a new set of parameters for a new Statement of higher
     * arity (number of placeholders) than the current one.
     *
     * @param jdbcType JDBC type object that represent type of placeholder
     */
    @inline
    protected final def thisParametersWith (jdbcType : AbstractJdbcType [_]) : Parameters =
            parameters :+ PlaceholderParameter (jdbcType)

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
final case class Statement0 [Domain] private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters = emptyCollection)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] = Statement0 [CustomDomain]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement0 (newSql, newParameters)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType) : Statement1 [Domain, JdbcType] =
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
final case class Statement1 [Domain,
                             JdbcType1 <: AbstractJdbcType [_]] private [statement] (
                            override val sql : String,
                            protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] = Statement1 [CustomDomain, JdbcType1]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement1 (newSql, newParameters)

    /**
     * Placeholder of the statement.
     */
    lazy val placeholder : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                            : Statement2 [Domain, JdbcType1, JdbcType] =
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
final case class Statement2 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] = Statement2 [CustomDomain, JdbcType1, JdbcType2]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement2 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                                : Statement3 [Domain, JdbcType1, JdbcType2, JdbcType] =
                     Statement3 (thisSqlWithArg, thisParametersWith (jdbcType))
}

// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount three
 * @define HigherStat Statement4
 */
final case class Statement3 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement3 [CustomDomain, JdbcType1, JdbcType2, JdbcType3]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement3 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                             : Statement4 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType] =
                Statement4 (thisSqlWithArg, thisParametersWith (jdbcType))
}

// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount four
 * @define HigherStat Statement5
 */
final case class Statement4 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_],
                             JdbcType4 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement4 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement4 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                   : Statement5 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType] =
                            Statement5 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount five
 * @define HigherStat Statement6
 */
final case class Statement5 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_],
                             JdbcType4 <: AbstractJdbcType [_],
                             JdbcType5 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement5 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement5 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $AccPlaceholder5
     */
    lazy val placeholder5 : Placeholder [JdbcType5] = getPlaceholder (4)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                  : Statement6 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                                JdbcType] =
                           Statement6 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount six
 * @define HigherStat Statement7
 */
final case class Statement6 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_],
                             JdbcType4 <: AbstractJdbcType [_],
                             JdbcType5 <: AbstractJdbcType [_],
                             JdbcType6 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement6 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5, JdbcType6]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement6 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $AccPlaceholder5
     */
    lazy val placeholder5 : Placeholder [JdbcType5] = getPlaceholder (4)

    /**
     * $AccPlaceholder6
     */
    lazy val placeholder6 : Placeholder [JdbcType6] = getPlaceholder (5)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                  : Statement7 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                                JdbcType6, JdbcType] =
                        Statement7 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount seven
 * @define HigherStat Statement8
 */
final case class Statement7 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_],
                             JdbcType4 <: AbstractJdbcType [_],
                             JdbcType5 <: AbstractJdbcType [_],
                             JdbcType6 <: AbstractJdbcType [_],
                             JdbcType7 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement7 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                    JdbcType6, JdbcType7]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement7 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $AccPlaceholder5
     */
    lazy val placeholder5 : Placeholder [JdbcType5] = getPlaceholder (4)

    /**
     * $AccPlaceholder6
     */
    lazy val placeholder6 : Placeholder [JdbcType6] = getPlaceholder (5)

    /**
     * $AccPlaceholder7
     */
    lazy val placeholder7 : Placeholder [JdbcType7] = getPlaceholder (6)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                  : Statement8 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                                JdbcType6, JdbcType7, JdbcType] =
                        Statement8 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount 8
 * @define HigherStat Statement9
 */
final case class Statement8 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_],
                             JdbcType4 <: AbstractJdbcType [_],
                             JdbcType5 <: AbstractJdbcType [_],
                             JdbcType6 <: AbstractJdbcType [_],
                             JdbcType7 <: AbstractJdbcType [_],
                             JdbcType8 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement8 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                    JdbcType6, JdbcType7, JdbcType8]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement8 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $AccPlaceholder5
     */
    lazy val placeholder5 : Placeholder [JdbcType5] = getPlaceholder (4)

    /**
     * $AccPlaceholder6
     */
    lazy val placeholder6 : Placeholder [JdbcType6] = getPlaceholder (5)

    /**
     * $AccPlaceholder7
     */
    lazy val placeholder7 : Placeholder [JdbcType7] = getPlaceholder (6)

    /**
     * $AccPlaceholder8
     */
    lazy val placeholder8 : Placeholder [JdbcType8] = getPlaceholder (7)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                  : Statement9 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                                JdbcType6, JdbcType7, JdbcType8, JdbcType] =
                      Statement9 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount 9
 * @define HigherStat Statement10
 */
final case class Statement9 [Domain,
                             JdbcType1 <: AbstractJdbcType [_],
                             JdbcType2 <: AbstractJdbcType [_],
                             JdbcType3 <: AbstractJdbcType [_],
                             JdbcType4 <: AbstractJdbcType [_],
                             JdbcType5 <: AbstractJdbcType [_],
                             JdbcType6 <: AbstractJdbcType [_],
                             JdbcType7 <: AbstractJdbcType [_],
                             JdbcType8 <: AbstractJdbcType [_],
                             JdbcType9 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement9 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                    JdbcType6, JdbcType7, JdbcType8, JdbcType9]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement9 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $AccPlaceholder5
     */
    lazy val placeholder5 : Placeholder [JdbcType5] = getPlaceholder (4)

    /**
     * $AccPlaceholder6
     */
    lazy val placeholder6 : Placeholder [JdbcType6] = getPlaceholder (5)

    /**
     * $AccPlaceholder7
     */
    lazy val placeholder7 : Placeholder [JdbcType7] = getPlaceholder (6)

    /**
     * $AccPlaceholder8
     */
    lazy val placeholder8 : Placeholder [JdbcType8] = getPlaceholder (7)

    /**
     * $AccPlaceholder9
     */
    lazy val placeholder9 : Placeholder [JdbcType9] = getPlaceholder (8)

    /**
     * $PlusPlus
     */
    def ++ [JdbcType <: AbstractJdbcType [_]] (jdbcType : JdbcType)
                  : Statement10 [Domain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                                 JdbcType6, JdbcType7, JdbcType8, JdbcType9, JdbcType] =
                        Statement10 (thisSqlWithArg, thisParametersWith (jdbcType))
}


// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////
// /////////////////////////////////////////////////////////////////////////////////////////////


/**
 * $StatementX
 * 
 * @define placeholdersCount 10
 * @define HigherStat Statement11
 */
final case class Statement10 [Domain,
                              JdbcType1 <: AbstractJdbcType [_],
                              JdbcType2 <: AbstractJdbcType [_],
                              JdbcType3 <: AbstractJdbcType [_],
                              JdbcType4 <: AbstractJdbcType [_],
                              JdbcType5 <: AbstractJdbcType [_],
                              JdbcType6 <: AbstractJdbcType [_],
                              JdbcType7 <: AbstractJdbcType [_],
                              JdbcType8 <: AbstractJdbcType [_],
                              JdbcType9 <: AbstractJdbcType [_],
                              JdbcType10 <: AbstractJdbcType [_]] private [statement] (
                                    override val sql : String,
                                    protected val parameters : Parameters)
                    extends Statement [Domain]
{
    protected type ThisWith [CustomDomain] =
        Statement10 [CustomDomain, JdbcType1, JdbcType2, JdbcType3, JdbcType4, JdbcType5,
                     JdbcType6, JdbcType7, JdbcType8, JdbcType9, JdbcType10]

    protected override def sameType (newSql : String, newParameters : Parameters) =
        Statement10 (newSql, newParameters)

    /**
     * $AccPlaceholder1
     */
    lazy val placeholder1 : Placeholder [JdbcType1] = getPlaceholder (0)

    /**
     * $AccPlaceholder2
     */
    lazy val placeholder2 : Placeholder [JdbcType2] = getPlaceholder (1)

    /**
     * $AccPlaceholder3
     */
    lazy val placeholder3 : Placeholder [JdbcType3] = getPlaceholder (2)

    /**
     * $AccPlaceholder4
     */
    lazy val placeholder4 : Placeholder [JdbcType4] = getPlaceholder (3)

    /**
     * $AccPlaceholder5
     */
    lazy val placeholder5 : Placeholder [JdbcType5] = getPlaceholder (4)

    /**
     * $AccPlaceholder6
     */
    lazy val placeholder6 : Placeholder [JdbcType6] = getPlaceholder (5)

    /**
     * $AccPlaceholder7
     */
    lazy val placeholder7 : Placeholder [JdbcType7] = getPlaceholder (6)

    /**
     * $AccPlaceholder8
     */
    lazy val placeholder8 : Placeholder [JdbcType8] = getPlaceholder (7)

    /**
     * $AccPlaceholder9
     */
    lazy val placeholder9 : Placeholder [JdbcType9] = getPlaceholder (8)

    /**
     * $AccPlaceholder10
     */
    lazy val placeholder10 : Placeholder [JdbcType10] = getPlaceholder (9)
}

