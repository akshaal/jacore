package info.akshaal.jacore
package system.test.unit.actor

import com.google.inject.{Guice, Injector}

import Predefs._
import system.module.Module
import system.JacoreManager
import system.scheduler.Scheduler
import system.test.unit.BaseUnitTest
import system.utils.HiPriorityPool
import system.actor.{Actor, HiPriorityActorEnv}
import system.daemon.DaemonStatus

import org.testng.annotations.Test
import org.testng.Assert._

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import java.io.File

// NOTE: This test cannot use usual Test module, because it must not set
// it to dying state!, so we redefine some objects

object MonitoringTestModule extends Module {
    val daemonStatusFileFile = File.createTempFile ("jacore", "monitoringTest")
    daemonStatusFileFile.deleteOnExit

    override lazy val daemonStatusJmxName = "jacore:name=monitoringTestDaemonStatus"
    override lazy val daemonStatusFile = daemonStatusFileFile.getAbsolutePath

    val injector = Guice.createInjector (this)
    val jacoreManager = injector.getInstance (classOf[JacoreManager])

    jacoreManager.start

    val daemonStatus = injector.getInstance (classOf[DaemonStatus])
    val scheduler = injector.getInstance (classOf[Scheduler])
    val hiPriorityPool = injector.getInstance (classOf[HiPriorityPool])
    val hiPriorityActorEnv = injector.getInstance (classOf[HiPriorityActorEnv])

    abstract class HiPriorityActor extends Actor (actorEnv = hiPriorityActorEnv)
}

class MonitoringTest extends BaseUnitTest {
    MonitoringTestModule // We use it

    @Test (groups=Array("unit"))
    def testBadActor () = {
        val srv = ManagementFactory.getPlatformMBeanServer()
        val statusObj = new ObjectName (MonitoringTestModule.daemonStatusJmxName)

        BadActor.start
        BadActor ! "Hi"
        
        assertFalse (MonitoringTestModule.daemonStatus.isDying,
                     "The application must not be dying at this moment!")
        assertFalse (MonitoringTestModule.daemonStatus.isShuttingDown,
                     "The application must not be shutting down at this moment!")
        assertEquals (srv.getAttribute (statusObj, "dying"), false)
        assertEquals (srv.getAttribute (statusObj, "shuttingDown"), false)

        Thread.sleep (MonitoringTestModule.monitoringInterval.asMilliseconds * 4)

        assertTrue (MonitoringTestModule.daemonStatus.isDying,
                    "The application must be dying at this moment!")
        assertTrue (MonitoringTestModule.daemonStatus.isShuttingDown,
                     "The application must be shutting down at this moment!")
        assertEquals (srv.getAttribute (statusObj, "dying"), true)
        assertEquals (srv.getAttribute (statusObj, "shuttingDown"), true)

        BadActor.stop
    }
}

object BadActor extends MonitoringTestModule.HiPriorityActor {
    override def act () = {
        case x => {
            debug ("Starting to sleep")
            Thread.sleep (MonitoringTestModule.monitoringInterval.asMilliseconds * 2)
            debug ("We slept well")
        }
    }
}
