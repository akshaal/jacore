/*
 * TestModule.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit

import com.google.inject.{Guice, Injector}

import Predefs._
import system.JacoreManager
import system.actor.{Actor, HiPriorityActorEnv}
import system.module.Module
import system.daemon.DaemonStatus
import system.scheduler.Scheduler
import system.fs.TextFile
import system.utils.{HiPriorityPool, NormalPriorityPool, ThreadPriorityChanger}


object UnitTestModule extends Module {
    override lazy val daemonStatusJmxName = "jacore:name=testStatus"

    val injector = Guice.createInjector (this)
    val jacoreManager = injector.getInstance (classOf[JacoreManager])

    jacoreManager.start

    val daemonStatus = injector.getInstance (classOf[DaemonStatus])
    val scheduler = injector.getInstance (classOf[Scheduler])
    val textFile = injector.getInstance (classOf[TextFile])
    val hiPriorityPool = injector.getInstance (classOf[HiPriorityPool])
    val normalPriorityPool = injector.getInstance (classOf[NormalPriorityPool])
    val threadPriorityChanger = injector.getInstance (classOf[ThreadPriorityChanger])
    val hiPriorityActorEnv = injector.getInstance (classOf[HiPriorityActorEnv])
}

abstract class HiPriorityActor extends Actor (
                     actorEnv = UnitTestModule.hiPriorityActorEnv)
