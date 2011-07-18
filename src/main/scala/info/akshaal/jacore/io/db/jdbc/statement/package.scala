/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc

import `type`.JdbcType


/**
 * Package object that contains implicit to be useful for Statement types.
 */
package object statement {
    /**
     * Implicit function to convert string into placeholderless statement.
     * 
     * @param sqlStatement string statement to be converted to Statement0 object
     * @return object created from the given string
     */
    @inline
    implicit def string2statement (sqlStatement : String) : Statement0 =
        Statement0 (sql = sqlStatement)


    // - - - -  - - - - - - - - Part that is private to this package - - - - - - - - -- - - - - - - -

    /**
     * Immutable collection type used to keep placeholders/parameters/values.
     *
     * @tparam T type of elements in collection
     */
    private[statement] type Collection [T] = Vector [T]

    /**
     * Value for empty immutable collection that is used in statement representation.
     * Should be used to hide underlying collection implementation and make it possible
     * to replace it without changing code in Statement* classes.
     *
     * @tparam type of elements in collection
     * @return empty collection value
     */
    @inline
    private[statement] def emptyCollection [T] : Collection [T] = Vector.empty

    /**
     * Type that describes SQL statement parameter. SQL statement parameter is defined
     * by its JdbcType and might have value provided.
     */
    private[statement] type Parameter =
            (JdbcType [ParameterValue], Option [ParameterValue]) forSome {type ParameterValue}

    /**
     * Collection of SQL statement parameters.
     */
    private[statement] type Parameters = Collection [Parameter]   
}
