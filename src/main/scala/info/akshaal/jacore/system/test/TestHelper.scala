/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package test

import com.google.inject.Injector

import java.util.concurrent.{CountDownLatch, TimeUnit => JavaTimeUnit}

import Predefs._
import actor.Actor
import utils.TimeUnit

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
        withNotStartedActors[A, B] ((actor1, actor2) => {
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
     * Execute the given code and wait for a message to be processed by actor. If
     * message is not received within some timeout interval, then test will be failed.
     * @param actor actor to wait message on
     * @param count number of batches to wait
     * @param f code to execute before waiting for a message on actor
     */
    def waitForMessageBatchesAfter[T <: Waitable] (actor : T, count : Int) (f : => Any) : Unit = {
        actor.messageLatch = new CountDownLatch (count)

        f
        
        if (!actor.messageLatch.await (timeout.asMilliseconds, JavaTimeUnit.MILLISECONDS)) {
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
}
