/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package jms

import javax.jms.{Connection, Destination, Message, MessageConsumer, Session, MessageListener}

import actor.{Actor, LowPriorityActorEnv}

/**
 * Template for all actors that are supposed to receive messages from some JMS destination.
 * @param connection JMS connection
 * @param destination topic or queue
 */
abstract class AbstractJmsReceiverActor[T] (lowPriorityActorEnv : LowPriorityActorEnv,
                                            connection : Connection,
                                            destination : Destination)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{
    private[this] var session : Session = null
    private[this] var messageConsumer : MessageConsumer = null
    
    /**
     * Message must convert message from JMS Message type to domain object.
     * No processing is supposed to happen here. This method is executed outside of actor context.
     */
    protected def convertMessage (message : Message) : T

    /**
     * This message is executed to process converted message in actor context.
     * Default implementation boradcasts converted message.
     */
    protected def handleMessage (message : T) : Unit = {
        broadcaster.broadcast (message)
    }

    /**
     * Create session. Override if some exotic session settings are to be applied.
     * Default implementation creates session with auto_acknowledge mode.
     */
    protected def createSession () : Session = {
        connection.createSession (false, Session.AUTO_ACKNOWLEDGE)
    }

    /**
     * Create message consumer. By default this method calls createConsumer on session.
     * That means that for topic subscription is not durable by default.
     */
    protected def createMessageConsumer (session : Session) : MessageConsumer = {
        session.createConsumer (destination)
    }

    /**
     * Start message consumer.
     */
    override def start () : Boolean = {
        if (super.start ()) {
            session = createSession
            messageConsumer = createMessageConsumer (session)

            messageConsumer.setMessageListener (new MessageListener () {
                override def onMessage (message : Message) : Unit = {
                    val convertedMessage = convertMessage (message)

                    if (convertedMessage == null) {
                        debugLazy ("Message skipped because converted returned null: " + message)
                    } else {
                        postponed {
                            handleMessage (convertedMessage)
                        }
                    }
                }
            })

            return true
        } else {
            return false
        }
    }

    /**
     * Stops message consumer.
     */
    override def stop () : Boolean = {
        if (super.stop ()) {
            try {
                messageConsumer.close ()
            } finally {
                session.close ()
            }

            return true
        } else {
            return false
        }
    }
}
