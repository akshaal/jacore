/*
 * ActorTest.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit.actor

import org.testng.annotations.Test
import org.testng.Assert._

import system.test.unit.{BaseUnitTest, UnitTestModule, HiPriorityActor}
import system.actor.{MessageExtractor, Broadcaster}
import system.annotation.{Act, ExtractBy}

/**
 * Test broadcasting.
 */
class BroadcasterTest extends BaseUnitTest {
    @Test (groups=Array("unit"))
    def testBroadcasting () = {
        val injector = UnitTestModule.injector

        val stringActor = injector.getInstance (classOf[StringBroadcasterTestActor])
        val intActor = injector.getInstance (classOf[IntBroadcasterTestActor])
        val broadcaster = injector.getInstance (classOf[Broadcaster])

        stringActor.start
        intActor.start

        assertEquals (stringActor.intCalls, 0)
        assertEquals (stringActor.stringCalls, 0)
        assertEquals (intActor.intCalls, 0)
        assertEquals (intActor.stringCalls, 0)

        broadcaster.broadcast (1)
        sleep

        assertEquals (stringActor.intCalls, 0)
        assertEquals (stringActor.stringCalls, 0)
        assertEquals (intActor.intCalls, 1)
        assertEquals (intActor.stringCalls, 0)

        broadcaster.broadcast ("")
        sleep

        assertEquals (stringActor.intCalls, 0)
        assertEquals (stringActor.stringCalls, 0)
        assertEquals (intActor.intCalls, 1)
        assertEquals (intActor.stringCalls, 0)

        broadcaster.broadcast ("hi")
        sleep

        assertEquals (stringActor.intCalls, 0)
        assertEquals (stringActor.stringCalls, 1)
        assertEquals (intActor.intCalls, 1)
        assertEquals (intActor.stringCalls, 0)

        stringActor.stop
        intActor.stop
    }

    def sleep : Unit = Thread.sleep (1000)
}

/**
 * Actor to test broadcast.
 */
class StringBroadcasterTestActor extends HiPriorityActor {
    var intCalls = 0
    var stringCalls = 0

    @Act
    def intHandler (msg : Int) = {
        intCalls += 1
    }

    @Act (subscribe = true)
    def stringHandler (msg : String, @ExtractBy(classOf[BrNotEmptyStringExtractor]) extraction : String) = {
        stringCalls += 1
    }
}

/**
 * Actor to test broadcast.
 */
class IntBroadcasterTestActor extends HiPriorityActor {
    var intCalls = 0
    var stringCalls = 0

    @Act (subscribe = true)
    def intHandler (msg : Int) = {
        intCalls += 1
    }

    @Act
    def stringHandler (msg : String, @ExtractBy(classOf[BrNotEmptyStringExtractor]) extraction : String) = {
        stringCalls += 1
    }
}

class BrNotEmptyStringExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = if (msg.isEmpty) null else msg
}
