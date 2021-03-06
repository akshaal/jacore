/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.jms

import org.specs.mock.{Mockito, MocksCreation}

import javax.jms.{Connection, Destination, Message, MessageConsumer, Session, MessageListener,
                  JMSException}

import unit.UnitTestHelper._
import io.jms.AbstractJmsReceiverActor
import annotation.Act

class JmsReceiverActorTest extends JacoreSpecWithJUnit ("AbstractJmsReceiverActor specification")
                              with Mockito
{
    import JmsReceiverActorTest._

    "AbstractJmsReceiverActor" should {
        // We use the same mockedConnection so test must not be run in parallel
        setSequential ()

        "receive messages from JMS destination" in {
            mockedConnection = mock [Connection]

            withNotStartedActor [JmsReceiverTestActor] (actor => {
                val connection = mockedConnection
                val session = mock [Session]
                val consumer = mock [MessageConsumer]
                var messageListener : MessageListener = null

                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createConsumer (MockHelper.destination) returns consumer
                consumer.setMessageListener(any[MessageListener]) answers {
                    listener => messageListener = listener.asInstanceOf[MessageListener]
                }

                val msg1 = mock [Message]
                val msg2 = mock [Message]
                val msg3 = mock [Message]

                actor.start ()

                actor.waitForMessageBatchesAfter (2) {messageListener.onMessage (msg1)}
                messageListener.onMessage (null)
                actor.waitForMessageBatchesAfter (2) {messageListener.onMessage (msg2)}
                actor.waitForMessageBatchesAfter (2) {messageListener.onMessage (msg3)}

                actor.stop ()

                there was one(connection).createSession (false, Session.AUTO_ACKNOWLEDGE)   then
                          one(session).createConsumer (MockHelper.destination)              then
                          one(consumer).setMessageListener (messageListener)                then
                          one(consumer).close ()                                            then
                          one(session).close ()   orderedBy (connection, session, consumer)

                there was no(connection).close()

                actor.msgs  must_==  List (msg3, msg2, msg1)
            })
        }

        "properly close resource on even with exceptions" in {
            mockedConnection = mock [Connection]

            withNotStartedActor [JmsReceiverTestActor] (actor => {
                val connection = mockedConnection
                val session = mock [Session]
                val consumer = mock [MessageConsumer]
                var messageListener : MessageListener = null

                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createConsumer (MockHelper.destination) returns consumer
                consumer.setMessageListener(any[MessageListener]) answers {
                    listener => messageListener = listener.asInstanceOf[MessageListener]
                }
                consumer.close () throws new JMSException ("test1")
                session.close () throws new JMSException ("test2")

                actor.start ()
                actor.stop () must throwA[JMSException]

                there was one(connection).createSession (false, Session.AUTO_ACKNOWLEDGE)   then
                          one(session).createConsumer (MockHelper.destination)              then
                          one(consumer).setMessageListener(messageListener)                 then
                          one(consumer).close ()                                            then
                          one(session).close ()  orderedBy (connection, session, consumer)

                there was no(connection).close()
            })
        }
    }
}

object JmsReceiverActorTest {
    var mockedConnection : Connection = null

    case class ConvertedMessage (origMessage: Message)

    class JmsReceiverTestActor extends AbstractJmsReceiverActor[ConvertedMessage] (
                                lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                connection = mockedConnection,
                                destination = MockHelper.destination)
                             with Waitable
    {
        var msgs : List[Message] = Nil

        override protected def convertMessage (message : Message) : ConvertedMessage = {
            if (message == null) {
                null
            } else {
                ConvertedMessage (message)
            }
        }

        @Act (subscribe = true)
        protected def onConvertedMessage (msg : ConvertedMessage) : Unit = {
            msgs = msg.origMessage :: msgs
        }
    }

    /**
     * This object is used to helper create mock object. We need this because
     * scala failed to work if JmsReceiverActorTest extends MockCreation trait.
     */
    object MockHelper extends MocksCreation {
        val destination = mock [Destination]
    }
}
