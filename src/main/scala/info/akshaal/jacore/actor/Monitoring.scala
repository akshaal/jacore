/** Akshaal (C) 2009. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package actor

import scala.collection.mutable.HashSet
import scala.collection.mutable.Set

import Predefs._
import annotation.Act
import daemon.DaemonStatus
import scheduler.UnfixedScheduling

@Singleton
private[jacore] final class MonitoringActors @Inject() (
                        val monitoringActor1 : MonitoringActor,
                        val monitoringActor2 : MonitoringActor)

private[actor] case object Ping extends NotNull
private[actor] case object Pong extends NotNull

/**
 * Implementation of monitoring actor.
 */
private[jacore] final class MonitoringActor @Inject() (
                     normalPriorityActorEnv : NormalPriorityActorEnv,
                     @Named("jacore.monitoring.interval") interval : TimeUnit,
                     daemonStatus : DaemonStatus)
            extends Actor (actorEnv = normalPriorityActorEnv)
            with UnfixedScheduling
{
    private val currentActors : Set[Actor] = new HashSet[Actor]
    private var monitoringActors : Set[Actor] = new HashSet[Actor]
    private var pingSentAt : TimeUnit = 0.nanoseconds

    schedule every interval executionOf monitor ()

    /**
     * This method is called when an actor has been started. Used to start monitoring
     * for the given actor.
     */
    @Act (subscribe = true)
    def handleStartedActor (event : ActorStartedEvent) : Unit = {
        currentActors += event.actor
    }

    /**
     * This method is called when an actor has been started. Used to start monitoring
     * for the given actor.
     */
    @Act (subscribe = true)
    def handleStoppedActor (event : ActorStoppedEvent) : Unit = {
        currentActors -= event.actor
    }

    /**
     * Process messages.
     */
    override def act () = {
        case Pong => sender.foreach (actor => monitoringActors -= actor)
    }

    /**
     * Do monitoring.
     */
    private[this] def monitor () = {
        // Check currently monitoring actors
        val notResponding = monitoringActors.filter (currentActors.contains (_))

        if (notResponding.isEmpty) {
            debug ("Actors are OK")
            daemonStatus.monitoringAlive
        } else {
            val diff = System.nanoTime.nanoseconds - pingSentAt

            error ("There are actors not responding: " + notResponding + ": for " + diff)
            daemonStatus.die
        }

        // Start monitoring current set of actors
        monitoringActors = currentActors.clone
        monitoringActors.foreach (_ ! Ping)
        pingSentAt = System.nanoTime.nanoseconds
    }
}
