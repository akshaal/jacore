/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package jms

import javax.jms.{Connection, Destination, Message, MessageProducer, Session}

import actor.{Actor, LowPriorityActorEnv}

/**
 * Issued to the requester when sending is finished.
 * @param payload arbitrary payload provided to send function
 *                  when message was requested for sending
 */
sealed case class JmsMessageSent (payload : Any)

/**
 * Template for all actors that are supposed to send messages to some JMS destination.
 * @param [T] type of message this actor sends
 * @param connection JMS connection
 * @param destination topic or queue
 */
abstract class AbstractJmsSenderActor[T] (lowPriorityActorEnv : LowPriorityActorEnv,
                                          connection : Connection,
                                          destination : Destination)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{
    protected var context : Option [(MessageProducer, Session)] = None
    protected var notifications : List[(Actor, Any)] = Nil

    /**
     * Creates a JMS Message from domain message.
     * @param msg domain message
     */
    protected def createJmsMessage (session : Session, msg : T) : Message

    /**
     * Send message.
     * @param msg message to send
     */
    def sendAsy (msg : T) : Unit = {
        postponed {
            doSend (msg)
        }
    }

    /**
     * Send message.
     * @param msg message to send
     * @param payload send this message back to sender
     */
    def sendAsy (msg : T, payload : Any) : Unit = {
        postponed {
            doSend (msg)

            sender.foreach (actor => notifications = (actor, payload) :: notifications)
        }
    }

    /**
     * Perform send operation.
     * @param msg message to send
     */
    protected def doSend (msg : T) : Unit = {
        context match {
            case Some ((producer, session)) =>
                producer.send (createJmsMessage (session, msg))

            case None =>
                context = Some (initContext)
                doSend (msg)
        }
    }

    /**
     * Init connection, session and producer.
     */
    private[this] def initContext () : (MessageProducer, Session) = {
        val session = createSession (connection)
        val producer = createProducer (session)
        
        (producer, session)
    }

    /**
     * Create session. Override if some exotic session settings are to be applied.
     */
    protected def createSession (connection : Connection) : Session = {
        connection.createSession (false, Session.AUTO_ACKNOWLEDGE)
    }

    /**
     * Create producer. Override if an exotic producer is required.
     */
    protected def createProducer (session : Session) : MessageProducer = {
        session.createProducer (destination)
    }

    /**
     * {@InheritDoc}
     *
     * Used to commit end session.
     */
    protected override def afterActs () : Unit = {
        for ((producer, session) <- context) {
            context = None

            try {
                producer.close ()

                for ((actor, payload) <- notifications) {
                    actor ! JmsMessageSent (payload)
                }
            } finally {
                session.close ()
            }
        }
    }
}
