/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.actor

import com.google.inject.Guice
import java.io.File
import java.lang.management.ManagementFactory
import javax.management.ObjectName

import module.Module
import actor.{Actor, HiPriorityActorEnv}
import daemon.DaemonStatus

import unit.UnitTestHelper._

class MonitoringTest extends JacoreSpecWithJUnit ("Monitoring specification") {
    import unit.UnitTestHelper._
    import MonitoringTest._

    // TODO: Need to suppress this terrible message shown in console

    "Monitoring" should {
        "detect not responding actors" in {
            val srv = ManagementFactory.getPlatformMBeanServer()
            val statusObj = new ObjectName (MonitoringTestModule.daemonStatusJmxName)

            withStartedActor [BadActor] (actor => {
                actor ! "Hi"

                MonitoringTestModule.daemonStatus.isDying         must beFalse
                MonitoringTestModule.daemonStatus.isShuttingDown  must beFalse
                srv.getAttribute (statusObj, "dying")             must_==  false
                srv.getAttribute (statusObj, "shuttingDown")      must_==  false

                Thread.sleep (MonitoringTestModule.monitoringInterval.asMilliseconds * 5)

                MonitoringTestModule.daemonStatus.isDying         must beTrue
                MonitoringTestModule.daemonStatus.isShuttingDown  must beTrue
                srv.getAttribute (statusObj, "dying")             must_==  true
                srv.getAttribute (statusObj, "shuttingDown")      must_==  true
            })
        }
    }
}

object MonitoringTest {
    // NOTE: This test cannot use usual Test module, because it must not set
    // it to dying state!, so we redefine some objects
    object MonitoringTestModule extends Module {
        val daemonStatusFileFile = File.createTempFile ("jacore", "monitoringTest")
        daemonStatusFileFile.deleteOnExit

        override lazy val daemonStatusJmxName = "jacore:name=monitoringTestDaemonStatus"
        override lazy val daemonStatusFile = daemonStatusFileFile.getAbsolutePath
        override lazy val monitoringInterval = 500.milliseconds

        val injector = Guice.createInjector (this)
        val jacoreManager = injector.getInstanceOf [JacoreManager]

        jacoreManager.start

        val daemonStatus = injector.getInstance (classOf[DaemonStatus])
        val hiPriorityActorEnv = injector.getInstance (classOf[HiPriorityActorEnv])
    }

    class BadActor extends Actor (actorEnv = MonitoringTestModule.hiPriorityActorEnv) {
        override def act () = {
            case x => {
                debug ("Starting to sleep")
                Thread.sleep (MonitoringTestModule.monitoringInterval.asMilliseconds * 2)
                debug ("We slept well")
            }
        }
    }
}
