/*
 * Module.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package module

import com.google.inject.AbstractModule
import com.google.inject.matcher.Matchers
import com.google.inject.name.Names

import utils.{TimeUnit, ThreadPriorityChanger, DummyThreadPriorityChanger}
import actor.{CallByMessageMethodInterceptor, Actor, Broadcaster, BroadcasterActor}
import fs.text.{TextFile, TextFileActor}
import scheduler.{Scheduler, SchedulerImpl}
import annotation.CallByMessage
import logger.Logging
import utils.Prefs

/**
 * This module is supposed to help instantiate all classes needed for jacore
 * to work.
 */
class Module extends AbstractModule with Logging {
    lazy val prefsResource = "jacore.properties"
    lazy val prefs = new Prefs (prefsResource)

    lazy val osFileEncoding = prefs.getString("jacore.os.file.encoding")

    lazy val monitoringInterval = prefs.getTimeUnit("jacore.monitoring.interval")

    lazy val lowPriorityPoolThreads = prefs.getInt("jacore.pool.low.threads")
    lazy val lowPriorityPoolLatencyLimit = prefs.getTimeUnit("jacore.pool.low.latency")
    lazy val lowPriorityPoolExecutionLimit = prefs.getTimeUnit("jacore.pool.low.execution")

    lazy val normalPriorityPoolThreads = prefs.getInt("jacore.pool.normal.threads")
    lazy val normalPriorityPoolLatencyLimit = prefs.getTimeUnit("jacore.pool.normal.latency")
    lazy val normalPriorityPoolExecutionLimit = prefs.getTimeUnit("jacore.pool.normal.execution")

    lazy val hiPriorityPoolThreads = prefs.getInt("jacore.pool.hi.threads")
    lazy val hiPriorityPoolLatencyLimit = prefs.getTimeUnit("jacore.pool.hi.latency")
    lazy val hiPriorityPoolExecutionLimit = prefs.getTimeUnit("jacore.pool.hi.execution")

    lazy val schedulerLatencyLimit = prefs.getTimeUnit("jacore.scheduler.latency")
    lazy val schedulerDrift = prefs.getTimeUnit("jacore.scheduler.drift")

    lazy val daemonStatusJmxName = prefs.getString("jacore.status.jmx.name")
    lazy val daemonStatusUpdateInterval = prefs.getTimeUnit("jacore.status.update.interval")
    lazy val daemonStatusFile = prefs.getString("jacore.status.file")

    lazy val qosSkipFirst = prefs.getTimeUnit ("jacore.qos.skip.first")

    lazy val threadPriorityChangerImplClass : Class[T] forSome {type T <: ThreadPriorityChanger} =
                    classOf[DummyThreadPriorityChanger]

    // -- tests

    if (daemonStatusUpdateInterval > 0.nanoseconds) {
        require (daemonStatusUpdateInterval > monitoringInterval * 2,
                 "daemonStatusUpdateInterval must at least be greater"
                 + " than 2*monitoringInterval")
    } else {
        debug ("daemon status file is disabled")
    }

    // - - - - - - - - - - - - Bindings - - - - - - - - - -

    override def configure () = {
        // ----------------------------------------------------------
        // Instances

        //  - - - - - - - - - - -  Named - - - - - - - - - -  - - - -

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.qos.skip.first"))
              .toInstance (qosSkipFirst)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.scheduler.latency"))
              .toInstance (schedulerLatencyLimit)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.scheduler.drift"))
              .toInstance (schedulerDrift)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.monitoring.interval"))
              .toInstance (monitoringInterval)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.status.update.interval"))
              .toInstance (daemonStatusUpdateInterval)

        bind (classOf[String])
              .annotatedWith (Names.named ("jacore.status.file"))
              .toInstance (daemonStatusFile)

        bind (classOf[String])
              .annotatedWith (Names.named ("jacore.os.file.encoding"))
              .toInstance (osFileEncoding)

        // Hi priority pool parameters

        bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.pool.hi.threads"))
              .toInstance (hiPriorityPoolThreads)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.hi.latency"))
              .toInstance (hiPriorityPoolLatencyLimit)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.hi.execution"))
              .toInstance (hiPriorityPoolExecutionLimit)


        // Normal priority pool parameters

        bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.pool.normal.threads"))
              .toInstance (normalPriorityPoolThreads)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.normal.latency"))
              .toInstance (normalPriorityPoolLatencyLimit)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.normal.execution"))
              .toInstance (normalPriorityPoolExecutionLimit)

        
        // Low priority pool parameters

        bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.pool.low.threads"))
              .toInstance (lowPriorityPoolThreads)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.low.latency"))
              .toInstance (lowPriorityPoolLatencyLimit)

        bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.low.execution"))
              .toInstance (lowPriorityPoolExecutionLimit)


        // Daemon

        bind (classOf[String])
              .annotatedWith (Names.named("jacore.status.jmx.name"))
              .toInstance (daemonStatusJmxName)

        // ----------------------------------------------------------
        // Classes

        // Configurable implementation bindings
        bind (classOf[ThreadPriorityChanger]).to (threadPriorityChangerImplClass)

        // Internal implemntation bindings
        bind (classOf[Broadcaster]).to (classOf[BroadcasterActor])
        bind (classOf[TextFile]).to (classOf[TextFileActor])
        bind (classOf[JacoreManager]).to (classOf[JacoreManagerImpl])
        bind (classOf[Scheduler]).to (classOf[SchedulerImpl])

        // - - - - - - - - - - - - AOP - - - - - - - - - - - -  -- -
        
        bindInterceptor(Matchers.subclassesOf(classOf[Actor]),
                        Matchers.annotatedWith (classOf[CallByMessage]),
                        new CallByMessageMethodInterceptor)

    }
}
