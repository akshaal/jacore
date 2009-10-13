package info.akshaal.jacore
package system
package actor

import org.jetlang.fibers.PoolFiberFactory
import org.jetlang.core.BatchExecutor

import Predefs._
import logger.Logging

/**
 * Very simple and hopefully fast implementation of actors.
 *
 * @param actorEnv environment for actor
 */
abstract class Actor (actorEnv : ActorEnv)
                extends Logging with NotNull
{
    /**
     * Method returns a partial function which must process
     * messages handled by actor. Function returns by default actor
     * processes no messages.
     */
    protected def act(): PartialFunction[Any, Unit] =
                                        Actor.defaultActMessageHandler

    // ===================================================================
    // Concrete methods

    protected final val schedule =
                        new ActorSchedule (this, actorEnv.scheduler)

    /**
     * Current sender. Only valid when act method is called.
     */
    protected var sender : Option[Actor] = None

    /**
     * A fiber used by this actor.
     */
    private[this] val fiber =
        new PoolFiberFactory (actorEnv.pool.executors)
                        .create (new ActorExecutor (this))


    /**
     * Queue message for processing by this actor.
     * @param msg message to process
     */
    final def queue (msg: Any): Unit = this ! msg

    /**
     * Send a message to the actor.
     */
    final def !(msg: Any): Unit = {
        val sentFrom = ThreadLocalState.current.get
        val runTimingFinisher = actorEnv.pool.latencyTiming.createFinisher

        // This runner will be executed by executor when time has come
        // to process the message
        val runner = mkRunnable {
            runTimingFinisher ("[latency] Actor started for message: " + msg)

            val executeTimingFinisher =
                        actorEnv.pool.executionTiming.createFinisher

            // Execute
            logIgnoredException ("Error processing message: " + msg) {
                invokeAct (msg, sentFrom)
            }

            // Show complete latency
            executeTimingFinisher ("[execution] Actor completed for message: " + msg)
        }

        fiber.execute (runner)
    }

    /**
     * Invokes this actor's act() method.
     */
    private[this] def invokeAct (msg : Any, sentFrom : Option[Actor]) =
    {
        sender = sentFrom

        try {
            msg match {
                case Ping => sentFrom.foreach (_ ! Pong)
                case call @ Call (inv) => CallByMessageMethodInterceptor.call (call)
                case other if act.isDefinedAt (msg) => act () (msg)
                case other => warn ("Ignored message: " + msg)
            }
        } finally {
            sender = None
        }
    }

    /**
     * Start actor.
     */
    private[actor] final def start () = {
        debug ("About to start")
        fiber.start
    }

    /**
     * Stop the actor.
     */
    private[actor] final def stop() = {
        debug ("About to stop")
        fiber.dispose
    }
}

/**
 * Helper object for the Actor class.
 */
private[actor] object Actor {
    /**
     * Default body of action handler. Processes no messages.
     */
    protected final val defaultActMessageHandler =
        new PartialFunction[Any, Unit] {
            def isDefinedAt (msg : Any) = false
            def apply (msg : Any) = ()
        }
}

/**
 * Executor of queued actors.
 * @param actor this actor will be used as a current actor
 *        when processing messages of the actor.
 */
private[actor] class ActorExecutor (actor : Actor)
                extends BatchExecutor {
    final override def execute (commands: Array[Runnable]) = {
        // Remember the current actor in thread local variable.
        // So later it may be referenced from ! method of other actors
        ThreadLocalState.current.set(Some(actor))

        // Execute
        for (command <- commands) {
            command.run
        }

        // Reset curren actor
        ThreadLocalState.current.set(None)
    }
}

/**
 * Thread local state of the actor environment.
 */
private[actor] object ThreadLocalState {
    final val current = new ThreadLocal[Option[Actor]]()
}
