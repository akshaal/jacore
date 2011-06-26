/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db

import java.sql.{Connection, PreparedStatement => JdbcPS}

import actor.{Actor, LowPriorityActorEnv}
import utils.io.db.SqlUtils
import jdbctype._
import jdbcaction._

/**
 * Template for all actors that are interested in working with JDBC.
 *
 * @param db database to use for connections
 * @param lowPriorityActorEnv low priority environment for this actor
 */
abstract class AbstractJdbcActor (lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{
    import AbstractJdbcActor._

    /**
     * Get connection to use for running statements.
     *
     * @return connection
     */
    protected def getConnection () : Connection


    /**
     * Setup connection. Called right after a connection is opened.
     * Default implementation does nothing.
     *
     * @param connection connection to be prepared
     */
    protected def prepareConnection (connection : Connection) : Unit = {}


    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // Prepared statement

    /**
     * Prepared parameterless statement. Call it as a function to get result of execution.
     *
     * @param [R] type of result
     */
    trait PreparedStatement0 [+R] extends Function0 [R] with PreparedStatement [R]


     /**
     * Prepared parametrized statement with one parameter.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T] type of the parameter
     */
    trait PreparedStatement1 [-T, +R] extends Function1 [T, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with two parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     */
    trait PreparedStatement2 [-T1, -T2, +R] extends Function2 [T1, T2, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with three parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     */
    trait PreparedStatement3 [-T1, -T2, -T3, +R]
                extends Function3 [T1, T2, T3, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with four parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     * @param [T4] type of the fourth parameter
     */
    trait PreparedStatement4 [-T1, -T2, -T3, -T4, +R]
                extends Function4 [T1, T2, T3, T4, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with five parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     * @param [T4] type of the fourth parameter
     * @param [T5] type of the 5th parameter
     */
    trait PreparedStatement5 [-T1, -T2, -T3, -T4, -T5, +R]
                extends Function5 [T1, T2, T3, T4, T5, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with six parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     * @param [T4] type of the fourth parameter
     * @param [T5] type of the 5th parameter
     * @param [T6] type of the 6th parameter
     */
    trait PreparedStatement6 [-T1, -T2, -T3, -T4, -T5, -T6, +R]
                extends Function6 [T1, T2, T3, T4, T5, T6, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with seven parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     * @param [T4] type of the fourth parameter
     * @param [T5] type of the 5th parameter
     * @param [T6] type of the 6th parameter
     * @param [T7] type of the 7th parameter
     */
    trait PreparedStatement7 [-T1, -T2, -T3, -T4, -T5, -T6, -T7, +R]
                extends Function7 [T1, T2, T3, T4, T5, T6, T7, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with eight parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     * @param [T4] type of the fourth parameter
     * @param [T5] type of the 5th parameter
     * @param [T6] type of the 6th parameter
     * @param [T7] type of the 7th parameter
     * @param [T8] type of the 8th parameter
     */
    trait PreparedStatement8 [-T1, -T2, -T3, -T4, -T5, -T6, -T7, -T8, +R]
                extends Function8 [T1, T2, T3, T4, T5, T6, T7, T8, R] with PreparedStatement [R]


    /**
     * Prepared parametrized statement with nine parameters.
     * Call it as a function to get result of execution.
     *
     * @param [R] type of result
     * @param [T1] type of the first parameter
     * @param [T2] type of the second parameter
     * @param [T3] type of the third parameter
     * @param [T4] type of the fourth parameter
     * @param [T5] type of the 5th parameter
     * @param [T6] type of the 6th parameter
     * @param [T7] type of the 7th parameter
     * @param [T8] type of the 8th parameter
     * @param [T9] type of the 9th parameter
     */
    trait PreparedStatement9 [-T1, -T2, -T3, -T4, -T5, -T6, -T7, -T8, -T9, +R]
                extends Function9 [T1, T2, T3, T4, T5, T6, T7, T8, T9, R] with PreparedStatement [R]


    /**
     * Prepare parameterless statement for execution.
     * After the statement is prepared, it can be executed by calling it as a function.
     * It is recommended to prepare statement in a private variable of the enclosing class.
     *
     * @param action action to prepare for execution
     */
    protected def prepare [R] (action : JdbcAction [R]) : PreparedStatement0 [R] =
        new {
            protected override val parameterCount = 0
            protected override val sqlAction = action
        } with PreparedStatement0 [R] {
            override def apply () : R = runAction ()
        }


    /**
     * Prepare statement with one parameter for execution.
     * After the statement is prepared, it can be executed providing paremeter value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param paramType type object of parameter
     */
    protected def prepare [T, R] (action : JdbcAction[R],
                                  paramType : JdbcType [T])
                            : PreparedStatement1 [T, R] =
        new {
            protected override val parameterCount = 1
            protected override val sqlAction = action
        } with PreparedStatement1 [T, R] {
            private val p1setter = getSetter (paramType)
            override def apply (param : T) : R = {
                p1setter (getJdbcPS(), 1, param)
                runAction ()
            }
        }


    /**
     * Prepare statement with two paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     */
    protected def prepare [T1, T2, R] (action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2])
                            : PreparedStatement2 [T1, T2, R] =
        new {
            protected override val parameterCount = 2
            protected override val sqlAction = action
        } with PreparedStatement2 [T1, T2, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            override def apply (p1 : T1, p2 : T2) : R = {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                runAction ()
            }
        }


    /**
     * Prepare statement with three paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     */
    protected def prepare [T1, T2, T3, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3])
                            : PreparedStatement3 [T1, T2, T3, R] =
        new {
            protected override val parameterCount = 3
            protected override val sqlAction = action
        } with PreparedStatement3 [T1, T2, T3, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3) : R = {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                runAction ()
            }
        }


    /**
     * Prepare statement with four paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     * @param param4Type type object of the fourth parameter
     */
    protected def prepare [T1, T2, T3, T4, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3],
                                       param4Type : JdbcType [T4])
                            : PreparedStatement4 [T1, T2, T3, T4, R] =
        new {
            protected override val parameterCount = 4
            protected override val sqlAction = action
        } with PreparedStatement4 [T1, T2, T3, T4, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            private val p4setter = getSetter (param4Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3, p4 : T4) : R = {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                runAction ()
            }
        }


    /**
     * Prepare statement with five paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     * @param param4Type type object of the fourth parameter
     * @param param5Type type object of the fifth parameter
     */
    protected def prepare [T1, T2, T3, T4, T5, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3],
                                       param4Type : JdbcType [T4],
                                       param5Type : JdbcType [T5])
                            : PreparedStatement5 [T1, T2, T3, T4, T5, R] =
        new {
            protected override val parameterCount = 5
            protected override val sqlAction = action
        } with PreparedStatement5 [T1, T2, T3, T4, T5, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            private val p4setter = getSetter (param4Type)
            private val p5setter = getSetter (param5Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3, p4 : T4, p5 : T5) : R = {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                runAction ()
            }
        }


    /**
     * Prepare statement with six paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     * @param param4Type type object of the fourth parameter
     * @param param5Type type object of the fifth parameter
     * @param param6Type type object of the sixth parameter
     */
    protected def prepare [T1, T2, T3, T4, T5, T6, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3],
                                       param4Type : JdbcType [T4],
                                       param5Type : JdbcType [T5],
                                       param6Type : JdbcType [T6])
                            : PreparedStatement6 [T1, T2, T3, T4, T5, T6, R] =
        new {
            protected override val parameterCount = 6
            protected override val sqlAction = action
        } with PreparedStatement6 [T1, T2, T3, T4, T5, T6, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            private val p4setter = getSetter (param4Type)
            private val p5setter = getSetter (param5Type)
            private val p6setter = getSetter (param6Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3, p4 : T4, p5 : T5, p6 : T6) : R = {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                runAction ()
            }
        }


    /**
     * Prepare statement with seven paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     * @param param4Type type object of the fourth parameter
     * @param param5Type type object of the fifth parameter
     * @param param6Type type object of the sixth parameter
     * @param param7Type type object of the seventh parameter
     */
    protected def prepare [T1, T2, T3, T4, T5, T6, T7, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3],
                                       param4Type : JdbcType [T4],
                                       param5Type : JdbcType [T5],
                                       param6Type : JdbcType [T6],
                                       param7Type : JdbcType [T7])
                            : PreparedStatement7 [T1, T2, T3, T4, T5, T6, T7, R] =
        new {
            protected override val parameterCount = 7
            protected override val sqlAction = action
        } with PreparedStatement7 [T1, T2, T3, T4, T5, T6, T7, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            private val p4setter = getSetter (param4Type)
            private val p5setter = getSetter (param5Type)
            private val p6setter = getSetter (param6Type)
            private val p7setter = getSetter (param7Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3, p4 : T4, p5 : T5, p6 : T6,
                                p7 : T7) : R =
            {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                p7setter (jdbcPS, 7, p7)
                runAction ()
            }
        }


    /**
     * Prepare statement with eight paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     * @param param4Type type object of the fourth parameter
     * @param param5Type type object of the fifth parameter
     * @param param6Type type object of the sixth parameter
     * @param param7Type type object of the seventh parameter
     * @param param8Type type object of the 8th parameter
     */
    protected def prepare [T1, T2, T3, T4, T5, T6, T7, T8, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3],
                                       param4Type : JdbcType [T4],
                                       param5Type : JdbcType [T5],
                                       param6Type : JdbcType [T6],
                                       param7Type : JdbcType [T7],
                                       param8Type : JdbcType [T8])
                            : PreparedStatement8 [T1, T2, T3, T4, T5, T6, T7, T8, R] =
        new {
            protected override val parameterCount = 8
            protected override val sqlAction = action
        } with PreparedStatement8 [T1, T2, T3, T4, T5, T6, T7, T8, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            private val p4setter = getSetter (param4Type)
            private val p5setter = getSetter (param5Type)
            private val p6setter = getSetter (param6Type)
            private val p7setter = getSetter (param7Type)
            private val p8setter = getSetter (param8Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3, p4 : T4, p5 : T5, p6 : T6,
                                p7 : T7, p8 : T8) : R =
            {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                p7setter (jdbcPS, 7, p7)
                p8setter (jdbcPS, 8, p8)
                runAction ()
            }
        }


    /**
     * Prepare statement with nine paremters for execution.
     * After the statement is prepared, it can be executed providing paremeters value to
     * the prepared statement. It is recommended to prepare statement in a private variable
     * of the enclosing class.
     *
     * @param action action to prepare for execution
     * @param param1Type type object of the first parameter
     * @param param2Type type object of the second parameter
     * @param param3Type type object of the third parameter
     * @param param4Type type object of the fourth parameter
     * @param param5Type type object of the fifth parameter
     * @param param6Type type object of the sixth parameter
     * @param param7Type type object of the seventh parameter
     * @param param8Type type object of the 8th parameter
     * @param param9Type type object of the 9th parameter
     */
    protected def prepare [T1, T2, T3, T4, T5, T6, T7, T8, T9, R] (
                                       action : JdbcAction[R],
                                       param1Type : JdbcType [T1],
                                       param2Type : JdbcType [T2],
                                       param3Type : JdbcType [T3],
                                       param4Type : JdbcType [T4],
                                       param5Type : JdbcType [T5],
                                       param6Type : JdbcType [T6],
                                       param7Type : JdbcType [T7],
                                       param8Type : JdbcType [T8],
                                       param9Type : JdbcType [T9])
                            : PreparedStatement9 [T1, T2, T3, T4, T5, T6, T7, T8, T9, R] =
        new {
            protected override val parameterCount = 9
            protected override val sqlAction = action
        } with PreparedStatement9 [T1, T2, T3, T4, T5, T6, T7, T8, T9, R] {
            private val p1setter = getSetter (param1Type)
            private val p2setter = getSetter (param2Type)
            private val p3setter = getSetter (param3Type)
            private val p4setter = getSetter (param4Type)
            private val p5setter = getSetter (param5Type)
            private val p6setter = getSetter (param6Type)
            private val p7setter = getSetter (param7Type)
            private val p8setter = getSetter (param8Type)
            private val p9setter = getSetter (param9Type)
            override def apply (p1 : T1, p2 : T2, p3 : T3, p4 : T4, p5 : T5, p6 : T6,
                                p7 : T7, p8 : T8, p9 : T9) : R =
            {
                val jdbcPS = getJdbcPS ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                p7setter (jdbcPS, 7, p7)
                p8setter (jdbcPS, 8, p8)
                p9setter (jdbcPS, 9, p9)
                runAction ()
            }
        }

    
    /**
     * Abstract prepared statement.
     *
     * @param [R] type of expected result
     */
    trait PreparedStatement [+R] {
        /**
         * Number of parameters that this prepared statement must prepare.
         */
        protected val parameterCount : Int

        /**
         * Action definition.
         */
        protected val sqlAction : JdbcAction [R]

        /**
         * Function to be used to axecute action.
         */
        private val actionRunner : Function0 [R] = null

        /**
         * Reference to the real prepared statement.
         */
        private var jdbcPsOption : Option [JdbcPS] = None

        /**
         * Execute current action.
         */
        protected def runAction () : R = {
            actionRunner ()
        }

        /**
         * Return JDBC PreparedStatement object associated with this Jacore PreparedStatemnt.
         * Create a new PreparedStatement if nothing is associated yet.
         */
        protected def getJdbcPS () : JdbcPS =
            jdbcPsOption match {
                case Some (jdbcPS) => jdbcPS
                case None =>
                    associateNewJdbcPS ()
                    getJdbcPS ()
            }

        /**
         * Construct new JdbcPS to be associated with this prepared statement.
         */
        private def associateNewJdbcPS () : Unit = {
            assert (jdbcPsOption.isEmpty)

            val jdbcPS = null
            jdbcPsOption = Some (jdbcPS)
        }

        // ---------------------------------------------------------------------------
        // Initialization of prepared statement

        // Validate sql parameters. Throws [[java.lang.IllegalArgumentException]] if
        // given statement is invalid.
        if (sqlAction.validate) {
            val foundParameterCount = SqlUtils.countPlaceholders (sqlAction.statement)

            if (parameterCount != foundParameterCount) {
                throw new IllegalArgumentException (
                    "Given sql statement (" + sqlAction.statement + ") expected to have "
                    + parameterCount + " parameters, but " + foundParameterCount + " found!")
            }
        }
    }
}


/**
 * Helper object for AbstractJdbcActor.
 */
private[db] object AbstractJdbcActor {
    /**
     * Function object to set Array parameter on JdbcPS.
     */
    object ArraySetter extends Function3 [JdbcPS, Int, java.sql.Array, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Array) : Unit =
            ps.setArray (idx, arg)
    }

    /**
     * Function object to set InputStream parameter with ascii data on JdbcPS.
     */
    object AsciiStreamSetter extends Function3 [JdbcPS, Int, java.io.InputStream, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.InputStream) : Unit =
            ps.setAsciiStream (idx, arg)
    }

    /**
     * Function object to set BigDecimal parameter on JdbcPS.
     */
    object BigDecimalSetter extends Function3 [JdbcPS, Int, java.math.BigDecimal, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.math.BigDecimal) : Unit =
            ps.setBigDecimal (idx, arg)
    }

    /**
     * Function object to set InputStream parameter with binary data on JdbcPS.
     */
    object BinaryStreamSetter extends Function3 [JdbcPS, Int, java.io.InputStream, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.InputStream) : Unit =
            ps.setBinaryStream (idx, arg)
    }

    /**
     * Function object to set Blob parameter on JdbcPS.
     */
    object BlobSetter extends Function3 [JdbcPS, Int, java.sql.Blob, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Blob) : Unit =
            ps.setBlob (idx, arg)
    }

    /**
     * Function object to set InputStream providing data for Blob parameter on JdbcPS.
     */
    object BlobStreamSetter extends Function3 [JdbcPS, Int, java.io.InputStream, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.InputStream) : Unit =
            ps.setBlob (idx, arg)
    }

    /**
     * Function object to set Boolean parameter on JdbcPS.
     */
    object BooleanSetter extends Function3 [JdbcPS, Int, Boolean, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Boolean) : Unit =
            ps.setBoolean (idx, arg)
    }

    /**
     * Function object to set byte parameter on JdbcPS.
     */
    object ByteSetter extends Function3 [JdbcPS, Int, Byte, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Byte) : Unit =
            ps.setByte (idx, arg)
    }

    /**
     * Function object to set byte array parameter on JdbcPS.
     */
    object BytesSetter extends Function3 [JdbcPS, Int, Array[Byte], Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Array[Byte]) : Unit =
            ps.setBytes (idx, arg)
    }

    /**
     * Function object to set Reader parameter with character data on JdbcPS.
     */
    object CharacterStreamSetter extends Function3 [JdbcPS, Int, java.io.Reader, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.Reader) : Unit =
            ps.setCharacterStream (idx, arg)
    }

    /**
     * Function object to set Clob parameter on JdbcPS.
     */
    object ClobSetter extends Function3 [JdbcPS, Int, java.sql.Clob, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Clob) : Unit =
            ps.setClob (idx, arg)
    }

    /**
     * Function object to set Reader providing data for Clob parameter on JdbcPS.
     */
    object ClobStreamSetter extends Function3 [JdbcPS, Int, java.io.Reader, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.Reader) : Unit =
            ps.setClob (idx, arg)
    }

    /**
     * Function object to set Date parameter on JdbcPS.
     */
    object SqlDateSetter extends Function3 [JdbcPS, Int, java.sql.Date, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Date) : Unit =
            ps.setDate (idx, arg)
    }

    /**
     * Function object to set Date parameter on JdbcPS.
     */
    object DateSetter extends Function3 [JdbcPS, Int, java.util.Date, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.util.Date) : Unit =
            ps.setDate (idx, new java.sql.Date (arg.getTime))
    }

    /**
     * Function object to set Double parameter on JdbcPS.
     */
    object DoubleSetter extends Function3 [JdbcPS, Int, Double, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Double) : Unit =
            ps.setDouble (idx, arg)
    }

    /**
     * Function object to set Float parameter on JdbcPS.
     */
    object FloatSetter extends Function3 [JdbcPS, Int, Float, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Float) : Unit =
            ps.setFloat (idx, arg)
    }

    /**
     * Function object to set Int parameter on JdbcPS.
     */
    object IntSetter extends Function3 [JdbcPS, Int, Int, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Int) : Unit =
            ps.setInt (idx, arg)
    }

    /**
     * Function object to set Long parameter on JdbcPS.
     */
    object LongSetter extends Function3 [JdbcPS, Int, Long, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Long) : Unit =
            ps.setLong (idx, arg)
    }

    /**
     * Function object to set Reader parameter with ncharacter data on JdbcPS.
     */
    object NCharacterStreamSetter extends Function3 [JdbcPS, Int, java.io.Reader, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.Reader) : Unit =
            ps.setNCharacterStream (idx, arg)
    }

    /**
     * Function object to set NClob parameter on JdbcPS.
     */
    object NClobSetter extends Function3 [JdbcPS, Int, java.sql.NClob, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.NClob) : Unit =
            ps.setNClob (idx, arg)
    }

    /**
     * Function object to set Reader providing data for NClob parameter on JdbcPS.
     */
    object NClobStreamSetter extends Function3 [JdbcPS, Int, java.io.Reader, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.io.Reader) : Unit =
            ps.setNClob (idx, arg)
    }

    /**
     * Function object to set NString parameter on JdbcPS.
     */
    object NStringSetter extends Function3 [JdbcPS, Int, String, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : String) : Unit =
            ps.setNString (idx, arg)
    }

    /**
     * Function object to set Object parameter on JdbcPS.
     */
    object ObjectSetter extends Function3 [JdbcPS, Int, Object, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Object) : Unit =
            ps.setObject (idx, arg)
    }

    /**
     * Function object to set Ref parameter on JdbcPS.
     */
    object RefSetter extends Function3 [JdbcPS, Int, java.sql.Ref, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Ref) : Unit =
            ps.setRef (idx, arg)
    }

    /**
     * Function object to set RowId parameter on JdbcPS.
     */
    object RowIdSetter extends Function3 [JdbcPS, Int, java.sql.RowId, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.RowId) : Unit =
            ps.setRowId (idx, arg)
    }

    /**
     * Function object to set Short parameter on JdbcPS.
     */
    object ShortSetter extends Function3 [JdbcPS, Int, Short, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : Short) : Unit =
            ps.setShort (idx, arg)
    }

    /**
     * Function object to set SQLXML parameter on JdbcPS.
     */
    object SqlXmlSetter extends Function3 [JdbcPS, Int, java.sql.SQLXML, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.SQLXML) : Unit =
            ps.setSQLXML (idx, arg)
    }

    /**
     * Function object to set String parameter on JdbcPS.
     */
    object StringSetter extends Function3 [JdbcPS, Int, String, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : String) : Unit =
            ps.setString (idx, arg)
    }

    /**
     * Function object to set Time parameter on JdbcPS.
     */
    object TimeSetter extends Function3 [JdbcPS, Int, java.sql.Time, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Time) : Unit =
            ps.setTime (idx, arg)
    }

    /**
     * Function object to set Timestamp parameter on JdbcPS.
     */
    object TimestampSetter extends Function3 [JdbcPS, Int, java.sql.Timestamp, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.sql.Timestamp) : Unit =
            ps.setTimestamp (idx, arg)
    }

    /**
     * Function object to set Url parameter on JdbcPS.
     */
    object UrlSetter extends Function3 [JdbcPS, Int, java.net.URL, Unit] {
        override def apply (ps : JdbcPS, idx : Int, arg : java.net.URL) : Unit =
            ps.setURL (idx, arg)
    }

    /**
     * Returns setter for the given param type.
     */
    protected def getSetter [T] (paramType : JdbcType [T]) : Function3 [JdbcPS, Int, T, Unit] =
        paramType match {
            case JdbcArray                  => ArraySetter
            case JdbcAsciiStream            => AsciiStreamSetter
            case JdbcBigDecimal             => BigDecimalSetter
            case JdbcBinaryStream           => BinaryStreamSetter
            case JdbcBlob                   => BlobSetter
            case JdbcBlobStream             => BlobStreamSetter
            case JdbcBoolean                => BooleanSetter
            case JdbcByte                   => ByteSetter
            case JdbcBytes                  => BytesSetter
            case JdbcCharacterStream        => CharacterStreamSetter
            case JdbcClob                   => ClobSetter
            case JdbcClobStream             => ClobStreamSetter
            case JdbcDate                   => DateSetter
            case JdbcDouble                 => DoubleSetter
            case JdbcFloat                  => FloatSetter
            case JdbcInt                    => IntSetter
            case JdbcLong                   => LongSetter
            case JdbcNCharacterStream       => NCharacterStreamSetter
            case JdbcNClob                  => NClobSetter
            case JdbcNClobStream            => NClobStreamSetter
            case JdbcNString                => NStringSetter
            case JdbcObject                 => ObjectSetter
            case JdbcRef                    => RefSetter
            case JdbcRowId                  => RowIdSetter
            case JdbcShort                  => ShortSetter
            case JdbcSqlDate                => SqlDateSetter
            case JdbcSqlXml                 => SqlXmlSetter
            case JdbcString                 => StringSetter
            case JdbcTime                   => TimeSetter
            case JdbcTimestamp              => TimestampSetter
            case JdbcUrl                    => UrlSetter
        }
}