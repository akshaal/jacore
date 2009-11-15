/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit
package actor

import org.specs.SpecificationWithJUnit

import Predefs._
import UnitTestHelper._

import system.annotation.{Act, ExtractBy}
import system.actor.{MessageExtractor, Broadcaster}

class BroadcasterTest extends SpecificationWithJUnit ("Broadcaster specification") {
    import BroadcasterTest._
    
    "Broadcaster" should {
        "broadcast messages to subscribers" in {
            withStartedActor [StringBroadcasterTestActor] (stringActor => {
                withStartedActor [IntBroadcasterTestActor] (intActor => {
                    val broadcaster = TestModule.injector.getInstanceOf[Broadcaster]

                    stringActor.intCalls     must_==  0
                    stringActor.stringCalls  must_==  0
                    intActor.intCalls        must_==  0
                    intActor.stringCalls     must_==  0

                    waitForMessageAfter (intActor) {broadcaster.broadcast (1)}

                    stringActor.intCalls     must_==  0
                    stringActor.stringCalls  must_==  0
                    intActor.intCalls        must_==  1
                    intActor.stringCalls     must_==  0

                    waitForMessageAfter (stringActor) {broadcaster.broadcast ("hi")}

                    stringActor.intCalls     must_==  0
                    stringActor.stringCalls  must_==  1
                    intActor.intCalls        must_==  1
                    intActor.stringCalls     must_==  0
                })
            })
        }
    }
}

object BroadcasterTest {
    class StringBroadcasterTestActor extends TestActor {
        var intCalls = 0
        var stringCalls = 0

        @Act
        def intHandler (msg : Int) = {
            intCalls += 1
        }

        @Act (subscribe = true)
        def stringHandler (msg : String,
                           @ExtractBy(classOf[BrNotEmptyStringExtractor]) extraction : String) =
        {
            stringCalls += 1
        }
    }

    class IntBroadcasterTestActor extends TestActor {
        var intCalls = 0
        var stringCalls = 0

        @Act (subscribe = true)
        def intHandler (msg : Int) = {
            intCalls += 1
        }

        @Act
        def stringHandler (msg : String,
                           @ExtractBy(classOf[BrNotEmptyStringExtractor]) extraction : String) =
        {
            stringCalls += 1
        }
    }

    class BrNotEmptyStringExtractor extends MessageExtractor[String, String] {
        override def extractFrom (msg : String) = if (msg.isEmpty) null else msg
    }
}