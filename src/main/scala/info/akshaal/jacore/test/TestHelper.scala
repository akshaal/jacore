/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test

import java.io.File

import com.google.inject.Injector

import actor.Actor
import utils.GuiceUtils

/**
 * Helper methods for convenient testing of actors and stuff depending on actors.
 */
trait TestHelper {
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
}
