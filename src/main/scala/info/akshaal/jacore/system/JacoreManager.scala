/*
 * JacoreManager.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system

import com.google.inject.{Singleton, Inject}
import java.lang.{Iterable => JavaIterable}

import collection.JavaConversions._

import fs.{TextFileActor}
import daemon.{DaemonStatusActor, DaemonStatus}
import actor.{MonitoringActors, BroadcasterActor, Actor}
import scheduler.Scheduler
import logger.Logging

/**
 * Manager for an instance of jacore framework.
 */
@Singleton
final class JacoreManager @Inject() (
                    textFileActor : TextFileActor,
                    daemonStatusActor : DaemonStatusActor,
                    daemonStatus : DaemonStatus,
                    monitoringActors : MonitoringActors,
                    broadcasterActor : BroadcasterActor,
                    scheduler : Scheduler
                ) extends Logging
{
    private[this] var stopped = false
    private[this] var started = false

    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Useful addons

    /**
     * Convenient method to help start actors.
     * @param actors variable argument parameters
     */
    def startActors (actors : Actor*) : Unit = {
        startActors (actors)
    }

    /**
     * Convenient method to help start actors.
     * @param it iterable object
     */
    def startActors (it : Iterable[Actor]) : Unit = {
        it.foreach (_.start)
    }

    /**
     * Convenient method to help start actors.
     * @param it iterable object
     */
    def startActors (it : JavaIterable[Actor]) : Unit = {
        startActors (it : Iterable[Actor])
    }

    /**
     * Convenient method to help stop actors.
     * @param actors variable argument parameters
     */
    def stopActors (actors : Actor*) : Unit = {
        stopActors (actors)
    }

    /**
     * Convenient method to help stop actors.
     * @param it iterable object
     */
    def stopActors (it : Iterable[Actor]) : Unit = {
        it.foreach (_.stop)
    }

    /**
     * Convenient method to help stop actors.
     * @param it iterable object
     */
    def stopActors (it : JavaIterable[Actor]) : Unit = {
        stopActors (it : Iterable[Actor])
    }

    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Init code

    // Actors
    private[this] val jacoreActors : List[Actor] =
        (   monitoringActors.monitoringActor1
         :: monitoringActors.monitoringActor2
         :: broadcasterActor
         :: textFileActor
         :: daemonStatusActor
         :: Nil)

    /**
     * Start instance of jacore.
     */
    lazy val start : Unit = {
        require (!stopped,
            "Unable to start JacoreManager. JacoreManager has been stopped")

        debug ("Starting Jacore")

        // Set flags
        started = true

        // Start scheduling
        scheduler.start ()

        // Start actors
        startActors (jacoreActors)

        debug ("Jacore started")
    }

    /**
     * Stop instance of jacore.
     */
    lazy val stop : Unit = {
        require (started,
                 "Unable to stop JacoreManager. JacoreManager is not started")

        debug ("Stopping Jacore")

        // Set flags
        stopped = true

        // Stop scheduling
        scheduler.shutdown ()

        // Stop actors
        stopActors (jacoreActors)

        // Unregister daemon status jmx bean
        daemonStatus.unregisterJmxBean

        debug ("Jacore stopped")
    }
}
