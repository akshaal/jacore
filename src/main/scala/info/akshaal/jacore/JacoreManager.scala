/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore

import java.lang.{Iterable => JavaIterable}

import collection.JavaConversions._

import io.fs.{TextFileServiceActor, FileServiceActor}
import daemon.{DaemonStatusActor, DaemonStatus}
import actor.{MonitoringActors, BroadcasterActor, Actor}
import scheduler.SchedulerImpl
import logger.Logging

/**
 * Manager for an instance of jacore framework.
 */
trait JacoreManager {
    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Useful addons

    /**
     * Convenient method to help start actors.
     * @param actors variable argument parameters
     */
    def startActors (actors : Actor*) : Unit

    /**
     * Convenient method to help start actors.
     * @param it iterable object
     */
    def startActors (it : Iterable[Actor]) : Unit

    /**
     * Convenient method to help start actors.
     * @param it iterable object
     */
    def startActors (it : JavaIterable[Actor]) : Unit

    /**
     * Convenient method to help stop actors.
     * @param actors variable argument parameters
     */
    def stopActors (actors : Actor*) : Unit

    /**
     * Convenient method to help stop actors.
     * @param it iterable object
     */
    def stopActors (it : Iterable[Actor]) : Unit

    /**
     * Convenient method to help stop actors.
     * @param it iterable object
     */
    def stopActors (it : JavaIterable[Actor]) : Unit

    /**
     * Start instance of jacore.
     */
    def start : Unit

    /**
     * Stop instance of jacore.
     */
    def stop : Unit
}

/**
 * Manager for an instance of jacore framework. Implementation.
 */
@Singleton
private[jacore] final class JacoreManagerImpl @Inject() (
                    fileServiceActor : FileServiceActor,
                    textFileServiceActor : TextFileServiceActor,
                    daemonStatusActor : DaemonStatusActor,
                    daemonStatus : DaemonStatus,
                    monitoringActors : MonitoringActors,
                    broadcasterActor : BroadcasterActor,
                    scheduler : SchedulerImpl
                ) extends JacoreManager with Logging
{
    private[this] var stopped = false
    private[this] var started = false

    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Useful addons

    /** {@inheritDoc} */
    override def startActors (actors : Actor*) : Unit = {
        startActors (actors)
    }

    /** {@inheritDoc} */
    override def startActors (it : Iterable[Actor]) : Unit = {
        it.foreach (_.start)
    }

    /** {@inheritDoc} */
    override def startActors (it : JavaIterable[Actor]) : Unit = {
        startActors (it : Iterable[Actor])
    }

    /** {@inheritDoc} */
    override def stopActors (actors : Actor*) : Unit = {
        stopActors (actors)
    }

    /** {@inheritDoc} */
    override def stopActors (it : Iterable[Actor]) : Unit = {
        it.foreach (_.stop)
    }

    /** {@inheritDoc} */
    override def stopActors (it : JavaIterable[Actor]) : Unit = {
        stopActors (it : Iterable[Actor])
    }

    // - - - - -- - - - - - - - - - - - - - - - - - - - --
    // Init code

    // Actors
    private[this] val jacoreActors : List[Actor] =
        (   monitoringActors.monitoringActor1
         :: monitoringActors.monitoringActor2
         :: broadcasterActor
         :: fileServiceActor
         :: textFileServiceActor
         :: daemonStatusActor
         :: Nil)

    /** {@inheritDoc} */
    override lazy val start : Unit = {
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

    /** {@inheritDoc} */
    override lazy val stop : Unit = {
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
