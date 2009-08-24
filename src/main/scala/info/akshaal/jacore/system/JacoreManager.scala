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

    lazy val start : Unit = {
        // Run actors
        val actors =
            (fileActor
             :: daemonStatusActor
             :: monitoringActors.monitoringActor1
             :: monitoringActors.monitoringActor2
             :: Nil)

        startActors (actors)

        // Start scheduling
        scheduler.start ()
    }
}
