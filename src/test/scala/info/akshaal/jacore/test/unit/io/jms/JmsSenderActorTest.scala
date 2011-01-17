/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.jms

import org.specs.mock.{Mockito, MocksCreation}

import javax.jms.{Connection, Destination, ObjectMessage, Message,
                  MessageProducer, Session, JMSException}

import unit.UnitTestHelper._
import io.jms.AbstractJmsSenderActor

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

                there was one(connection).createSession (false, Session.AUTO_ACKNOWLEDGE)  then
                          one(session).createProducer (MockHelper.destination)             then
                          one(session).createObjectMessage ("one")                         then
                          one(producer).send (msg1)                                        then
                          one(session).createObjectMessage ("two")                         then
                          one(producer).send (msg2)                                        then
                          one(session).createObjectMessage ("3")                           then
                          one(producer).send (msg3)                                        then
                          one(producer).close ()                                           then
                          one(session).close ()     orderedBy (connection, session, producer)

                there was no(connection).close()
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

                there was one(connection).createSession (false, Session.AUTO_ACKNOWLEDGE)  then
                          one(session).createProducer (MockHelper.destination)             then
                          one(session).createObjectMessage ("one")                         then
                          one(producer).send (msg1)                                        then
                          one(producer).close ()                                           then
                          one(session).close ()    orderedBy (connection, session, producer)

                there was no(connection).close()
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
