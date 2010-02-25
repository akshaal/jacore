/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test

import com.google.inject.Injector

import java.util.concurrent.{CountDownLatch, TimeUnit => JavaTimeUnit}
import java.io.File

import org.specs.SpecificationWithJUnit
import org.specs.specification.{Example, Examples}

import actor.Actor
import utils.GuiceUtils
import logger.Logger

/**
 * Helper methods for convenient testing of actors and stuff depending on actors.
 */
trait TestHelper {
    /**
     * How much time we wait for a message to arrive to actor before timing out.
     */
    val timeout : TimeUnit

    /**
     * Injector to use for tests.
     */
    val injector : Injector

    /**
     * Create graph definition if property jacore.module.debug.dir is defined.
     * @param ilenameSuffix suffix for name of the file to create
     */
    def createModuleGraphInDebugDir (filenameSuffix : String) : Unit = {
        val debugDir = System.getProperty ("jacore.module.debug.dir")
        if (debugDir != null) {
            new File (debugDir).mkdirs
            GuiceUtils.createModuleGraph (debugDir + "/" + filenameSuffix, injector)
        }
    }

    /**
     * Start actor, execute code and then stop actor.
     *
     * @param actor to manage.
     */
    def withStartedActor (actor : Actor) (code : => Unit) {
        actor.start

        try {
            code
        } finally {
            actor.stop
        }
    }

    /**
     * Execute function with the actor constructed by using guice injector.
     * Created actor will be started and passed to function f. When function is completed
     * its work, actor will be stopped.
     * @param f function receiving actor
     * @param [A] actor class
     */
    def withStartedActor[A <: Actor] (f : A => Any) (implicit clazz : ClassManifest[A]) : Any =
    {
        withNotStartedActor[A] (actor => {
            actor.start
            f (actor)
        })
    }

    /**
     * Execute function with the actors constructed by using guice injector.
     * Created actors will be started and passed to function f. When function is completed
     * its work, actors will be stopped.
     * @param f function receiving actors
     * @param [A] actor class
     * @param [B] second actor class
     */
    def withStartedActors[A <: Actor, B <: Actor] (f : (A, B) => Any)
                                            (implicit clazzA : ClassManifest[A],
                                             clazzB : ClassManifest[B]) : Any =
    {
        withNotStartedActors[A, B] ((actor1 , actor2) => {
            actor1.start
            actor2.start
            f (actor1, actor2)
        }) (clazzA, clazzB)
    }

    /**
     * Execute function with the actor constructed by using guice injector. Extor must be
     * starter explicitly in f.
     * @param f function receiving actor, when execution of this function is done, actor
     *          will be stopped
     * @param [A] actor class to instantiate
     */
    def withNotStartedActor[A <: Actor] (f : A => Any) (implicit clazz : ClassManifest[A]) : Any =
    {
        val actor = injector.getInstanceOf[A]
        try {
            f (actor)
        } finally {
            actor.stop
        }
    }

    /**
     * Execute function with the actors constructed by using guice injector.
     * Created actors will not be started. The actors will be passed to function f.
     * When function is completed its work, actors will be stopped.
     * @param f function receiving actors
     * @param [A] first actor class
     * @param [B] second actor class
     */
    def withNotStartedActors[A <: Actor, B <: Actor] (f : (A, B) => Any)
                                            (implicit clazzA : ClassManifest[A],
                                             clazzB : ClassManifest[B]): Any = {
        withNotStartedActor[A] (actor1 => {
            withNotStartedActor[B] (actor2 => {
                f (actor1, actor2)
            }) (clazzB)
        }) (clazzA)
    }

    /**
     * Thrown if time is occured.
     */
    class MessageTimeout extends RuntimeException ("Tinmeout while waiting for a message")

    /**
     * Makes it possible to wait a momment when messages are processed by actor.
     */
    trait Waitable extends Actor {
        private var messageLatch : CountDownLatch = null

        /**
         * Execute the given code and wait for a message to be processed by actor. If
         * message is not received within some timeout interval, then MessageTimeout exception
         * will be thrown.
         * @param count number of batches to wait
         * @param f code to execute before waiting for a message on actor
         */
        def waitForMessageBatchesAfter (count : Int) (f : => Any) : Unit = {
            messageLatch = new CountDownLatch (count)

            debug ("Executing message trigger before waiting for message(s)")

            f

            debug ("Waiting for " + count + " message(s)")
            if (!messageLatch.await (timeout.asMilliseconds, JavaTimeUnit.MILLISECONDS)) {
                throw new MessageTimeout
            }
        }

        /**
         * Execute the given code and wait for a message to be processed by actor. If
         * message is not received within some timeout interval, then MessageTimeout
         * exception will be thrown.
         * @param f code to execute before waiting for a message on actor
         */
        def waitForMessageAfter (f : => Any) : Unit = {
            waitForMessageBatchesAfter (1) {f}
        }

        protected override def afterActs () : Unit = {
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
     * Specification with additional features to be tested specs framework runned by junit.
     */
    class JacoreSpecWithJUnit (name : String) extends SpecificationWithJUnit (name) {
        protected implicit val jacoreLogger : Logger = Logger.get (this)

        override def beforeExample (ex: Examples) = {
            ex match {
                case example : Example =>
                    beforeOneExample (example)
                    super.beforeExample (ex)

                case other =>
                    super.beforeExample (other)
            }
        }

        override def afterExample (ex: Examples) = {
            ex match {
                case example : Example =>
                    afterOneExample (example)
                    super.afterExample (ex)

                case other =>
                    super.afterExample (other)
            }
        }

        /**
         * Called right before an example is executed.
         */
        def beforeOneExample (example : Example) : Unit = {
            jacoreLogger.debugLazy ("== == == About to run example: " + example.description)
        }

        /**
         * Called right after an example is executed.
         */
        def afterOneExample (example : Example) : Unit = {
            jacoreLogger.debugLazy ("== == == Example execution finished: " + example.description)
        }
    }
}
