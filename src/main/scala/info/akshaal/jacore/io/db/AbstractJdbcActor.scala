/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db

import java.sql.{Connection, PreparedStatement}

import actor.{Actor, LowPriorityActorEnv}
import utils.io.db.SqlUtils
import jdbctype._
import jdbcaction._

/**
  * Template for all actors that are interested in working with JDBC.
  *
  * @param db database to use for connections
  * @param lowPriorityActorEnv low priority environment for this actor
  *
  * @define paramWillBePassed parameter that will be passed to JDBC statement of the action
  *
  * @define CommonTParams
  *    @tparam Result JDBC action result type
  *    @tparam Action JDBC action type
  *    @tparam Param the type of the parameter for the action
  *    @tparam Param1 the type of the first $paramWillBePassed
  *    @tparam Param2 the type of the second $paramWillBePassed
  *    @tparam Param3 the type of the third $paramWillBePassed
  *    @tparam Param4 the type of the fourth $paramWillBePassed
  *    @tparam Param5 the type of the fifth $paramWillBePassed
  *    @tparam Param6 the type of the sixth $paramWillBePassed
  *    @tparam Param7 the type of the seventh $paramWillBePassed
  *    @tparam Param8 the type of the eighth $paramWillBePassed
  *    @tparam Param9 the type of the ninth $paramWillBePassed
  *
  * @define PreparedActionDeclaration
  *    Call it as a function to perform the action that this class encapsulates.
  *    $CommonTParams
  *
  * @define prepareMethod
  *    After the action is prepared, it can be executed providing paremeter values to
  *    the prepared action. It is highly recommended to prepare all actions in private
  *    variables of the enclosing class during the class initialization.
  *
  *    $CommonTParams
  *    @param action the action to prepare for execution
  *    @param paramJdbcType JDBC type object of the parameter
  *    @param param1JdbcType JDBC type object of the first parameter
  *    @param param2JdbcType JDBC type object of the second parameter
  *    @param param3JdbcType JDBC type object of the third parameter
  *    @param param4JdbcType JDBC type object of the fourth parameter
  *    @param param5JdbcType JDBC type object of the fifth parameter
  *    @param param6JdbcType JDBC type object of the sixth parameter
  *    @param param7JdbcType JDBC type object of the seventh parameter
  *    @param param8JdbcType JDBC type object of the eighth parameter
  *    @param param9JdbcType JDBC type object of the ninth parameter
  *
  */
abstract class AbstractJdbcActor (lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{
    import AbstractJdbcActor._

    /**
     * Get connection for using to run actions.
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
    // Prepared action

    /**
     * Prepared parameterless JDBC action.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction0 [+Result, +Action <: JdbcAction [Result]]
                extends Function0 [Result] with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with one parameter.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction1 [-Param, +Result, +Action <: JdbcAction [Result]]
                extends Function1 [Param, Result] with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with two parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction2 [-Param1, -Param2, +Result, +Action <: JdbcAction [Result]]
                extends Function2 [Param1, Param2, Result] with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with three parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction3 [-Param1, -Param2, -Param3, +Result, +Action <: JdbcAction [Result]]
                extends Function3 [Param1, Param2, Param3, Result]
                   with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with four parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction4 [-Param1, -Param2, -Param3, -Param4, +Result,
                           +Action <: JdbcAction [Result]]
                extends Function4 [Param1, Param2, Param3, Param4, Result]
                   with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with five parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction5 [-Param1, -Param2, -Param3, -Param4, -Param5, +Result,
                           +Action <: JdbcAction [Result]]
                extends Function5 [Param1, Param2, Param3, Param4, Param5, Result]
                   with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with six parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction6 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                           +Result, +Action <: JdbcAction [Result]]
                extends Function6 [Param1, Param2, Param3, Param4, Param5, Param6, Result]
                   with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with seven parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction7 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                           -Param7, +Result, +Action <: JdbcAction [Result]]
                extends Function7 [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Result]
                   with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with eight parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction8 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                           -Param7, -Param8, +Result, +Action <: JdbcAction [Result]]
                extends Function8 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                   Param8, Result] with PreparedAction [Result, Action]


    /**
     * Prepared parametrized action with nine parameters.
     *
     * $PreparedActionDeclaration
     */
    trait PreparedAction9 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6, -Param7,
                           -Param8, -Param9, +Result, +Action <: JdbcAction [Result]]
                extends Function9 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                   Param8, Param9, Result]
                   with PreparedAction [Result, Action]


    /**
     * Prepare parameterless action.
     *
     * $prepareMethod
     */
    protected def prepare [Result, Action <: JdbcAction [Result]] (action : Action)
                            : PreparedAction0 [Result, Action] =
        new {
            protected override val parameterCount = 0
            protected override val sqlAction = action
        } with PreparedAction0 [Result, Action] {
            override def apply () : Result = {
                val jdbcPS = getPreparedStatement ()
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with one parameter.
     *
     * $prepareMethod
     */
    protected def prepare [Param, Result, Action <: JdbcAction [Result]] (
                                  action : Action,
                                  paramJdbcType : JdbcType [Param])
                            : PreparedAction1 [Param, Result, Action] =
        new {
            protected override val parameterCount = 1
            protected override val sqlAction = action
        } with PreparedAction1 [Param, Result, Action] {
            private val p1setter = getSetter (paramJdbcType)

            override def apply (param : Param) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, param)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with two paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Result, Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2])
                            : PreparedAction2 [Param1, Param2, Result, Action] =
        new {
            protected override val parameterCount = 2
            protected override val sqlAction = action
        } with PreparedAction2 [Param1, Param2, Result, Action] {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)

            override def apply (p1 : Param1, p2 : Param2) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with three paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Result, Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3])
                            : PreparedAction3 [Param1, Param2, Param3, Result, Action] =
        new {
            protected override val parameterCount = 3
            protected override val sqlAction = action
        } with PreparedAction3 [Param1, Param2, Param3, Result, Action] {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with four paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Param4, Result, Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3],
                                param4JdbcType : JdbcType [Param4])
                            : PreparedAction4 [Param1, Param2, Param3, Param4, Result, Action] =
        new {
            protected override val parameterCount = 4
            protected override val sqlAction = action
        } with PreparedAction4 [Param1, Param2, Param3, Param4, Result, Action] {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)
            private val p4setter = getSetter (param4JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with five paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Param4, Param5, Result,
                           Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3],
                                param4JdbcType : JdbcType [Param4],
                                param5JdbcType : JdbcType [Param5])
                            : PreparedAction5 [Param1, Param2, Param3, Param4, Param5,
                                               Result, Action] =
        new {
            protected override val parameterCount = 5
            protected override val sqlAction = action
        } with PreparedAction5 [Param1, Param2, Param3, Param4, Param5, Result, Action] {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)
            private val p4setter = getSetter (param4JdbcType)
            private val p5setter = getSetter (param5JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4,
                                p5 : Param5) : Result =
            {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with six paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Result,
                           Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3],
                                param4JdbcType : JdbcType [Param4],
                                param5JdbcType : JdbcType [Param5],
                                param6JdbcType : JdbcType [Param6])
                            : PreparedAction6 [Param1, Param2, Param3, Param4, Param5,
                                               Param6, Result, Action] =
        new {
            protected override val parameterCount = 6
            protected override val sqlAction = action
        } with PreparedAction6 [Param1, Param2, Param3, Param4, Param5, Param6,
                                Result, Action]
        {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)
            private val p4setter = getSetter (param4JdbcType)
            private val p5setter = getSetter (param5JdbcType)
            private val p6setter = getSetter (param6JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4, p5 : Param5,
                                p6 : Param6) : Result =
            {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with seven paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Result,
                           Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3],
                                param4JdbcType : JdbcType [Param4],
                                param5JdbcType : JdbcType [Param5],
                                param6JdbcType : JdbcType [Param6],
                                param7JdbcType : JdbcType [Param7])
                            : PreparedAction7 [Param1, Param2, Param3, Param4, Param5, Param6,
                                               Param7, Result, Action] =
        new {
            protected override val parameterCount = 7
            protected override val sqlAction = action
        } with PreparedAction7 [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Result,
                                Action]
        {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)
            private val p4setter = getSetter (param4JdbcType)
            private val p5setter = getSetter (param5JdbcType)
            private val p6setter = getSetter (param6JdbcType)
            private val p7setter = getSetter (param7JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4, p5 : Param5,
                                p6 : Param6, p7 : Param7) : Result =
            {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                p7setter (jdbcPS, 7, p7)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with eight paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Param8, Result,
                           Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3],
                                param4JdbcType : JdbcType [Param4],
                                param5JdbcType : JdbcType [Param5],
                                param6JdbcType : JdbcType [Param6],
                                param7JdbcType : JdbcType [Param7],
                                param8JdbcType : JdbcType [Param8])
                            : PreparedAction8 [Param1, Param2, Param3, Param4, Param5, Param6,
                                               Param7, Param8, Result, Action] =
        new {
            protected override val parameterCount = 8
            protected override val sqlAction = action
        } with PreparedAction8 [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Param8,
                                Result, Action]
        {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)
            private val p4setter = getSetter (param4JdbcType)
            private val p5setter = getSetter (param5JdbcType)
            private val p6setter = getSetter (param6JdbcType)
            private val p7setter = getSetter (param7JdbcType)
            private val p8setter = getSetter (param8JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4, p5 : Param5,
                                p6 : Param6, p7 : Param7, p8 : Param8) : Result =
            {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                p7setter (jdbcPS, 7, p7)
                p8setter (jdbcPS, 8, p8)
                runAction (jdbcPS)
            }
        }


    /**
     * Prepare action with nine paremters.
     *
     * $prepareMethod
     */
    protected def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Param8, Param9,
                           Result, Action <: JdbcAction [Result]] (
                                action : Action,
                                param1JdbcType : JdbcType [Param1],
                                param2JdbcType : JdbcType [Param2],
                                param3JdbcType : JdbcType [Param3],
                                param4JdbcType : JdbcType [Param4],
                                param5JdbcType : JdbcType [Param5],
                                param6JdbcType : JdbcType [Param6],
                                param7JdbcType : JdbcType [Param7],
                                param8JdbcType : JdbcType [Param8],
                                param9JdbcType : JdbcType [Param9])
                            : PreparedAction9 [Param1, Param2, Param3, Param4, Param5, Param6,
                                               Param7, Param8, Param9, Result, Action] =
        new {
            protected override val parameterCount = 9
            protected override val sqlAction = action
        } with PreparedAction9 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                Param8, Param9, Result, Action]
        {
            private val p1setter = getSetter (param1JdbcType)
            private val p2setter = getSetter (param2JdbcType)
            private val p3setter = getSetter (param3JdbcType)
            private val p4setter = getSetter (param4JdbcType)
            private val p5setter = getSetter (param5JdbcType)
            private val p6setter = getSetter (param6JdbcType)
            private val p7setter = getSetter (param7JdbcType)
            private val p8setter = getSetter (param8JdbcType)
            private val p9setter = getSetter (param9JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4, p5 : Param5,
                                p6 : Param6, p7 : Param7, p8 : Param8, p9 : Param9) : Result =
            {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                p6setter (jdbcPS, 6, p6)
                p7setter (jdbcPS, 7, p7)
                p8setter (jdbcPS, 8, p8)
                p9setter (jdbcPS, 9, p9)
                runAction (jdbcPS)
            }
        }

    
    /**
     * Abstract prepared action to be used to access database.
     * 
     * Call it as a function to perform requested action.
     *
     * @tparam Result type of expected action result
     * @tparam Action type of JDBC action that this prepared action is supposed to perform
     */
    trait PreparedAction [+Result, +Action <: JdbcAction [Result]] {
        /**
         * Number of parameters that this prepared action must prepare. This value
         * should be defined in the implementation of the prepared action using early
         * initialization for it is used in the trait for validation during trait initialization.
         *
         * Because traits do not support parameters we use this way to customize trait behavior.
         */
        protected val parameterCount : Int

        /**
         * Action definition. Should be set using early initialization. Because
         * traits do not support parameters we use this way to customize trait behavior.
         */
        protected val sqlAction : Action

        /**
         * Function to be used to axecute action. Action runner receives prepared
         * statement and runs jdbc action that this action runner is responsible for.
         */
        private val actionRunner : ActionRunner [Result] = null // TODO !!!!!!!!!!!!!!!!!
        // TODO !!!!!!!!!!!!!! ^^^^^^ Action runner depends on the way we run it
        // it should be possible to write custom actions runner proviers
        // because in one case we have to process one message in one transaction
        // in some cases we need to use addBatch on statement... or possible
        // consider when preparing statement?

        /**
         * Reference to the JDBC's prepared statement.
         */
        private var jdbcPsOption : Option [PreparedStatement] = None

        /**
         * Execute action that is associated with this prepared action.
         * This method is supposed to be called at the and of 'apply' method implementation
         * when all parameters are set.
         *
         * @param jdbcPS JDBC PreparedStatement that was previouslyy (at the beginning of
         *               the 'apply' method) obtained using {getPreparedStatement} method.
         */
        @inline
        protected final def runAction (jdbcPS : PreparedStatement) : Result = actionRunner (jdbcPS)

        /**
         * Return JDBC PreparedStatement object associated with this PreparedAction.
         *
         * Createss a new PreparedStatement if nothing was associated so far.
         */
        final def getPreparedStatement () : PreparedStatement =
            jdbcPsOption match {
                case Some (jdbcPS) => jdbcPS
                case None =>
                    // jdbcPs is not defined, so we have to define it
                    associateNewPreparedStatement ()

                    // Recursive call to this function, we are sure that this call
                    // will return correct value
                    getPreparedStatement ()
            }

        /**
         * Construct new PreparedStatement to be associated with this prepared action.
         */
        private def associateNewPreparedStatement () : Unit = {
            assert (jdbcPsOption.isEmpty)

            val jdbcPS = null // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
            jdbcPsOption = Some (jdbcPS)
        }

        // ---------------------------------------------------------------------------
        // Initialization of prepared action

        // Validate sql parameters. Throws [[java.lang.IllegalArgumentException]] if
        // given action is invalid.
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
     * ActionRunner is a function that receives JDBC PreparedStatement and executes
     * some action upon it.
     * 
     * All JDBC parameters must be set action runnder is called
     *
     * @tparam Result type of action result.
     */
    trait ActionRunner [+Result] extends Function1 [PreparedStatement, Result]

    // ===================================================================================
    // = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
    // ===================================================================================

    /**
     * Function object to set Array parameter on PreparedStatement.
     */
    object ArraySetter extends Function3 [PreparedStatement, Int, java.sql.Array, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Array) : Unit =
            ps.setArray (idx, arg)
    }

    /**
     * Function object to set InputStream parameter with ascii data on PreparedStatement.
     */
    object AsciiStreamSetter extends Function3 [PreparedStatement, Int, java.io.InputStream, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
            ps.setAsciiStream (idx, arg)
    }

    /**
     * Function object to set BigDecimal parameter on PreparedStatement.
     */
    object BigDecimalSetter extends Function3 [PreparedStatement, Int, java.math.BigDecimal, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.math.BigDecimal) : Unit =
            ps.setBigDecimal (idx, arg)
    }

    /**
     * Function object to set InputStream parameter with binary data on PreparedStatement.
     */
    object BinaryStreamSetter extends Function3 [PreparedStatement, Int, java.io.InputStream, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
            ps.setBinaryStream (idx, arg)
    }

    /**
     * Function object to set Blob parameter on PreparedStatement.
     */
    object BlobSetter extends Function3 [PreparedStatement, Int, java.sql.Blob, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Blob) : Unit =
            ps.setBlob (idx, arg)
    }

    /**
     * Function object to set InputStream providing data for Blob parameter on PreparedStatement.
     */
    object BlobStreamSetter extends Function3 [PreparedStatement, Int, java.io.InputStream, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
            ps.setBlob (idx, arg)
    }

    /**
     * Function object to set Boolean parameter on PreparedStatement.
     */
    object BooleanSetter extends Function3 [PreparedStatement, Int, Boolean, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Boolean) : Unit =
            ps.setBoolean (idx, arg)
    }

    /**
     * Function object to set byte parameter on PreparedStatement.
     */
    object ByteSetter extends Function3 [PreparedStatement, Int, Byte, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Byte) : Unit =
            ps.setByte (idx, arg)
    }

    /**
     * Function object to set byte array parameter on PreparedStatement.
     */
    object BytesSetter extends Function3 [PreparedStatement, Int, Array[Byte], Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Array[Byte]) : Unit =
            ps.setBytes (idx, arg)
    }

    /**
     * Function object to set Reader parameter with character data on PreparedStatement.
     */
    object CharacterStreamSetter extends Function3 [PreparedStatement, Int, java.io.Reader, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
            ps.setCharacterStream (idx, arg)
    }

    /**
     * Function object to set Clob parameter on PreparedStatement.
     */
    object ClobSetter extends Function3 [PreparedStatement, Int, java.sql.Clob, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Clob) : Unit =
            ps.setClob (idx, arg)
    }

    /**
     * Function object to set Reader providing data for Clob parameter on PreparedStatement.
     */
    object ClobStreamSetter extends Function3 [PreparedStatement, Int, java.io.Reader, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
            ps.setClob (idx, arg)
    }

    /**
     * Function object to set Date parameter on PreparedStatement.
     */
    object SqlDateSetter extends Function3 [PreparedStatement, Int, java.sql.Date, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Date) : Unit =
            ps.setDate (idx, arg)
    }

    /**
     * Function object to set Date parameter on PreparedStatement.
     */
    object DateSetter extends Function3 [PreparedStatement, Int, java.util.Date, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.util.Date) : Unit =
            ps.setDate (idx, new java.sql.Date (arg.getTime))
    }

    /**
     * Function object to set Double parameter on PreparedStatement.
     */
    object DoubleSetter extends Function3 [PreparedStatement, Int, Double, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Double) : Unit =
            ps.setDouble (idx, arg)
    }

    /**
     * Function object to set Float parameter on PreparedStatement.
     */
    object FloatSetter extends Function3 [PreparedStatement, Int, Float, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Float) : Unit =
            ps.setFloat (idx, arg)
    }

    /**
     * Function object to set Int parameter on PreparedStatement.
     */
    object IntSetter extends Function3 [PreparedStatement, Int, Int, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Int) : Unit =
            ps.setInt (idx, arg)
    }

    /**
     * Function object to set Long parameter on PreparedStatement.
     */
    object LongSetter extends Function3 [PreparedStatement, Int, Long, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Long) : Unit =
            ps.setLong (idx, arg)
    }

    /**
     * Function object to set Reader parameter with ncharacter data on PreparedStatement.
     */
    object NCharacterStreamSetter extends Function3 [PreparedStatement, Int, java.io.Reader, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
            ps.setNCharacterStream (idx, arg)
    }

    /**
     * Function object to set NClob parameter on PreparedStatement.
     */
    object NClobSetter extends Function3 [PreparedStatement, Int, java.sql.NClob, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.NClob) : Unit =
            ps.setNClob (idx, arg)
    }

    /**
     * Function object to set Reader providing data for NClob parameter on PreparedStatement.
     */
    object NClobStreamSetter extends Function3 [PreparedStatement, Int, java.io.Reader, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
            ps.setNClob (idx, arg)
    }

    /**
     * Function object to set NString parameter on PreparedStatement.
     */
    object NStringSetter extends Function3 [PreparedStatement, Int, String, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
            ps.setNString (idx, arg)
    }

    /**
     * Function object to set Object parameter on PreparedStatement.
     */
    object ObjectSetter extends Function3 [PreparedStatement, Int, Object, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Object) : Unit =
            ps.setObject (idx, arg)
    }

    /**
     * Function object to set Ref parameter on PreparedStatement.
     */
    object RefSetter extends Function3 [PreparedStatement, Int, java.sql.Ref, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Ref) : Unit =
            ps.setRef (idx, arg)
    }

    /**
     * Function object to set RowId parameter on PreparedStatement.
     */
    object RowIdSetter extends Function3 [PreparedStatement, Int, java.sql.RowId, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.RowId) : Unit =
            ps.setRowId (idx, arg)
    }

    /**
     * Function object to set Short parameter on PreparedStatement.
     */
    object ShortSetter extends Function3 [PreparedStatement, Int, Short, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : Short) : Unit =
            ps.setShort (idx, arg)
    }

    /**
     * Function object to set SQLXML parameter on PreparedStatement.
     */
    object SqlXmlSetter extends Function3 [PreparedStatement, Int, java.sql.SQLXML, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.SQLXML) : Unit =
            ps.setSQLXML (idx, arg)
    }

    /**
     * Function object to set String parameter on PreparedStatement.
     */
    object StringSetter extends Function3 [PreparedStatement, Int, String, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
            ps.setString (idx, arg)
    }

    /**
     * Function object to set Time parameter on PreparedStatement.
     */
    object TimeSetter extends Function3 [PreparedStatement, Int, java.sql.Time, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Time) : Unit =
            ps.setTime (idx, arg)
    }

    /**
     * Function object to set Timestamp parameter on PreparedStatement.
     */
    object TimestampSetter extends Function3 [PreparedStatement, Int, java.sql.Timestamp, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Timestamp) : Unit =
            ps.setTimestamp (idx, arg)
    }

    /**
     * Function object to set Url parameter on PreparedStatement.
     */
    object UrlSetter extends Function3 [PreparedStatement, Int, java.net.URL, Unit] {
        override def apply (ps : PreparedStatement, idx : Int, arg : java.net.URL) : Unit =
            ps.setURL (idx, arg)
    }

    /**
     * Returns setter for the given param type.
     */
    protected def getSetter [Param] (paramJdbcType : JdbcType [Param])
                                        : Function3 [PreparedStatement, Int, Param, Unit] =
        paramJdbcType match {
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
