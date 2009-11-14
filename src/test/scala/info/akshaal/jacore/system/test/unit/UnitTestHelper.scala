/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit

import com.google.inject.Guice
import java.io.File

import java.util.concurrent.{CountDownLatch, TimeUnit}

import Predefs._
import system.JacoreManager
import system.actor.{Actor, HiPriorityActorEnv}
import system.module.Module

/**
 * Helper methods for convenient testing of actors and stuff depending on actors.
 */
object UnitTestHelper {
    /**
     * How much time we wait for a message to arrive to actor before timing out.
     */
    val TIMEOUT = 2.seconds

    /**
     * Thrown if time is occured.
     */
    class MessageTimeout extends RuntimeException

    /**
     * Basic ancestor for all actor that are to be used in tests.
     */
    class TestActor extends Actor (actorEnv = TestModule.hiPriorityActorEnv) {
        var messageLatch : CountDownLatch = null

        override def afterActs () : Unit = {
            super.afterActs ()

            if (messageLatch != null) {
                messageLatch.countDown ()
            }
        }
    }

    /**
     * Execute function with the actor constructed by using guice injector.
     */
    def withStartedActor[T <: TestActor] (f : T => Any) (implicit clazz : ClassManifest[T]) : Unit =
    {
        val actor = TestModule.injector.getInstanceOf[T]
        try {
            actor.start
            f (actor)
        } finally {
            actor.stop
        }
    }

    /**
     * Execute the given code and wait for a message to be processed by actor. If
     * message is not received within some timeout interval, then test will be failed.
     * @param actor actor to wait message on
     * @param f code to execute before waiting for a message on actor
     */
    def waitForMessageAfter[T <: TestActor] (actor : T) (f : => Any) : Unit = {
        actor.messageLatch = new CountDownLatch (1)
        f
        if (!actor.messageLatch.await (TIMEOUT.asMilliseconds, TimeUnit.MILLISECONDS)) {
            throw new MessageTimeout
        }
    }

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
    }
}
