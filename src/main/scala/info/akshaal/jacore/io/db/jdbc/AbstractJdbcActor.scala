/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc

import java.sql.{Connection, PreparedStatement}

import actor.{Actor, LowPriorityActorEnv}
import utils.io.db.SqlUtils

import `type`._
import `type`.setter._
import action._

/**
  * Template for all actors that are interested in working with JDBC.
  *
  * @param db database to use for connections
  * @param lowPriorityActorEnv low priority environment for this actor
  *
  * @define lowlevel
  *    This method is very low level and incorrect use may break design of the actor. Don't use
  *    it unless you know what you are doing.
  *
  * @define paramWillBePassed parameter that will be passed to JDBC statement of the action
  *
  * @define CommonTParams
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
    // PreparedAction traits

    /**
     * Prepared parameterless JDBC action.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction0 [+Action <: AbstractJdbcAction]
                extends Function0 [Action#Result] with PreparedAction [Action]

    
    /**
     * Prepared parametrized action with one parameter.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction1 [-Param, +Action <: AbstractJdbcAction]
                extends Function1 [Param, Action#Result] with PreparedAction [Action]


    /**
     * Prepared parametrized action with two parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction2 [-Param1, -Param2, +Action <: AbstractJdbcAction]
                extends Function2 [Param1, Param2, Action#Result] with PreparedAction [Action]


    /**
     * Prepared parametrized action with three parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction3 [-Param1, -Param2, -Param3, +Action <: AbstractJdbcAction]
                extends Function3 [Param1, Param2, Param3, Action#Result]
                   with PreparedAction [Action]


    /**
     * Prepared parametrized action with four parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction4 [-Param1, -Param2, -Param3, -Param4,
                                            +Action <: AbstractJdbcAction]
                extends Function4 [Param1, Param2, Param3, Param4, Action#Result]
                   with PreparedAction [Action]


    /**
     * Prepared parametrized action with five parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction5 [-Param1, -Param2, -Param3, -Param4, -Param5,
                                            +Action <: AbstractJdbcAction]
                extends Function5 [Param1, Param2, Param3, Param4, Param5, Action#Result]
                   with PreparedAction [Action]


    /**
     * Prepared parametrized action with six parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction6 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                                            +Action <: AbstractJdbcAction]
                extends Function6 [Param1, Param2, Param3, Param4, Param5, Param6, Action#Result]
                   with PreparedAction [Action]


    /**
     * Prepared parametrized action with seven parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction7 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                                            -Param7, +Action <: AbstractJdbcAction]
                extends Function7 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                   Action#Result]
                   with PreparedAction [Action]


    /**
     * Prepared parametrized action with eight parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction8 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                                            -Param7, -Param8, +Action <: AbstractJdbcAction]
                extends Function8 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                   Param8, Action#Result] with PreparedAction [Action]


    /**
     * Prepared parametrized action with nine parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction9 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                                            -Param7, -Param8, -Param9, +Action <: AbstractJdbcAction]
                extends Function9 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                   Param8, Param9, Action#Result]
                   with PreparedAction [Action]


    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // prepare method
    
    /**
     * Prepare parameterless action.
     *
     * $prepareMethod
     */
    protected final def prepare [Action <: AbstractJdbcAction] (action : Action)
                            : PreparedAction0 [Action] =
        new {
            protected override val parameterCount = 0
            protected override val jdbcAction = action
        } with PreparedAction0 [Action] {
            override def apply () : Result = {
                runAction ()
            }
        }

    /**
     * Prepare action with one parameter.
     *
     * $prepareMethod
     */
    protected final def prepare [Param, Action <: AbstractJdbcAction] (
                                  action : Action,
                                  paramJdbcType : AbstractJdbcType [Param])
                            : PreparedAction1 [Param, Action] =
        new {
            protected override val parameterCount = 1
            protected override val jdbcAction = action
        } with PreparedAction1 [Param, Action] {
            private val p1setter = AbstractJdbcSetter.getFor (paramJdbcType)

            override def apply (param : Param) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, param)
                runAction ()
            }
        }


    /**
     * Prepare action with two paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2])
                            : PreparedAction2 [Param1, Param2, Action] =
        new {
            protected override val parameterCount = 2
            protected override val jdbcAction = action
        } with PreparedAction2 [Param1, Param2, Action] {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)

            override def apply (p1 : Param1, p2 : Param2) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                runAction ()
            }
        }


    /**
     * Prepare action with three paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Result, Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3])
                            : PreparedAction3 [Param1, Param2, Param3, Action] =
        new {
            protected override val parameterCount = 3
            protected override val jdbcAction = action
        } with PreparedAction3 [Param1, Param2, Param3, Action] {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                runAction ()
            }
        }


    /**
     * Prepare action with four paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Param4, Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3],
                                param4JdbcType : AbstractJdbcType [Param4])
                            : PreparedAction4 [Param1, Param2, Param3, Param4, Action] =
        new {
            protected override val parameterCount = 4
            protected override val jdbcAction = action
        } with PreparedAction4 [Param1, Param2, Param3, Param4, Action] {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)
            private val p4setter = AbstractJdbcSetter.getFor (param4JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4) : Result = {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                runAction ()
            }
        }


    /**
     * Prepare action with five paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Param4, Param5, Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3],
                                param4JdbcType : AbstractJdbcType [Param4],
                                param5JdbcType : AbstractJdbcType [Param5])
                            : PreparedAction5 [Param1, Param2, Param3, Param4, Param5,
                                               Action] =
        new {
            protected override val parameterCount = 5
            protected override val jdbcAction = action
        } with PreparedAction5 [Param1, Param2, Param3, Param4, Param5, Action] {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)
            private val p4setter = AbstractJdbcSetter.getFor (param4JdbcType)
            private val p5setter = AbstractJdbcSetter.getFor (param5JdbcType)

            override def apply (p1 : Param1, p2 : Param2, p3 : Param3, p4 : Param4,
                                p5 : Param5) : Result =
            {
                val jdbcPS = getPreparedStatement ()
                p1setter (jdbcPS, 1, p1)
                p2setter (jdbcPS, 2, p2)
                p3setter (jdbcPS, 3, p3)
                p4setter (jdbcPS, 4, p4)
                p5setter (jdbcPS, 5, p5)
                runAction ()
            }
        }


    /**
     * Prepare action with six paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Param4, Param5, Param6,
                           Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3],
                                param4JdbcType : AbstractJdbcType [Param4],
                                param5JdbcType : AbstractJdbcType [Param5],
                                param6JdbcType : AbstractJdbcType [Param6])
                            : PreparedAction6 [Param1, Param2, Param3, Param4, Param5,
                                               Param6, Action] =
        new {
            protected override val parameterCount = 6
            protected override val jdbcAction = action
        } with PreparedAction6 [Param1, Param2, Param3, Param4, Param5, Param6, Action]
        {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)
            private val p4setter = AbstractJdbcSetter.getFor (param4JdbcType)
            private val p5setter = AbstractJdbcSetter.getFor (param5JdbcType)
            private val p6setter = AbstractJdbcSetter.getFor (param6JdbcType)

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
                runAction ()
            }
        }


    /**
     * Prepare action with seven paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                           Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3],
                                param4JdbcType : AbstractJdbcType [Param4],
                                param5JdbcType : AbstractJdbcType [Param5],
                                param6JdbcType : AbstractJdbcType [Param6],
                                param7JdbcType : AbstractJdbcType [Param7])
                            : PreparedAction7 [Param1, Param2, Param3, Param4, Param5, Param6,
                                               Param7, Action] =
        new {
            protected override val parameterCount = 7
            protected override val jdbcAction = action
        } with PreparedAction7 [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Action]
        {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)
            private val p4setter = AbstractJdbcSetter.getFor (param4JdbcType)
            private val p5setter = AbstractJdbcSetter.getFor (param5JdbcType)
            private val p6setter = AbstractJdbcSetter.getFor (param6JdbcType)
            private val p7setter = AbstractJdbcSetter.getFor (param7JdbcType)

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
                runAction ()
            }
        }


    /**
     * Prepare action with eight paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Param8,
                           Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3],
                                param4JdbcType : AbstractJdbcType [Param4],
                                param5JdbcType : AbstractJdbcType [Param5],
                                param6JdbcType : AbstractJdbcType [Param6],
                                param7JdbcType : AbstractJdbcType [Param7],
                                param8JdbcType : AbstractJdbcType [Param8])
                            : PreparedAction8 [Param1, Param2, Param3, Param4, Param5, Param6,
                                               Param7, Param8, Action] =
        new {
            protected override val parameterCount = 8
            protected override val jdbcAction = action
        } with PreparedAction8 [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Param8,
                                Action]
        {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)
            private val p4setter = AbstractJdbcSetter.getFor (param4JdbcType)
            private val p5setter = AbstractJdbcSetter.getFor (param5JdbcType)
            private val p6setter = AbstractJdbcSetter.getFor (param6JdbcType)
            private val p7setter = AbstractJdbcSetter.getFor (param7JdbcType)
            private val p8setter = AbstractJdbcSetter.getFor (param8JdbcType)

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
                runAction ()
            }
        }


    /**
     * Prepare action with nine paremters.
     *
     * $prepareMethod
     */
    protected final def prepare [Param1, Param2, Param3, Param4, Param5, Param6, Param7, Param8,
                                 Param9, Action <: AbstractJdbcAction] (
                                action : Action,
                                param1JdbcType : AbstractJdbcType [Param1],
                                param2JdbcType : AbstractJdbcType [Param2],
                                param3JdbcType : AbstractJdbcType [Param3],
                                param4JdbcType : AbstractJdbcType [Param4],
                                param5JdbcType : AbstractJdbcType [Param5],
                                param6JdbcType : AbstractJdbcType [Param6],
                                param7JdbcType : AbstractJdbcType [Param7],
                                param8JdbcType : AbstractJdbcType [Param8],
                                param9JdbcType : AbstractJdbcType [Param9])
                            : PreparedAction9 [Param1, Param2, Param3, Param4, Param5, Param6,
                                               Param7, Param8, Param9, Action] =
        new {
            protected override val parameterCount = 9
            protected override val jdbcAction = action
        } with PreparedAction9 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                Param8, Param9, Action]
        {
            private val p1setter = AbstractJdbcSetter.getFor (param1JdbcType)
            private val p2setter = AbstractJdbcSetter.getFor (param2JdbcType)
            private val p3setter = AbstractJdbcSetter.getFor (param3JdbcType)
            private val p4setter = AbstractJdbcSetter.getFor (param4JdbcType)
            private val p5setter = AbstractJdbcSetter.getFor (param5JdbcType)
            private val p6setter = AbstractJdbcSetter.getFor (param6JdbcType)
            private val p7setter = AbstractJdbcSetter.getFor (param7JdbcType)
            private val p8setter = AbstractJdbcSetter.getFor (param8JdbcType)
            private val p9setter = AbstractJdbcSetter.getFor (param9JdbcType)

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
                runAction ()
            }
        }

    
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // Common PreparedAction trait

    /**
     * Abstract prepared action to be used to access database.
     * 
     * Call it as a function to perform requested action.
     *
     * @tparam Action type of JDBC action that this prepared action is supposed to perform
     */
    protected sealed trait PreparedAction [+Action <: AbstractJdbcAction] {
        /**
         * Alias for result type of action.
         */
        protected final type Result = Action#Result

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
        protected val jdbcAction : Action

        /**
         * Function to be used to axecute action. Action runner receives prepared
         * statement and runs jdbc action that this action runner is responsible for.
         */
        private val actionRunner : ActionRunner = null

        /**
         * Reference to the JDBC's prepared statement.
         */
        private var jdbcPsOption : Option [PreparedStatement] = None

        /**
         * True indicates that somthing was added to batch but not executed yet.
         * Should only be used by routines from this file only.
         */
        private[AbstractJdbcActor] var batchDirty : Boolean = false

        /**
         * Execute action that is associated with this prepared action.
         * This method is supposed to be called at the and of 'apply' method implementation
         * when all parameters are set.
         *
         * @param jdbcPS JDBC PreparedStatement that was previouslyy (at the beginning of
         *               the 'apply' method) obtained using {getPreparedStatement} method.
         */
        @inline
        private[AbstractJdbcActor] final def runAction () : Result = actionRunner ()

        /**
         * Returns JDBC PreparedStatement if it already created for this action.
         * Otherwise returns None.
         *
         * $lowlevel
         */
        @inline
        final def getPreparedStatementIfAny () : Option [PreparedStatement] = jdbcPsOption

        /**
         * Return JDBC PreparedStatement object associated with this PreparedAction.
         *
         * Createss a new PreparedStatement if nothing was associated so far.
         *
         * $lowlevel
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
         * Construct and associate a new PreparedStatement with this prepared action and actor.
         */
        private def associateNewPreparedStatement () : Unit = {
            assert (jdbcPsOption.isEmpty)

            val connection = getConnection ()
            val jdbcPS = newPreparedStatement (connection, jdbcAction)

            jdbcPsOption = Some (jdbcPS)
        }

        /**
         * Reset prepared action do its initial state.
         *
         * Underlying prepared statement gets closed as well.
         * Calling this method may result in losing uncommitting changes, batches...
         * 
         * $lowlevel
         */
        final def reset () : Unit = {
            batchDirty = false

            // Close underlying prepared statement
            for (jdbcPS <- jdbcPsOption) {
                // First forget about the underlying prepared statement
                jdbcPsOption = None

                // Now close it
                jdbcPS.close ()
            }
        }

        // ---------------------------------------------------------------------------
        // Action runners

        /**
         * ActionRunner is a function that receives JDBC PreparedStatement and executes
         * some action upon it.
         *
         * All JDBC parameters must be set action runnder is called
         *
         * @tparam Result type of action result.
         */
        sealed trait ActionRunner extends Function0 [Result]


        // ---------------------------------------------------------------------------
        // Initialization of prepared action

        // Validate sql parameters. Throws [[java.lang.IllegalArgumentException]] if
        // given action is invalid.
        if (jdbcAction.validate) {
            val foundParameterCount = SqlUtils.countPlaceholders (jdbcAction.statement)

            if (parameterCount != foundParameterCount) {
                throw new IllegalArgumentException (
                    "Given jdbc statement (" + jdbcAction.statement + ") expected to have "
                    + parameterCount + " parameters, but " + foundParameterCount + " found!")
            }
        }
    }

    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // Specialized PreparedActions wrappers for different JDBC Actions

    /**
     * Implicit method that creates specialized version of prepared action with methods
     * suitable for batch action.
     */
    protected implicit def toPreparedBatchAction (preparedAction : PreparedAction [JdbcBatch])
                             : PreparedBatchAction = new PreparedBatchAction (preparedAction)

    /**
     * Class provides additional methods for PreparedAction of Batch type.
     *
     * @param preparedAction prepared action of batch type
     */
    protected final class PreparedBatchAction (preparedAction : PreparedAction [JdbcBatch]) {
        /**
         * Check if batch statement was added but not yet executed.
         *
         * @return true if there is batch to execute
         */
        @inline
        def isBatchDirty () : Boolean = preparedAction.batchDirty

        /**
         * Execute batch.
         *
         * If no batch is found then returns empty array.
         *
         * @see PreparedStatement.executeBatch for more information
         */
        def executeBatch () : Array [Int] = {
            if (preparedAction.batchDirty) {
                // Execute batch
                val jdbcPS = preparedAction.getPreparedStatement ()
                val result = jdbcPS.executeBatch ()
                jdbcPS.clearBatch ()

                // No longer dirty
                preparedAction.batchDirty = false

                // Return result
                result
            } else {
                Array.empty
            }
        }
    }
}

// =============================================================================================
// =============================================================================================
// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =
// =============================================================================================
// =============================================================================================
// Companion object

/**
 * Helper object for AbstractJdbcActor.
 */
private[jdbc] object AbstractJdbcActor {
    /**
     * Create new prepared statement using the given connection for the given action.
     * All initialization for the actions is supposed to be done in this method.
     *
     * @param conn JDBC connection
     * @param action action to construct prepared statement for
     * @return created prepared statement
     */
    def newPreparedStatement (conn : Connection, action : AbstractJdbcAction) : PreparedStatement = {
        conn.prepareStatement (action.statement)
    }
}