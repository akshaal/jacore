/*
 * JacoreManager.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system

import com.google.inject.{Singleton, Inject}

import fs.FileActor
import daemon.DaemonStatusActor
import actor.{ActorManager, MonitoringActors, Actor}
import scheduler.Scheduler

@Singleton
final class JacoreManager @Inject() (
                    fileActor : FileActor,
                    daemonStatusActor : DaemonStatusActor,
                    monitoringActors : MonitoringActors,
                    actorManager : ActorManager,
                    scheduler : Scheduler
                )
{
    private[this] var stopped = false
    private[this] var started = false

    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Useful addons

    def startActor (actor : Actor) = {
        actorManager.startActor (actor)
    }

    def startActors (it : Iterable[Actor]) = {
        it.foreach (startActor (_))
    }

    def stopActor (actor : Actor) = {
        actorManager.stopActor (actor)
    }


    def stopActors (it : Iterable[Actor]) = {
        it.foreach (stopActor (_))
    }

    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Init code

    // Actors
    private[this] val actors =
        (fileActor
         :: daemonStatusActor
         :: monitoringActors.monitoringActor1
         :: monitoringActors.monitoringActor2
         :: Nil)

    lazy val start : Unit = {
        require (!stopped,
            "Unable to start JacoreManager. JacoreManager has been stopped")

        // Set flags
        started = true

        // Start scheduling
        scheduler.start ()

        // Start actors
        startActors (actors)
    }

    lazy val stop : Unit = {
        require (started,
                 "Unable to stop JacoreManager. JacoreManager is not started")

        // Set flags
        stopped = true

        // Stop scheduling
        scheduler.shutdown ()

        // Stop actors
        stopActors (actors)
    }
}
