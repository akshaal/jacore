/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.jms

import org.specs.mock.{Mockito, MocksCreation}

import javax.jms.{Connection, Destination, ObjectMessage, Message,
                  MessageProducer, Session, JMSException}

import unit.UnitTestHelper._
import jms.AbstractJmsSenderActor

class JmsSenderActorTest extends JacoreSpecWithJUnit ("AbstractJmsSenderActor specification")
                            with Mockito
{
    import JmsSenderActorTest._

    "AbstractJmsSenderActor" should {
        // We use the same mockedConnection so test must not be run in parallel
        setSequential ()

        "send messages to JMS destination" in {
            mockedConnection = mock [Connection]

            withNotStartedActor [JmsSenderTestActor] (actor => {
                val connection = mockedConnection
                val session = mock [Session]
                val producer = mock [MessageProducer]
                val msg1 = mock [ObjectMessage]
                val msg2 = mock [ObjectMessage]
                val msg3 = mock [ObjectMessage]

                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createProducer (MockHelper.destination) returns producer
                session.createObjectMessage ("one") returns msg1
                session.createObjectMessage ("two") returns msg2
                session.createObjectMessage ("3") returns msg3

                actor.sendAsy ("one")
                actor.sendAsy ("two")
                actor.sendAsy ("3")

                actor.waitForMessageAfter {actor.start}

                (connection.createSession (false, Session.AUTO_ACKNOWLEDGE) on connection)  then
                (session.createProducer (MockHelper.destination)            on session)     then
                (session.createObjectMessage ("one")                        on session)     then
                (producer.send (msg1)                                       on producer)    then
                (session.createObjectMessage ("two")                        on session)     then
                (producer.send (msg2)                                       on producer)    then
                (session.createObjectMessage ("3")                          on session)     then
                (producer.send (msg3)                                       on producer)    then
                (producer.close ()                                          on producer)    then
                (session.close ()                                   on session) were calledInOrder

                connection.close was notCalled
            })
        }

        "properly close resources on exceptions" in {
            mockedConnection = mock [Connection]

            withNotStartedActor [JmsSenderTestActor] (actor => {
                val connection = mockedConnection
                val session = mock [Session]
                val producer = mock [MessageProducer]
                val msg1 = mock [ObjectMessage]

                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createProducer (MockHelper.destination) returns producer
                session.createObjectMessage ("one") returns msg1
                producer.close () throws new JMSException ("test")
                session.close () throws new JMSException ("test")

                actor.sendAsy ("one")

                actor.waitForMessageAfter {actor.start}

                (connection.createSession (false, Session.AUTO_ACKNOWLEDGE) on connection)  then
                (session.createProducer (MockHelper.destination)            on session)     then
                (session.createObjectMessage ("one")                        on session)     then
                (producer.send (msg1)                                       on producer)    then
                (producer.close ()                                          on producer)    then
                (session.close ()                                   on session) were calledInOrder

                connection.close was notCalled
            })
        }
    }
}

object JmsSenderActorTest {
    var mockedConnection : Connection = null

    class JmsSenderTestActor extends AbstractJmsSenderActor[String] (
                                lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                connection = mockedConnection,
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
