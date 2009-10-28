/*
 * Module.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package module

import com.google.inject.{Module => GuiceModule, Binder, Singleton, Inject}
import com.google.inject.matcher.Matchers
import com.google.inject.name.Names

import Predefs._
import utils.{TimeUnit, ThreadPriorityChanger, DummyThreadPriorityChanger}
import actor.{CallByMessageMethodInterceptor, Actor, Broadcaster, BroadcasterImpl}
import annotation.CallByMessage

/**
 * This module is supposed to help instantiate all classes needed for jacore
 * to work.
 */
class Module extends GuiceModule {
    lazy val prefsResource = "/jacore.properties"
    lazy val prefs = new Prefs (prefsResource)

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

    lazy val daemonStatusJmxName = prefs.getString("jacore.status.jmx.name")
    lazy val daemonStatusUpdateInterval = prefs.getTimeUnit("jacore.status.update.interval")
    lazy val daemonStatusFile = prefs.getString("jacore.status.file")

    lazy val fileReadBytesLimit = 1024*1024

    lazy val threadPriorityChangerImplClass : Class[T] forSome {type T <: ThreadPriorityChanger} =
                    classOf[DummyThreadPriorityChanger]

    // -- tests

    require (daemonStatusUpdateInterval > monitoringInterval * 2,
             "daemonStatusUpdateInterval must at least be greater"
             + " than 2*monitoringInterval")

    // - - - - - - - - - - - - Bindings - - - - - - - - - -

    override def configure (binder : Binder) = {
        // Configurable implementation bindings
        binder.bind (classOf[ThreadPriorityChanger]).to (threadPriorityChangerImplClass)

        // Internal implemntation bindings
        binder.bind (classOf[Broadcaster]).to (classOf[BroadcasterImpl])

        // - - - - - - - - - - - - AOP - - - - - - - - - - - -  -- -
        
        binder.bindInterceptor(Matchers.subclassesOf(classOf[Actor]),
                               Matchers.annotatedWith (classOf[CallByMessage]),
                               new CallByMessageMethodInterceptor)

        // - - - - - - - - - - - - Instances - - - - - - - - -  -- - 

        binder.bind (classOf[Prefs])
              .toInstance (prefs)

        //  - - - - - - - - - - -  Named - - - - - - - - - -  - - - -

        binder.bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.file.buffer.limit"))
              .toInstance (fileReadBytesLimit)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.scheduler.latency"))
              .toInstance (schedulerLatencyLimit)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.monitoring.interval"))
              .toInstance (monitoringInterval)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.status.update.interval"))
              .toInstance (daemonStatusUpdateInterval)

        binder.bind (classOf[String])
              .annotatedWith (Names.named ("jacore.status.file"))
              .toInstance (daemonStatusFile)


        // Hi priority pool parameters

        binder.bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.pool.hi.threads"))
              .toInstance (hiPriorityPoolThreads)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.hi.latency"))
              .toInstance (hiPriorityPoolLatencyLimit)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.hi.execution"))
              .toInstance (hiPriorityPoolExecutionLimit)


        // Normal priority pool parameters

        binder.bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.pool.normal.threads"))
              .toInstance (normalPriorityPoolThreads)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.normal.latency"))
              .toInstance (normalPriorityPoolLatencyLimit)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.normal.execution"))
              .toInstance (normalPriorityPoolExecutionLimit)

        
        // Low priority pool parameters
        binder.bind (classOf[Int])
              .annotatedWith (Names.named ("jacore.pool.low.threads"))
              .toInstance (lowPriorityPoolThreads)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.low.latency"))
              .toInstance (lowPriorityPoolLatencyLimit)

        binder.bind (classOf[TimeUnit])
              .annotatedWith (Names.named ("jacore.pool.low.execution"))
              .toInstance (lowPriorityPoolExecutionLimit)


        // Daemon

        binder.bind (classOf[String])
              .annotatedWith (Names.named("jacore.status.jmx.name"))
              .toInstance (daemonStatusJmxName)
    }
}
