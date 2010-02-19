/*
 * DeamonStatus.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package daemon

import collection.immutable.{List, Nil}
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

import logger.DummyLogging
import jmx.{SimpleJmx, JmxOper, JmxAttr}

@Singleton
final class DaemonStatus @Inject() (
                 @Named ("jacore.status.jmx.name") val jmxObjectName : String,
                 @Named ("jacore.qos.skip.first") val qosSkipFirst : TimeUnit)
              extends DummyLogging with SimpleJmx
{
    @volatile
    private[this] var shuttingDown = false

    @volatile
    private[this] var dying = false
    
    @volatile
    private[this] var lastAliveTimestamp = System.nanoTime.nanoseconds

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
    val startedAt = System.nanoTime.nanoseconds

    /**
     * After this nanoseconds (compered to System.nanoTime) it is allowed to do qos measurements
     */
    private[this] val qosAllowedAfterNanos = (startedAt + qosSkipFirst).asNanoseconds

    /**
     * Returns true if application is dying (feels bad).
     */
    def isDying = dying

    /**
     * Returns true if the application is shutting down.
     */
    def isShuttingDown = shuttingDown

    /**
     * Returns timestamp when the application was alive last time.
     */
    def lastAlive = lastAliveTimestamp

    /**
     * Called by monitoring actor to set
     */
    def monitoringAlive () = lastAliveTimestamp = System.nanoTime.nanoseconds

    /**
     * Check if QoS measurements are allowed at the current point in time.
     */
    def isQosAllowed = System.nanoTime > qosAllowedAfterNanos

    /**
     * Called when application is no more reliable and must die.
     */
    lazy val die : Unit = {
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
    lazy val shutdown = {
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
