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
import statement._


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
  *    @tparam Domain type of domain value
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
  *    @tparam Param10 the type of the tenth $paramWillBePassed
  *    @tparam Param11 the type of the eleventh $paramWillBePassed
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
  *    @param param10JdbcType JDBC type object of the tenth parameter
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


    /**
     * Prepared parametrized action with nine parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction10 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                                             -Param7, -Param8, -Param9, -Param10,
                                             Action <: AbstractJdbcAction]
                extends Function10 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                    Param8, Param9, Param10, Action#Result]
                   with PreparedAction [Action]


    /**
     * Prepared parametrized action with nine parameters.
     *
     * $PreparedActionDeclaration
     */
    protected sealed trait PreparedAction11 [-Param1, -Param2, -Param3, -Param4, -Param5, -Param6,
                                             -Param7, -Param8, -Param9, -Param10, -Param11,
                                             Action <: AbstractJdbcAction]
                extends Function11 [Param1, Param2, Param3, Param4, Param5, Param6, Param7,
                                    Param8, Param9, Param10, Param11, Action#Result]
                   with PreparedAction [Action]


    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // =====================================================================================
    // prepare method

    /**
     * Prepare parameterless and domainless action.
     *
     * $prepareMethod
     */
    protected final def prepare [Action <: AbstractJdbcAction]
                                (action : Action, stmt : Statement0 [Domainless], dummy : Null = null)
                                    : PreparedAction0 [Action] =
        new {
            protected override val jdbcAction = action
            protected override val statement = stmt
        } with PreparedAction0 [Action] {
            override def apply () : Result = {
                runAction ()
            }
        }


    /**
     * Prepare parameterless action for the given statement with domain type.
     *
     * $prepareMethod
     */
/*    protected final def prepare [Action <: AbstractJdbcAction, Domain]
                                (action : Action, stmt : Statement0 [Domain])
                                    : PreparedAction1 [Domain, Action] =
        new {
            protected override val jdbcAction = action
            protected override val statement = stmt
        } with PreparedAction1 [Domain, Action] {
            private val domainSetter = createDomainSetter (stmt)

            override def apply (domain : Domain) : Result = {
                val jdbcPS = getPreparedStatement ()
                domainSetter (jdbcPS, domain)
                runAction ()
            }
        }
*/

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
         * Action definition. Should be set using early initialization. Because
         * traits do not support parameters we use this way to customize trait behavior.
         */
        protected val jdbcAction : Action

        /**
         * Statement definition
         */
        protected val statement : Statement [_]

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
            val jdbcPS = createAndInitPreparedStatement (connection, jdbcAction, statement)

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
                             : PreparedBatchAction =
        new PreparedBatchAction (preparedAction)

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
            if (isBatchDirty) {
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
 * Companion object for AbstractJdbcActor.
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
    def createAndInitPreparedStatement (conn : Connection,
                                        action : AbstractJdbcAction,
                                        statement : Statement [_]) : PreparedStatement =
    {
        // Create
        val jdbcPS = conn.prepareStatement (statement.sql)

        // Init
        def setProvidedValue [Value] (pv : Statement.ProvidedValue [Value, AbstractJdbcType [Value]]) {
            val setter = getSetterForJdbcType (pv.jdbcType)
            setter (jdbcPS, pv.position, pv.value)
        }

        for (pv <- statement.providedValues) {
            setProvidedValue (pv)
        }

        // Return result
        jdbcPS
    }


    // ==========================================================================================
    // Domain setter


    /**
     * Type represents a function which sets given domain value(s) on the given prepared
     * statement.
     */
    type DomainSetter [Domain] = Function2 [PreparedStatement, Domain, Unit]


    /**
     * Constructs a new domain setter for the given domain statement. Constructed function
     * accepts JDBC prepared statement and the domain object. When called, this function
     * sets values for domain placeholders of the given statement on the given prepared statement.
     */
    def createDomainSetter [Domain] (statement : Statement [Domain]) : DomainSetter [Domain] =
    {
        // Makes a new domain setter for the given domain placeholder.
        def mkSetter [Value] (ph : Statement.DomainPlaceholder [Domain, Value, AbstractJdbcType [Value]])
                                                            : DomainSetter [Domain] =
        {
            // Get JDBC setter in advance
            val setter = getSetterForJdbcType (ph.jdbcType)

            // Return function which will set placeholder value using 'setter'
            (jdbcPS : PreparedStatement, domain : Domain) => setter (jdbcPS, ph.position, ph.f (domain))
        }

        // Make a collection of domain setters (one for each domain placeholder). This action is done
        // in advance to save time during domain setter invokation.
        val setters = statement.domainPlaceholders.map (mkSetter (_))

        // Return function which will invoke each setter from 'setters' collection
        (jdbcPS : PreparedStatement, domain : Domain) => setters.foreach (_ (jdbcPS, domain))
    }
}
