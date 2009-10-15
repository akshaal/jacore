package info.akshaal.jacore
package system
package actor

import org.jetlang.fibers.PoolFiberFactory
import org.jetlang.core.BatchExecutor

import java.lang.reflect.Modifier

import Predefs._
import logger.Logging
import annotation.{Act, ExtractBy}

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
        var methods : List[ActMethodDesc] = Nil

        // For each annotated method (including inherited) of this class
        val allMethodNames = getClass.getMethods.map (_.getName)

        for (method <- getClass.getMethods if method.isAnnotationPresent (classOf[Act])) {
            val methodName = method.getName

            def unrecover (str : String) {
                throw new UnrecoverableError ("Action method " + methodName + " " + str)
            }

            // Checks modifiers
            val modifiers = method.getModifiers

            if (Modifier.isPrivate (modifiers)) {
                unrecover ("must not be private")
            }

            if (Modifier.isStatic (modifiers)) {
                unrecover ("must not be static")
            }

            // Check return type
            val returnType = method.getReturnType
            if (returnType != Void.TYPE) {
                unrecover ("must return nothing, but returns " + returnType.getName)
            }

            // Check uniqueness of method name
            if (allMethodNames.filter(_ == methodName).length > 1) {
                unrecover ("must not be overloaded")
            }

            // Check params
            val paramTypes = method.getParameterTypes
            if (paramTypes.length == 0) {
                unrecover ("must have at least one argument")
            }

            val paramExtractors =
                method.getParameterAnnotations.map (
                        _.find (_.isInstanceOf [ExtractBy])
                         .map (_.asInstanceOf [ExtractBy].value())
                    )

            val paramDescs =
                for ((paramClazz, paramExtractor) <- paramTypes.zip (paramExtractors))
                    yield ActMethodParamDesc (clazz = paramClazz,
                                              extractor = paramExtractor)

            if (paramDescs.filter (_.extractor.isEmpty).length > 1) {
                unrecover ("must have no more than one argument without extractor")
            }

            if (Set(paramDescs : _*).size != paramDescs.length) {
                unrecover ("must not have duplicated arguments")
            }

            // Gather information
            val actAnnotation = method.getAnnotation (classOf[Act])

            methods ::= ActMethodDesc (name = methodName,
                                       subscribe = actAnnotation.subscribe,
                                       params = paramDescs)
        }

        // Sanity checks on class level
        val methodMatchingSets =
                methods.map (methodDesc => (methodDesc.name, Set (methodDesc.params : _*)))

        for ((_, methodGroup) <- methodMatchingSets.groupBy (_._2)) {
            if (methodGroup.length > 1) {
                throw new UnrecoverableError ("More than one mathod match the same messages: "
                                              + methodGroup.map(_._1).mkString(" "))
            }
        }

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
 * Describes a method annotated with @Act annotation.
 */
private[actor] sealed case class ActMethodDesc (name : String,
                                                subscribe : Boolean,
                                                params : Seq[ActMethodParamDesc])

private[actor] sealed case class ActMethodParamDesc (clazz : Class[_ <: Any],
                                                     extractor : Option[Class[_ <: Any]])

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
