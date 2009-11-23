/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit
package jms

import org.specs.SpecificationWithJUnit
import org.specs.mock.{Mockito, MocksCreation}

import javax.jms.{Connection, ConnectionFactory, Destination, ObjectMessage, Message,
                  MessageProducer, Session, JMSException}

import Predefs._
import UnitTestHelper._
import system.jms.AbstractJmsSenderActor

class JmsSenderActorTest extends SpecificationWithJUnit ("AbstractJmsSenderActor specification")
                            with Mockito
{
    import JmsSenderActorTest._

    "AbstractJmsSenderActor" should {
        // We use the same mockedConnectionFactory so test must not be run in parallel
        setSequential ()

        "send messages to JMS destination" in {
            mockedConnectionFactory = mock [ConnectionFactory]

            withNotStartedActor [JmsSenderTestActor] (actor => {
                val factory = mockedConnectionFactory
                val connection = mock [Connection]
                val session = mock [Session]
                val producer = mock [MessageProducer]
                val msg1 = mock [ObjectMessage]
                val msg2 = mock [ObjectMessage]
                val msg3 = mock [ObjectMessage]

                factory.createConnection() returns connection
                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createProducer (MockHelper.destination) returns producer
                session.createObjectMessage ("one") returns msg1
                session.createObjectMessage ("two") returns msg2
                session.createObjectMessage ("3") returns msg3

                actor.send ("one")
                actor.send ("two")
                actor.send ("3")

                waitForMessageAfter (actor) {actor.start}

                (factory.createConnection()                                 on factory)     then
                (connection.start ()                                        on connection)  then
                (connection.createSession (false, Session.AUTO_ACKNOWLEDGE) on connection)  then
                (session.createProducer (MockHelper.destination)            on session)     then
                (session.createObjectMessage ("one")                        on session)     then
                (producer.send (msg1)                                       on producer)    then
                (session.createObjectMessage ("two")                        on session)     then
                (producer.send (msg2)                                       on producer)    then
                (session.createObjectMessage ("3")                          on session)     then
                (producer.send (msg3)                                       on producer)    then
                (producer.close ()                                          on producer)    then
                (session.close ()                                           on session)     then
                (connection.close ()                            on connection)  were calledInOrder
            })
        }

        "properly close resources on exceptions" in {
            mockedConnectionFactory = mock [ConnectionFactory]

            withNotStartedActor [JmsSenderTestActor] (actor => {
                val factory = mockedConnectionFactory
                val connection = mock [Connection]
                val session = mock [Session]
                val producer = mock [MessageProducer]
                val msg1 = mock [ObjectMessage]

                factory.createConnection() returns connection
                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createProducer (MockHelper.destination) returns producer
                session.createObjectMessage ("one") returns msg1
                producer.close () throws new JMSException ("test")
                session.close () throws new JMSException ("test")

                actor.send ("one")

                waitForMessageAfter (actor) {actor.start}

                (factory.createConnection()                                 on factory)     then
                (connection.start ()                                        on connection)  then
                (connection.createSession (false, Session.AUTO_ACKNOWLEDGE) on connection)  then
                (session.createProducer (MockHelper.destination)            on session)     then
                (session.createObjectMessage ("one")                        on session)     then
                (producer.send (msg1)                                       on producer)    then
                (producer.close ()                                          on producer)    then
                (session.close ()                                           on session)     then
                (connection.close ()                            on connection)  were calledInOrder
            })
        }
    }
}

object JmsSenderActorTest {
    var mockedConnectionFactory : ConnectionFactory = null

    class JmsSenderTestActor extends AbstractJmsSenderActor[String] (
                                lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                connectionFactory = mockedConnectionFactory,
                                destination = MockHelper.destination)
                             with Waitable
    {
        override protected def createJmsMessage (session : Session, msg : String) : Message = {
            session.createObjectMessage (msg)
        }
    }

    /**
     * This object is used to helper create mock object. We need this because
     * scala failed to work if JmsSenderActorTest extends MockCreation trait.
     */
    object MockHelper extends MocksCreation {
        val destination = mock [Destination]
    }
}