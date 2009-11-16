package info.akshaal.jacore
package system
package actor

import org.jetlang.fibers.PoolFiberFactory
import org.jetlang.core.BatchExecutor

import Predefs._
import logger.Logging

/**
 * Implementation of actors.
 *
 * @param actorEnv environment for actor
 */
abstract class Actor (actorEnv : ActorEnv) extends Logging with NotNull
{
    // TODO: Split to traits
    // TODO: Extract transport (fibers) out of this class

    /**
     * A set of descriptions for methods annotated with @Act annotation.
     * This is used to create a dispatcher and subscribe/unsubscribe actor to messages.
     */
    private[this] final val actMethodDescs = ActorClassScanner.scan (this)

    /**
     * A sequence of matchers to be used for auto-subscribe/unsubscribe of this actor
     * when it is started/stopped.
     */
    private[this] final val matcherDefinitionsForSubscribe =
                                    actMethodDescs.filter(_.subscribe).map (_.matcherDefinition)

    /**
     * Method dispatcher. Forwards message processing request to
     * an appropriate method of this class.
     */
    private[this] final val dispatcher = createDispatcher

    /**
     * Schedule to be used by this actor.
     */
    protected final val schedule = new ActorSchedule (this, actorEnv.scheduler)

    /**
     * Broadcaster to be used by this actor.
     */
    protected final val broadcaster = actorEnv.broadcaster

    /**
     * A fiber used by this actor.
     */
    private[this] final val fiber =
        new PoolFiberFactory (actorEnv.pool.executors).create (new ActorExecutor (this))

    /**
     * Current sender. Only valid when act method is called.
     */
    protected var sender : Option[Actor] = None

    /**
     * Indicates a need to execute afterActs method. Must be set during invocation
     * of actor's act() like methods with not system messages.
     */
    protected var afterActsNeeded = false

    /**
     * Method returns a partial function which must process
     * messages handled by actor. Function returns by default actor
     * processes no messages.
     */
    protected def act(): PartialFunction[Any, Unit] = Actor.defaultActMessageHandler

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
    @inline
    private[this] def invokeAct (msg : Any, sentFrom : Option[Actor]) = {
        sender = sentFrom

        var userMessage = true
        try {
            msg match {
                case Ping =>
                    userMessage = false
                    sentFrom.foreach (_ ! Pong)

                case call @ Call (inv) =>
                    CallByMessageMethodInterceptor.call (call)

                case other if dispatcher.dispatch(msg) =>

                case other if act.isDefinedAt (msg) =>
                    act () (msg)

                case other =>
                    userMessage = false
                    warn ("Ignored message: " + msg)
            }
        } finally {
            afterActsNeeded = afterActsNeeded || userMessage
            sender = None
        }
    }

    /**
     * Called after processing of some amount message is done. Amount of message is undefined.
     * This method is guaranteed to be called even if an exception is occured during processing
     * of messages. Internal messages (like Ping) will not trigger this method.
     */
    protected def afterActs () = {}

    /**
     * This method is called by actor executor when the last message of batch is processed
     * and it is time to execute 'afterActs' method.
     */
    private[actor] def executeAfterActs () = {
        // Only execute after acts method if there were messages that are not system
        if (afterActsNeeded) {
            afterActsNeeded = false
            logIgnoredException ("Error while invoking afterActs method") {
                afterActs ()
            }
        }
    }

    /**
     * Start actor.
     */
    def start () = {
        debug ("About to start")

        // Subscribe first. This is very first thing to do before publish any event.
        if (!matcherDefinitionsForSubscribe.isEmpty) {
            broadcaster.subscribe (this, matcherDefinitionsForSubscribe : _*)
        }

        // Start transport
        fiber.start

        // Publish event
        broadcaster.broadcast (ActorStartedEvent (this))
    }

    /**
     * Stop the actor.
     */
    def stop() = {
        debug ("About to stop")
        
        // Unsubscribe
        if (!matcherDefinitionsForSubscribe.isEmpty) {
            broadcaster.unsubscribe (this, matcherDefinitionsForSubscribe : _*)
        }

        // Stop transport
        fiber.dispose

        // Publish event
        broadcaster.broadcast (ActorStoppedEvent (this))
    }

    /**
     * Scans this actor and creates an implementation of MethodDispatcher
     * suitable to forward request for message processing to an appropriate
     * annotated method of this object. It also subscribes.
     */
    private[this] def createDispatcher : MethodDispatcher = {
        if (actMethodDescs.isEmpty) {
            EmptyMethodDispatcher
        } else {
            new ActorMethodDispatcherGenerator (this, actMethodDescs)
                           .create ().asInstanceOf[MethodDispatcher]
        }
    }

    /**
     * Implementation of this class is supposed to forward
     * a message processing request to an annotated method of the
     * enclosed class.
     */
    protected abstract class MethodDispatcher extends NotNull {
        /**
         * Dispatch message to one of methods annotated with @Act annotation.
         *
         * @param msg message to process
         * @return true if message has been forwarded
         */
        def dispatch (msg : Any) : Boolean
    }

    /**
     * Implementations that is to be used for Actor classes with no methods annotated with @Act
     * annotations.
     */
    private[this] object EmptyMethodDispatcher extends MethodDispatcher {
        /**
         * {@Inherited}
         */
        override def dispatch (msg : Any) : Boolean = false
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
        ThreadLocalState.current.set (Some(actor))

        try {
            // Execute
            try {
                commands.foreach (_.run)
            } finally {
                actor.executeAfterActs ()
            }
        } finally {
            // Reset curren actor
            ThreadLocalState.current.set (None)
        }
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

sealed case class ActorStartedEvent (actor : Actor)
sealed case class ActorStoppedEvent (actor : Actor)