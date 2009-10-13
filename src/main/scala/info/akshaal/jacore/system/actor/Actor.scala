package info.akshaal.jacore
package system
package actor

import org.jetlang.fibers.PoolFiberFactory
import org.jetlang.core.BatchExecutor

import java.lang.reflect.Modifier

import Predefs._
import logger.Logging
import annotation.Act

/**
 * Implementation of actors.
 *
 * @param actorEnv environment for actor
 */
abstract class Actor (actorEnv : ActorEnv)
                extends Logging with NotNull
{
    /**
     * Method dispatcher. Forwards message processing request to
     * an appropriate method of this class.
     */
    private[this] final val dispatcher = createDispatcherAndSubscribe

    /**
     * Schedule to be used by this actor.
     */
    protected final val schedule =
                    new ActorSchedule (this, actorEnv.scheduler)

    /**
     * A fiber used by this actor.
     */
    private[this] final val fiber =
        new PoolFiberFactory (actorEnv.pool.executors)
                        .create (new ActorExecutor (this))

    /**
     * Current sender. Only valid when act method is called.
     */
    protected var sender : Option[Actor] = None

    /**
     * Method returns a partial function which must process
     * messages handled by actor. Function returns by default actor
     * processes no messages.
     */
    protected def act(): PartialFunction[Any, Unit] =
                    Actor.defaultActMessageHandler

    /**
     * Queue message for processing by this actor. This is alias for ! method.
     *
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
    private def invokeAct (msg : Any, sentFrom : Option[Actor]) =
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

    /**
     * Scans this actor and creates an implementation of MethodDispatcher
     * suitable to forward request for message processing to an appropriate
     * annotated method of this object. It also subscribes.
     */
    private def createDispatcherAndSubscribe : MethodDispatcher = {
        // Check methods and collect information
        var methods : List[(String, Class[_])] = Nil

        for (val method <- getClass.getMethods if method.isAnnotationPresent (classOf[Act])) {
            val methodName = method.getName

            def unrecover (str : String) {
                throw new UnrecoverableError ("Action method " + methodName + " " + str)
            }

            // Checks
            val modifiers = method.getModifiers

            if (Modifier.isPrivate (modifiers)) {
                unrecover ("must not be private")
            }

            if (Modifier.isStatic (modifiers)) {
                unrecover ("must not be static")
            }

            val returnType = method.getReturnType
            if (!returnType.equals (classOf[Void])) {
                unrecover ("must return nothing")
            }

            val paramTypes = method.getParameterTypes
            if (paramTypes.length != 1) {
                unrecover ("must have only one argument")
            }

            methods = (methodName, paramTypes(0)) :: methods
        }

        // TODO: groupBy to find duplicates
        // TODO: sortWith to order

        null
    }

    /**
     * Concrete implementation of this class is supposed to forward
     * a message processing request to an annotated method of the
     * enclosed class.
     */
    protected abstract class MethodDispatcher {
        def dispatch (msg : Any) : Unit
    }
}

/**
 * Executor of queued actors.
 *
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

/**
 * Helper object for the Actor class.
 */
private[actor] object Actor {
    /**
     * Default body of action handler. Processes no messages.
     */
    protected val defaultActMessageHandler =
        new PartialFunction[Any, Unit] {
            def isDefinedAt (msg : Any) = false
            def apply (msg : Any) = ()
        }    
}
