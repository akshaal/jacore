/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Extended operations support for actors.
 */
trait ActorExtendedOperations {
    this : Actor =>

    import ActorExtendedOperations._

    // //////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////
    // Restarting operations

    /**
     * Implicit converter from Operation to rerunnable operation.
     */
    protected implicit def operation2rerunnableOperation [A] (
                 newOperation : => Operation.WithComplexResult[A]) : RerunnableActorOperation [A] =
    {
        new RerunnableActorOperation [A] (() => newOperation)
    }

    /**
     * Represents an operation that can be restarted in case of unsatisfying result.
     *
     * @tparam A type of result of operation
     * @param newOperation function to (re)construct operation
     */
    protected class RerunnableActorOperation [A] (newOperation : () => Operation.WithComplexResult[A])
    {
        private[this] val started = new AtomicBoolean

        /**
         * Method to be called to run operation with support for retrying.
         *
         * @param retries list of retries
         * @param filter function that is used to filter result and schedule operation for retry
         * @param matcher function that will receive result by applier when operation is over
         */
        def runMatchingResultAsy (retries : Retries, filter : ResultFilter [A])
                                 (matcher : FilteredResult [A] => Unit)
                                 (implicit resultApplier : Operation.ResultApplier) : Unit =
        {
            if (started.compareAndSet (false, true)) {
                var retriesLeft = retries

                def internalMatcher (result : A) : Unit = {
                    filter (result) match {
                        case ResultAccepted =>
                            resultApplier (matcher, AcceptedResult (result))

                        case ResultRejected =>
                            retriesLeft match {
                                case Nil =>
                                    resultApplier (matcher, RejectedResult)

                                case time :: newRetriesLeft =>
                                    retriesLeft = newRetriesLeft

                                    schedule in time executionOf {
                                        doTry ()
                                    }
                            }
                    }
                }

                def doTry () : Unit = {
                    newOperation ().runMatchingResultAsy (internalMatcher) (thisActorResultApplier)
                }

                doTry ()
            } else {
                throw new UnrecoverableError ("Same operation can'be executed more than once!")
            }
        }
    }
}


// ######################################################################################
// ######################################################################################
// ######################################################################################


object ActorExtendedOperations {
    // //////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////////
    // Restarting operations

    /**
     * Abstract class of results filtering.
     */
    sealed abstract class ResultFilterDecision

    /**
     * Returned by filter when result is considered as accepted.
     */
    case object ResultAccepted extends ResultFilterDecision

    /**
     * Returned by filter when result is rejected and must be retried.
     */
    case object ResultRejected extends ResultFilterDecision

    /**
     * Type of result filter.
     *
     * @tparam A type of values this filter checks
     */
    type ResultFilter [A] = A => ResultFilterDecision

    /**
     * List of retries. Defines times to wait before make next try.
     */
    type Retries = List [TimeValue]

    /**
     * Abstract class for filtered results.
     *
     * @tparam type of filtered result
     */
    sealed abstract class FilteredResult [+A]

    /**
     * Class which encapsulates result accepted by filter.
     */
    final case class AcceptedResult [+A] (result : A) extends FilteredResult [A]

    /**
     * Object which states that all results are rejected by filter.
     */
    final case object RejectedResult extends FilteredResult [Nothing]
}