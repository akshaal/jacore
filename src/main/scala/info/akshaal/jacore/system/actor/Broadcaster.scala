/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import com.google.inject.{Inject, Singleton}

import annotation.CallByMessage

/**
 * Broadcaster service. Provides functionality to subscribe, sunsubscribe and broadcast messages.
 */
trait Broadcaster {
    /**
     * Subscribe the given actor on messages accepted by the given matcher.
     * Request is ignored if actor is already subscribed to this kind of messages.
     * @param actor actor that will receive message accepted by matcher.
     * @param matcher matcher that will accept or reject message for the actor.
     */
    def subscribe (actor : Actor, matcher : MessageMatcher[_]*) : Unit

    /**
     * Unsubscribe the given actor from messages accepted by the given matcher.
     * If actor is not subscribed then request is ingored.
     * @param actor actor that is currently subscribed to the messages accepted by matcher.
     * @param matcher matcher that is currently used by actor to filter broadcasted messages.
     */
    def unsubscribe (actor : Actor, matcher : MessageMatcher[_]*) : Unit

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
private[system] class BroadcasterImpl @Inject() (hiPriorityActorEnv : HiPriorityActorEnv)
                extends Actor (actorEnv = hiPriorityActorEnv)
                   with Broadcaster
{
    /** {@Inherited} */
    @CallByMessage
    override def subscribe (actor : Actor, matcher : MessageMatcher[_]*) : Unit = {
        // TODO
    }

    /** {@Inherited} */
    @CallByMessage
    override def unsubscribe (actor : Actor, matcher : MessageMatcher[_]*) : Unit = {
        // TODO
    }

    /** {@Inherited} */
    @CallByMessage
    override def broadcast (msg : Any) : Unit = {
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
private[actor] sealed case class MessageMatcher[A] (
                    acceptMessageClass : Class[A],
                    messageExtractionMatchers : Set[MessageExtractionMatcher[_ >: A]])

/**
 * Describes a matcher for message extraction.
 * @param A a class of objects that is accepted by extractor
 * @param acceptExtractionClass a class of extraction that is accepted by matcher
 * @param messageExtractor extractor class to be used to get extraction out of message
 */
private[actor] sealed case class MessageExtractionMatcher[A] (
                    acceptExtractionClass : Class[_],
                    messageExtractor : Class[MessageExtractor[A, _]])