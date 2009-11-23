/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit
package jms

import org.specs.SpecificationWithJUnit
import org.specs.mock.{Mockito, MocksCreation}

import javax.jms.{Connection, ConnectionFactory, Destination, Message, MessageProducer, Session}

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

                factory.createConnection() returns connection
                connection.createSession (false, Session.AUTO_ACKNOWLEDGE) returns session
                session.createProducer (MockHelper.destination) returns producer

                actor.send ("one")
                actor.send ("two")
                actor.send ("3")

                waitForMessageAfter (actor) {actor.start}

                (factory.createConnection()                   on factory)     then
                (connection.close ()                          on connection)  were calledInOrder
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