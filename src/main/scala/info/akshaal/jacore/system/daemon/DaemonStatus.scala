/*
 * DeamonStatus.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package daemon

import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named

import collection.immutable.{List, Nil}
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

import Predefs._
import logger.DummyLogging
import jmx.{SimpleJmx, JmxAttr, JmxOper}

@Singleton
final class DaemonStatus @Inject() (
                 @Named("jacore.status.jmx.name") val jmxObjectName : String)
              extends DummyLogging with SimpleJmx
{
    @volatile
    private var shuttingDown = false

    @volatile
    private var dying = false
    
    @volatile
    private var lastAliveTimestamp = System.nanoTime.nanoseconds

    /**
     * List of exposed JMX attributes.
     */
    override protected lazy val jmxAttributes = List (
        JmxAttr ("dying",           Some (() => dying),          None),
        JmxAttr ("shuttingDown",    Some (() => shuttingDown),   None)
    )

    /**
     * List of exposed JMX operations.
     */
    override protected lazy val jmxOperations = List (
        JmxOper ("shutdown", () => shutdown)
    )

    /**
     * Holds information about a time when daemon was started
     */
    final val startedAt = System.nanoTime.nanoseconds

    /**
     * Returns true if application is dying (feels bad).
     */
    final def isDying = dying

    /**
     * Returns true if the application is shutting down.
     */
    final def isShuttingDown = shuttingDown

    /**
     * Returns timestamp when the application was alive last time.
     */
    final def lastAlive = lastAliveTimestamp

    /**
     * Called by monitoring actor to set
     */
    final def monitoringAlive () = lastAliveTimestamp = System.nanoTime.nanoseconds

    /**
     * Called when application is no more reliable and must die.
     */
    final lazy val die : Unit = {
        // Mark as dying
        dying = true

        // Dying
        error ("Soon will die, but first... postmortum information:")
        dumpThreads
        
        // Shutdown gracefully if possible
        shutdown
    }

    /**
     * Called when shutdown is requested.
     */
    final lazy val shutdown = {
        info ("Shutdown requested. Shutting down...")
        shuttingDown = true
    }

    /**
     * Dump threads stack.
     */
    private def dumpThreads () = {
        val traces : Map[Thread, Array[StackTraceElement]] =
                        Thread.getAllStackTraces ()

        var threadDumpList : List[String] = Nil
        for ((thread, stackTraceElements) <- traces) {
            val name = thread.getName
            val stack = stackTraceElements.mkString (",\n    ")

            threadDumpList = (name + ":\n    " + stack + "\n") :: threadDumpList
        }

        error ("Dumping threads:\n" + threadDumpList.mkString ("\n"))
    }
}