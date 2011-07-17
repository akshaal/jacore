/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package statement

import scala.collection.immutable.Vector

import `type`._
import Statement._


/**
 * Abstract SQL statement. Contains sql statement string which might be parametrized with placeholders.
 */
sealed abstract class Statement {
    /**
     * Statement as SQL string suitable for JDBC
     */
    val sql : String

    protected val paramDefs : ParamDefs

    /**
     * This SQL string with additional placeholder. This value should be used during
     * construction of new Statement from this one when extra placeholder is added to this
     * statement.
     */
    protected final def thisSqlWithArg : String = sql + " ?"

    /**
     * Returns this SQL string concatenated with the one given as argument to this method.
     * Should be used during construction of new statements based on this one.
     */
    protected final def thisSqlWith (thatSql : String) : String = sql + " " + thatSql

    protected final def thisParamDefsWith (jdbcType : JdbcType [_]) : ParamDefs =
            paramDefs :+ ((jdbcType, None))
}


final case class Statement0 (override val sql : String) extends Statement {
    protected override val paramDefs = emptyParamDefs

    def + (thatSql : String) : Statement0 = Statement0 (thisSqlWith (thatSql))

    def + (stmt : Statement0) : Statement0 = Statement0 (thisSqlWith (stmt.sql))

    def + [JdbcType1 <: JdbcType[_]] (jdbcType1 : JdbcType1) : Statement1 [JdbcType1] =
                    Statement1 (thisSqlWithArg, thisParamDefsWith (jdbcType1))
}


final case class Statement1 [JdbcType1 <: JdbcType [_]] private [statement] (
                            override val sql : String,
                            val paramDefs : ParamDefs) extends Statement
{
    //lazy val param
}



private[statement] object Statement {
    type ParamDef = (JdbcType [Parameter], Option [Parameter]) forSome {type Parameter}

    type ParamDefs = Vector [ParamDef]
    
    val emptyParamDefs : ParamDefs = Vector.empty
}
