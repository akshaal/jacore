/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import com.google.inject.{Inject, Singleton}
import java.util.IdentityHashMap

import annotation.CallByMessage

/**
 * Broadcaster service. Provides functionality to subscribe, sunsubscribe and broadcast messages.
 */
trait Broadcaster {
    /**
     * Subscribe the given actor on messages accepted by the given matcher.
     * Request is ignored if actor is already subscribed to this kind of messages.
     * @param actor actor that will receive message accepted by matcher.
     * @param matchers matchers that will accept or reject message for the actor.
     */
    def subscribe (actor : Actor, matchers : MessageMatcherDefinition[_]*) : Unit

    /**
     * Unsubscribe the given actor from messages accepted by the given matcher.
     * If actor is not subscribed then request is ingored.
     * @param actor actor that is currently subscribed to the messages accepted by matcher.
     * @param matchers matchers that are currently used by actor to filter broadcasted messages.
     */
    def unsubscribe (actor : Actor, matchers : MessageMatcherDefinition[_]*) : Unit

    /**
     * Broadcast message to all actors subscribed to this type of msg.
     * @param msg msg to broadcast
     */
    def broadcast (msg : Any) : Unit
}

/**
 * Implementation of broadcaster service.
 */
@Singleton
private[system] class BroadcasterActor @Inject() (hiPriorityActorEnv : HiPriorityActorEnv)
                extends Actor (actorEnv = hiPriorityActorEnv)
                   with Broadcaster
{
    /**
     * Subscriptions of actors. We use IdentityHashMap and IdentityHashSet to speedup
     * work. Implementation classes are used as type parameters in order to emphasize
     * that values must be identity hash maps!
     */
    private[this] final val subscriptions =
                new IdentityHashMap[MessageMatcher, IdentityHashMap[Actor, Void]]

    /**
     * Used for perfomance optimization.
     */
    private[this] final val subscriptionEntries = subscriptions.entrySet

    /**
     * Used in broadcast method to track actors that have already been executed
     * for the given message. This is not a local variable inside method, but a property
     * in order to increase performance and memory garbage collection overheads.
     */
    private[this] final val executed = new IdentityHashMap[Actor, Void]

    // - - - - - - - - - - - -
    // Message handlers

    /** {@Inherited} */
    @CallByMessage
    override final def subscribe (actor : Actor, matchers : MessageMatcherDefinition[_]*) : Unit =
    {
        matchers.foreach (subscribeOneMatcher (actor, _))
    }

    /** {@Inherited} */
    @CallByMessage
    override final def unsubscribe (actor : Actor, matchers : MessageMatcherDefinition[_]*) : Unit =
    {
        matchers.foreach (unsubscribeOneMatcher (actor, _))
    }

    /** {@Inherited} */
    @CallByMessage
    override final def broadcast (msg : Any) : Unit = {
        executed.clear
        
/*        for (entry <- subscriptionEntries) {
            if (entry.getKey ().check (msg)) {
                for (final Code code : entry.getValue ()) {
                    final Object prev = executed.put (code, null);
                    if (prev == null) {
                        code.run ();
                    }
                }
            }
        }*/
    }

    // - - - - - - - - - - - - -
    // Private methods

    /**
     * Subscribe actor to one matcher.
     * @param actor actor to subscribe
     * @param matcher matcher to subscribe actor to
     */
    private def subscribeOneMatcher (actor : Actor, matcher : MessageMatcherDefinition[_]) : Unit =
    {
        // TODO
    }

    /**
     * Unsubscribe actor from one matcher.
     * @param actor actor to unsubscribe
     * @param matcher matcher to unsubscribe actor from
     */
    private def unsubscribeOneMatcher (actor : Actor, matcher : MessageMatcherDefinition[_]) : Unit =
    {
        // TODO
    }
}

/**
 * Describes a matcher for messages.
 * @param [A] type that is accepted by matcher
 * @param acceptMessageClass message must be assignable to object of this class in order to be
 *                              accepted by this matcher.
 * @param messageExtractions a set of message extractions that must be tested against message
 */
sealed case class MessageMatcherDefinition[A] (
                    acceptMessageClass : Class[A],
                    messageExtractionDefinitions : Set[MessageExtractionDefinition[_ >: A]])

/**
 * Describes a matcher for message extraction.
 * @param A a class of objects that is accepted by extractor
 * @param acceptExtractionClass a class of extraction that is accepted by matcher
 * @param messageExtractor extractor class to be used to get extraction out of message
 */
sealed case class MessageExtractionDefinition[A] (
                    acceptExtractionClass : Class[_],
                    messageExtractor : Class[MessageExtractor[A, _]])

/**
 * Trait for an implementations of matcher definition.
 */
private[actor] trait MessageMatcher {
    /**
     * Returns true if the given message is acceptable.
     */
    def isAcceptable (msg : Any) : Boolean
}