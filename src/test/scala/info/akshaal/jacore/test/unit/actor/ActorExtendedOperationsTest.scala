/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.actor

import unit.UnitTestHelper._
import actor.{ActorExtendedOperations, Actor, Operation}

class ActorExtendedOperationsTest extends JacoreSpecWithJUnit ("ActorExtendedOperations specification") {
    import ActorExtendedOperationsTest._

    "ActorExtendedOperations" should {
        "support restarting of operations" in {
            withStartedActors [RestartingOperationActor,
                               UnreliableOperationActor] ((restartingActor, unreliableActor) => {
                // Check that all attempts are made but it failed nevertheless
                val rejectionStarted = System.currentTimeMillis
                unreliableActor.forceFailure = true
                restartingActor.filteredOuts = Nil
                restartingActor.isFailure (unreliableActor).runWithFutureAsy.get must_== rejected
                (System.currentTimeMillis - rejectionStarted) must beIn (680 to 1000)
                restartingActor.filteredOuts  must_==  List (failure, failure, failure)

                // Test for last hope (all failures, except the last one)
                unreliableActor.forceFailure = true
                restartingActor.filteredOuts = Nil
                val lastHopeFuture = restartingActor.isFailure (unreliableActor).runWithFutureAsy
                Thread.sleep (200)
                restartingActor.filteredOuts must_== List (failure)
                Thread.sleep (400)
                restartingActor.filteredOuts must_== List (failure, failure)
                unreliableActor.forceFailure = false
                Thread.sleep (300)
                restartingActor.filteredOuts must_== List (success, failure, failure)
                lastHopeFuture.get  must_==  success

                // Test for instant success
                unreliableActor.forceFailure = false
                restartingActor.filteredOuts = Nil
                val instantSuccessFuture = restartingActor.isFailure (unreliableActor).runWithFutureAsy
                Thread.sleep (100)
                restartingActor.filteredOuts must_== List (success)
                instantSuccessFuture.get  must_==  success

                // First retry success
                unreliableActor.forceFailure = true
                restartingActor.filteredOuts = Nil
                val firstRetryFuture = restartingActor.isFailure (unreliableActor).runWithFutureAsy
                Thread.sleep (200)
                restartingActor.filteredOuts must_== List (failure)
                unreliableActor.forceFailure = false
                Thread.sleep (400)
                restartingActor.filteredOuts must_== List (success, failure)
                firstRetryFuture.get  must_==  success
            })
        }
    }
}

object ActorExtendedOperationsTest {
    import ActorExtendedOperations._

    val failure : Result[Unit] = Failure ("Failed")
    val success : Result[Unit] = Success [Unit] ()
    val rejected : Result[Unit] = Failure ("rejected")

    class RestartingOperationActor extends TestActor with ActorExtendedOperations {
        private val retries = List (400 milliseconds, 300 milliseconds)
        var filteredOuts : List[Result[Unit]] = Nil

        def filter (result : Result[Unit]) : ResultFilterDecision = {
            filteredOuts ::= result
            result match {
                case Failure (_, _) => ResultRejected
                case Success (_)    => ResultAccepted
            }
        }

        def isFailure (unreliableActor : UnreliableOperationActor) : Operation.WithResult [Unit] = {
            new AbstractOperation [Result[Unit]] {
                override def processRequest () {
                    unreliableActor.isFailure ().runMatchingResultAsy (retries, filter _) {
                        case AcceptedResult (result) =>
                            yieldResult (result)

                        case RejectedResult =>
                            yieldResult  (rejected)
                    }
                }
            }
        }
    }

    class UnreliableOperationActor extends TestActor {
        var forceFailure = false

        def isFailure () : Operation.WithResult [Unit] = {
            new AbstractOperation [Result[Unit]] {
                override def processRequest () {
                    yieldResult (if (forceFailure) failure else success)
                }
            }
        }
    }
}