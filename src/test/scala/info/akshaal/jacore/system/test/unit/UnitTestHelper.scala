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
import system.actor.{Actor, HiPriorityActorEnv, LowPriorityActorEnv}
import system.module.Module
import system.scheduler.Scheduler
import system.fs.TextFile

/**
 * Helper methods for convenient testing of actors and stuff depending on actors.
 */
object UnitTestHelper {
    /**
     * How much time we wait for a message to arrive to actor before timing out.
     */
    val TIMEOUT = 2.seconds

    /**
     * Execute function with the actor constructed by using guice injector.
     * Created actor will be started and passed to function f. When function is completed
     * its work, actor will be stopped.
     * @param f function receiving actor
     * @param [T] actor class
     */
    def withStartedActor[T <: Actor] (f : T => Any) (implicit clazz : ClassManifest[T]) : Unit =
    {
        withNotStartedActor[T] (actor => {
            actor.start
            f (actor)
        })
    }

    /**
     * Execute function with the actor constructed by using guice injector. Extor must be
     * starter explicitly in f.
     * @param f function receiving actor, when execution of this function is done, actor
     *          will be stopped
     * @param [T] actor class to instantiate
     */
    def withNotStartedActor[T <: Actor] (f : T => Any) (implicit clazz : ClassManifest[T]) : Unit =
    {
        val actor = TestModule.injector.getInstanceOf[T]
        try {
            f (actor)
        } finally {
            actor.stop
        }
    }

    /**
     * Execute the given code and wait for a message to be processed by actor. If
     * message is not received within some timeout interval, then test will be failed.
     * @param actor actor to wait message on
     * @param count number of batches to wait
     * @param f code to execute before waiting for a message on actor
     */
    def waitForMessageBatchesAfter[T <: Waitable] (actor : T, count : Int) (f : => Any) : Unit = {
        actor.messageLatch = new CountDownLatch (count)

        f
        
        if (!actor.messageLatch.await (TIMEOUT.asMilliseconds, TimeUnit.MILLISECONDS)) {
            throw new MessageTimeout
        }
    }

    /**
     * Execute the given code and wait for a message to be processed by actor. If
     * message is not received within some timeout interval, then test will be failed.
     * @param actor actor to wait message on
     * @param f code to execute before waiting for a message on actor
     */
    def waitForMessageAfter[T <: Waitable] (actor : T) (f : => Any) : Unit = {
        waitForMessageBatchesAfter (actor, 1) {f}
    }

    /**
     * Thrown if time is occured.
     */
    class MessageTimeout extends RuntimeException ("Tinmeout while waiting for a message")

    /**
     * Basic ancestor for all actor that are to be used in tests.
     */
    class TestActor extends Actor (actorEnv = TestModule.hiPriorityActorEnv) with Waitable {
    }

    /**
     * Makes it possible to wait a momment when messages are processed by actor.
     */
    trait Waitable extends Actor {
        var messageLatch : CountDownLatch = null

        override def afterActs () : Unit = {
            try {
                super.afterActs
            } finally {
                if (messageLatch != null) {
                    messageLatch.countDown ()
                }
            }
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
        val lowPriorityActorEnv = injector.getInstanceOf[LowPriorityActorEnv]
        val scheduler = injector.getInstanceOf[Scheduler]
        val textFile = injector.getInstanceOf[TextFile]
    }
}