/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test.unit

import com.google.inject.{Guice, Binder}
import java.io.File

import info.akshaal.jacore.actor.{Actor, HiPriorityActorEnv, LowPriorityActorEnv}
import info.akshaal.jacore.module.Module
import info.akshaal.jacore.scheduler.Scheduler
import info.akshaal.jacore.fs.text.TextFile
import info.akshaal.jacore.test.TestHelper
import info.akshaal.jacore.JacoreManager

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
