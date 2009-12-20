/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

import Predefs._

/**
 * This trait provides a way for actor to delegate a code execution by passing
 * code in a message.
 */
trait ActorDelegation {    
    this : Actor =>

    /**
     * A function that must be called by operation implementation to pass result.
     */
    type ResultReceiver[A] = A => Unit

    /**
     * Implicit value that will be used by <code>OperationWithResult<code> to
     * apply result to a result matching function. This values is supposed to be passed
     * along with the result matching function to some an actor's method which will generate
     * some kind of result. The <code>ResultMatchApplier</code> submits execution of the
     * result matching function to the actor it belongs to.
     */
    protected implicit val _ =
        new Operation.ResultMatchApplier {
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
    protected def postponed (reason : String) (code : => Unit) : Unit = {
        this ! (PostponedBlock (reason, () => code))
    }

    /**
     * Creates asynchronous operation with a complex result.
     *
     * @param description of operation
     * @param operationBody body of operation. The body will be executed by-message. Body
     *                      receives resultReceiver and must call it with the result of operation.
     */
    protected def operationWithComplexResult [A] (description : String)
                                                 (operationBody : ResultReceiver[A] => Unit)
                                        : Operation.WithComplexResult [A] =
    {
        new OperationWithResultImpl [A] (description) {
            override protected def processRequest (matcher : A => Unit) : Unit = {
                operationBody (matcher)
            }
        }
    }

    /**
     * Creates asynchronous operation with a result.
     *
     * @param description of operation
     * @param operationBody body of operation. The body will be executed by-message. Body
     *                      receives resultReceiver and must call it with the result of operation.
     */
    protected def operation [A] (description : String)
                                (operationBody : ResultReceiver[Result[A]] => Unit)
                                        : Operation.WithResult [A] =
    {
        operationWithComplexResult [Result[A]] (description) (operationBody)
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
    private abstract class OperationWithResultImpl [A] (description : String)
                                                extends Operation.WithComplexResult [A]
    {
        /**
         * Method to be called to run operation.
         *
         * @param matcher function that will receive result by applier when operation is over
         * @param applier object that applies matcher to a result. An implementation of applier
         *                is supposed to run matcher by message to caller actor.
         */
        final def matchResult (matcher : A => Unit) (implicit applier : Operation.ResultMatchApplier) {
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

object Operation {
    /**
     * Applier of operation results to result matchers.
     */
    sealed trait ResultMatchApplier {
        def apply [A] (resultMatch : A => Unit, result : A) : Unit
    }

    /**
     * Encapsulate asyncronous operation with some result of complex type.
     */
    sealed trait WithComplexResult [A] {
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
     * Encapsulate asyncronous operation with a result..
     */
    type WithResult [A] = WithComplexResult [Result [A]]
}