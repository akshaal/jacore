/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package jms

import javax.jms.{Connection, ConnectionFactory, Destination, Message, MessageProducer, Session}

import Predefs._
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
 */
abstract class AbstractJmsSenderActor[T] (lowPriorityActorEnv : LowPriorityActorEnv,
                                          connectionFactory : ConnectionFactory,
                                          destination : Destination)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{
    protected var context : Option [(MessageProducer, Session, Connection)] = None
    protected var notifications : List[(Actor, Any)] = Nil

    /**
     * Creates a JMS Message from domain message.
     * @param msg domain message
     */
    protected def createJmsMessage (msg : T) : Message

    /**
     * Perform send operation.
     * @param msg message to send
     */
    protected def doSend (msg : T) : Unit = {
        val producer = context match {
            case Some ((prod, _, _)) => prod
            case None => initContext
        }

        producer.send (createJmsMessage (msg))
    }

    /**
     * Perform send operation.
     * @param msg message to send
     * @param payload send this message back to sender
     */
    protected def doSend (msg : T, payload : Any) : Unit = {
        doSend (msg)

        sender.foreach (actor => notifications = (actor, payload) :: notifications)
    }

    /**
     * Init connection, session and producer.
     *
     * NOTE: context must be updated by this method.
     */
    private[this] def initContext () : MessageProducer = {
        val connection = createConnection ()
        val session = createSession (connection)
        val producer = createProducer (session)
        
        context = Some (producer, session, connection)

        producer
    }

    /**
     * Creates a connection.
     */
    protected def createConnection () : Connection = {
        connectionFactory.createConnection
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
        context match {
            case None => ()
            case Some ((producer, session, connection)) =>
                context = None

                try {
                    producer.close ()

                    for ((actor, payload) <- notifications) {
                        actor ! JmsMessageSent (payload)
                    }
                } finally {
                    try {
                        session.close ()
                    } finally {
                        connection.close ()
                    }
                }
        }
    }
}
