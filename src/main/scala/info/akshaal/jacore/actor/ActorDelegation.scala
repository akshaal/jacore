/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package actor

import java.util.concurrent.{Future, FutureTask, Callable}
import java.util.concurrent.atomic.AtomicBoolean

/**
 * This trait provides a way for actor to delegate a code execution by passing
 * code in a message.
 */
trait ActorDelegation {
    this : Actor =>

    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // Postpone support

    /**
     * Execute code later on (after all currently queued message are processed).
     *
     * @param code code to be executed later
     */
    protected def postponed (code : => Unit) : Unit = {
        this ! (PostponedBlock (() => code))
    }

    /**
     * Message with postponed code.
     */
    protected case class PostponedBlock (code : () => Unit)

    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // -----------------------------------------------------------------------------
    // Operation

    import Operation._

    /**
     * Implicit value that will be used by <code>OperationWithResult<code> to
     * apply result to a result matching function. This values is supposed to be passed
     * along with the result matching function to an actor's method which will generate
     * some kind of result. The <code>ResultMatchApplier</code> submits execution of the
     * result matching function to the actor it belongs to.
     */
    protected implicit val thisActorResultApplier =
        new ResultApplier {
            def apply [A] (resultMatch : A => Unit, result : A) : Unit = {
                postponed {
                    resultMatch (result)
                }
            }
        }

    /**
     * Represents an operation which produce a result of type <code>A</code>.
     * Operation code is supposed to be defined in <code>processRequest</code> method.
     *
     * @tparam A type of result
     */
    protected abstract class AbstractOperation [A] extends WithComplexResult [A]
    {
        private[this] val started = new AtomicBoolean
        private[this] val yielded = new AtomicBoolean
        private[this] var resultHandler : A => Unit = null

        /**
         * This method can be called only once for each instance of this object.
         * If the method is called first time, then code passed as argument is postponed
         * to be executed by actor as a message.
         * If the method is called the second time, then UnrecoverableError is casted.
         *
         * @param code code to run once
         */
        private[this] def onceAndPostponed (code : => Unit) : Unit = {
            if (started.compareAndSet (false, true)) {
                postponed {
                    code
                }
            } else {
                throw new UnrecoverableError ("Same operation can'be executed more than once!")
            }
        }

        /**
         * Method to be called to run operation.
         *
         * @param matcher function that will receive result by applier when operation is over
         * @param applier object that applies matcher to a result. An implementation of applier
         *                is supposed to run matcher by message to caller actor.
         */
        override final def runMatchingResultAsy (matcher : A => Unit)
                                                (implicit applier : ResultApplier) : Unit =
        {
            onceAndPostponed {
                this.resultHandler = applier (matcher, _)
                processRequest ()
            }
        }

        /**
         * Run operation. Result of operation will be provided in the future object
         * when operation is done.
         *
         * @return future object that can be used to get result of operation
         */
        override final def runWithFutureAsy () : Future [A] = {
            val future = new SettableFuture [A]

            onceAndPostponed {
                this.resultHandler = future.set
                processRequest ()
            }

            future
        }

        /**
         * Body of the operation. Executed by message.
         */
        protected def processRequest () : Unit

        /**
         * Yield result of operation.
         */
        protected final def yieldResult (result : A) : Unit = {
            if (yielded.compareAndSet (false, true)) {
                this.resultHandler (result)
            } else {
                throw new UnrecoverableError ("Result can't be yielded more than once!")
            }
        }
    }
}

// ////////////////////////////////////////////////////////////////////////////////////////
// ////////////////////////////////////////////////////////////////////////////////////////
// ////////////////////////////////////////////////////////////////////////////////////////
// Operation object. This must be located in the same file as ActorDelegation trait.

object Operation {
    /**
     * Applier of operation results to result matchers.
     */
    sealed trait ResultApplier {
        // NOTE: This trait is sealed because we want only one implementation. Only code
        // from this file is trusted because the whole idea is to prohibit execution of arbitrary
        // code in context of arbitrary actor. This is what can happen if a new implementation
        // of this class are allowed. That is why we don't allow it. The only implementation
        // is in Actor class and it is only available from inside the Actor class. So any
        // actor can use only its implementation and no one elses.

        def apply [A] (resultMatch : A => Unit, result : A) : Unit
    }

    /**
     * Encapsulate asynhcronous operation with some result of complex type.
     *
     * @tparam A type of result
     */
    sealed trait WithComplexResult [A] {
        /**
         * Method to be called to run operation.
         *
         * @param matcher function that will receive result by applier when operation is over
         * @param applier object that applies matcher to a result. An implementation of applier
         *                is supposed to run matcher by message to caller actor.
         */
        def runMatchingResultAsy (matcher : A => Unit) (implicit applier : ResultApplier)

        /**
         * Run operation passing result of computation as a future object. The operation can't
         * be interrupted.
         *
         * @return future object that can be used to get results of computation.
         */
        def runWithFutureAsy () : Future [A]
    }

    /**
     * Encapsulate asyncronous operation with a result..
     */
    type WithResult [A] = WithComplexResult [Result [A]]

    /**
     * Dummy callable.
     */
    private[actor] object DummyCallable extends Callable[Object] {
        def call () : Object = null
    }

    /**
     * The same as FutureTask but set method is exposed for public.
     */
    private[actor] class SettableFuture[A]
                            extends FutureTask[A] (DummyCallable.asInstanceOf[Callable[A]])
    {
        /**
         * Set the result of computation.
         *
         * @param result result
         */
        override def set (result : A) : Unit = {
            super.set (result)
        }
    }
}
