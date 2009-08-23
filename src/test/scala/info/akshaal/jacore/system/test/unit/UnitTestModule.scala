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
import system.MywireManager
import system.actor.{Actor, ActorManager}
import system.module.Module
import system.daemon.DaemonStatus
import system.scheduler.Scheduler
import system.fs.FileActor
import system.utils.{HiPriorityPool, NormalPriorityPool, ThreadPriorityChanger}


object UnitTestModule extends Module {
    override lazy val daemonStatusJmxName = "mywire:name=testStatus"

    val injector = Guice.createInjector (this)
    val mywireManager = injector.getInstance (classOf[MywireManager])

    mywireManager.start

    val daemonStatus = injector.getInstance (classOf[DaemonStatus])
    val scheduler = injector.getInstance (classOf[Scheduler])
    val actorManager = injector.getInstance (classOf[ActorManager])
    val fileActor = injector.getInstance (classOf[FileActor])
    val hiPriorityPool = injector.getInstance (classOf[HiPriorityPool])
    val normalPriorityPool = injector.getInstance (classOf[NormalPriorityPool])
    val threadPriorityChanger = injector.getInstance (classOf[ThreadPriorityChanger])
}

abstract class HiPriorityActor extends Actor (
                     scheduler = UnitTestModule.scheduler,
                     pool = UnitTestModule.hiPriorityPool)
