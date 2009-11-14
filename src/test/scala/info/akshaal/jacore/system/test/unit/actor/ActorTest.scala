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

class ActorTest extends SpecificationWithJUnit ("Actor specification") {
    import ActorTest._

    "Actor" should {
        "be constructable" in {
            withStartedActor [ConstructionTestActor] (actor =>
                actor must not be null
            )
        }

        "receive messages" in {
            withStartedActor [MessageReceivingTestActor] (actor => {
                actor.received  must_==  0

                waitForMessageAfter (actor) {actor ! "Hi"}

                actor.received  must_==  1
            })
        }

        "have afterActs methods" in {
            withStartedActor [AfterTestActor] (actor =>
                () // TODO
            )
        }
    }
}

object ActorTest {
    class ConstructionTestActor extends TestActor

    class MessageReceivingTestActor extends TestActor {
        var received = 0

        override def act () = {
            case str : String => received += 1
        }
    }

    class AfterTestActor extends TestActor {
    }
}