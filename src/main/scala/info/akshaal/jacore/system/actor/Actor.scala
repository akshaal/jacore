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

            if (Modifier.isStatic (modifiers)) {
                unrecover ("must not be static")
            }

            // Check return type
            val returnType = method.getReturnType
            if (returnType != Void.TYPE) {
                unrecover ("must return nothing, but returns " + returnType.getName)
            }

            // Check uniqueness of method name
            if (allMethodNames.count(_ == methodName) > 1) {
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

            if (!paramDescs.head.extractor.isEmpty) {
                unrecover ("can't have extraction as a first argument."
                           + " First argument must be a message method receives.")
            }

            if (paramDescs.count (_.extractor.isEmpty) > 1) {
                unrecover ("must have no more than one argument without extractor")
            }

            // Create matcher
            val acceptMessageClass = paramDescs.head.clazz
            val messageExtractions =
                    for (paramDesc <- paramDescs.tail)
                            yield MessageExtraction (acceptExtractionClass = paramDesc.clazz,
                                                     messageExtractor = paramDesc.extractor.get)

            for (messageExtraction <- messageExtractions) {
                val extractor = messageExtraction.messageExtractor
                val acceptExtractionClass = messageExtraction.acceptExtractionClass

                if (acceptExtractionClass.isPrimitive) {
                    unrecover ("can't use primitive types for extractions")
                }

                if (!classOf[MessageExtractor[Any, Any]].isAssignableFrom(extractor)) {
                    unrecover ("has an extractor not implementing MessageExtractor interface: "
                               + extractor)
                }

                val extractingMethods =
                        extractor.getMethods
                                 .filter (m => m.getName == "extractFrom" && !m.isSynthetic)
                if (extractingMethods.length > 1) {
                    unrecover ("uses an extractor with overloaded extractFrom method: "
                               + extractor)
                }

                val extractorMethod = extractingMethods.head
                val extractorMethodArg = extractorMethod.getParameterTypes()(0)
                val extractorMethodReturn = extractorMethod.getReturnType

                if (!extractorMethodArg.isAssignableFrom(acceptMessageClass)) {
                    unrecover ("uses extractor " + extractor
                               + " which can't handle messages of class " + acceptMessageClass)
                }

                if (!extractorMethodReturn.isAssignableFrom(acceptExtractionClass)
                    && !acceptExtractionClass.isAssignableFrom(extractorMethodReturn))
                {
                    unrecover ("uses extractor " + extractor
                               + " which produces values of class incompatible"
                               + " to the class of extraction (argument)")
                }
            }

            val messageMatcher =
                    MessageMatcher (acceptMessageClass = acceptMessageClass,
                                    messageExtractions = Set(messageExtractions : _*))

            if (messageMatcher.messageExtractions.size != paramDescs.length - 1) {
                unrecover ("must not have duplicated arguments")
            }

            // Gather information
            val actAnnotation = method.getAnnotation (classOf[Act])

            methods ::= ActMethodDesc (name = methodName,
                                       subscribe = actAnnotation.subscribe,
                                       params = paramDescs,
                                       matcher = messageMatcher)

        }

        debugLazy ("Found action methods " + methods)

        // Sanity checks on class level
        for ((_, methodGroup) <- methods.groupBy (_.matcher)) {
            if (methodGroup.length > 1) {
                throw new UnrecoverableError ("More than one mathod match the same messages: "
                                              + methodGroup.map(_.name).mkString(" "))
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
                                                params : Seq[ActMethodParamDesc],
                                                matcher : MessageMatcher)

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
