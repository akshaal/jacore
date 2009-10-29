/** Akshaal (C) 2009. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.system
package actor

import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named

import scala.collection.mutable.HashSet
import scala.collection.mutable.Set

import annotation.Act
import daemon.DaemonStatus
import scheduler.{UnfixedScheduling, TimeOut}
import utils.TimeUnit

@Singleton
private[system] final class MonitoringActors @Inject() (
                        val monitoringActor1 : MonitoringActor,
                        val monitoringActor2 : MonitoringActor)

private[actor] case object Ping extends NotNull
private[actor] case object Pong extends NotNull
private[actor] case object Monitor extends NotNull

/**
 * Implementation of monitoring actor.
 */
private[system] final class MonitoringActor @Inject() (
                     normalPriorityActorEnv : NormalPriorityActorEnv,
                     @Named("jacore.monitoring.interval") interval : TimeUnit,
                     daemonStatus : DaemonStatus)
            extends Actor (actorEnv = normalPriorityActorEnv)
            with UnfixedScheduling
{
    schedule payload Monitor every interval

    private val currentActors : Set[Actor] = new HashSet[Actor]
    private var monitoringActors : Set[Actor] = new HashSet[Actor]

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
        case TimeOut (Monitor) => monitor

        case Pong => sender.foreach (actor => monitoringActors -= actor)
    }

    /**
     * Do monitoring.
     */
    private[this] def monitor () = {
        // Check currently monitoring actors
        val notResponding =
                monitoringActors.filter (currentActors.contains (_))

        if (notResponding.isEmpty) {
            debug ("Actors are OK")
            daemonStatus.monitoringAlive
        } else {
            error ("There are actors not responding: " + notResponding)
            daemonStatus.die
        }

        // Start monitoring current set of actors
        monitoringActors = currentActors.clone
        monitoringActors.foreach (_ ! Ping)
    }
}
