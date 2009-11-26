/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test
package unit

import com.google.inject.{Guice, Binder}
import java.io.File

import java.util.concurrent.{CountDownLatch, TimeUnit}

import Predefs._
import system.JacoreManager
import system.actor.{Actor, HiPriorityActorEnv, LowPriorityActorEnv}
import system.module.Module
import system.scheduler.Scheduler
import system.fs.TextFile

/**
 * Helper methods for convenient testing of actors and stuff depending on actors.
 */
object UnitTestHelper extends TestHelper {
    override val timeout = 2.seconds
    override val injector = TestModule.injector

    createModuleGraphInDebugDir ("test-module.dot")

    /**
     * Basic ancestor for all actor that are to be used in tests.
     */
    class TestActor extends Actor (actorEnv = TestModule.hiPriorityActorEnv) with Waitable

    /**
     * Test module that is used for tests.
     */
    object TestModule extends Module {
        val daemonStatusFileFile = File.createTempFile ("Jacore", "UnitTest")
        daemonStatusFileFile.deleteOnExit

        override lazy val daemonStatusJmxName = "jacore:name=testStatus" + hashCode
        override lazy val daemonStatusFile = daemonStatusFileFile.getAbsolutePath

        val injector = Guice.createInjector (this)
        val jacoreManager = injector.getInstanceOf [JacoreManager]

        jacoreManager.start

        val hiPriorityActorEnv = injector.getInstanceOf[HiPriorityActorEnv]
        val lowPriorityActorEnv = injector.getInstanceOf[LowPriorityActorEnv]
        val scheduler = injector.getInstanceOf[Scheduler]
        val textFile = injector.getInstanceOf[TextFile]
    }
}
