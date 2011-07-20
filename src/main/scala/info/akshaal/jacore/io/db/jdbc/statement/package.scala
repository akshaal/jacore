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
}
