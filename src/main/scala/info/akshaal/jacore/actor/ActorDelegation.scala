/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

/**
 * This trait provides a way for actor to delegate a code execution by passing
 * code in a message.
 */
trait ActorDelegation {    
    this : Actor =>

    /**
     * Implicit value that will be used by <code>OperationWithResult<code> to
     * apply result to a result matching function. This values is supposed to be passed
     * along with the result matching function to some an actor's method which will generate
     * some kind of result. The <code>ResultMatchApplier</code> submits execution of the
     * result matching function to the actor it belongs to.
     */
    protected implicit val _ =
        new ResultMatchApplier {
            def apply [A] (resultMatch : A => Unit, result : A) : Unit = {
                postponed ("applying result") {
                    resultMatch (result)
                }
            }
        }

    /**
     * Execute code later on (after all currently queued message are processed).
     *
     * @param reason describe why the code must be postponed
     * @param code code to be executed later
     */
    def postponed (reason : String) (code : => Unit) : Unit = {
        this ! (PostponedBlock (reason, () => code))
    }

    /**
     * Message with postponed code.
     */
    protected case class PostponedBlock (reason : String, code : () => Unit)

    /**
     * Represents an operation which produce a result of type <code>A</code>.
     * Operation code is supposed to be defined in <code>processRequest</code> method.
     *
     * @param description text description of operation, used for debug only
     */
    abstract class OperationWithResultImpl [A] (description : String)
                                                extends actor.OperationWithResult [A]
    {
        /**
         * Method to be called to run operation.
         *
         * @param matcher function that will receive result by applier when operation is over
         * @param applier object that applies matcher to a result. An implementation of applier
         *                is supposed to run matcher by message to caller actor.
         */
        final def matchResult (matcher : A => Unit) (implicit applier : ResultMatchApplier) {
            postponed (description) {
                processRequest (applier (matcher, _))
            }
        }

        /**
         * Body of the operation. Execute by message.
         */
        protected def processRequest (matcher : A => Unit) : Unit
    }
}

sealed trait OperationWithResult [A] {
    /**
     * Method to be called to run operation.
     *
     * @param matcher function that will receive result by applier when operation is over
     * @param applier object that applies matcher to a result. An implementation of applier
     *                is supposed to run matcher by message to caller actor.
     */
    def matchResult (matcher : A => Unit) (implicit applier : ResultMatchApplier)
}

/**
 * Applier of operation results to result matchers.
 */
sealed trait ResultMatchApplier {
    def apply [A] (resultMatch : A => Unit, result : A) : Unit
}