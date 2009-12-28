/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test
package unit.scheduler

import org.specs.SpecificationWithJUnit

import Predefs._
import unit.UnitTestHelper._
import scheduler.TimeOut

class SchedulerTest extends SpecificationWithJUnit ("Scheduler specification") {
    import SchedulerTest._

    "Scheduler" should {
        "provide recurrent scheduling which start when actor starts" in {
            withNotStartedActor [RecurrentTestActor] (actor => {
                Thread.sleep (400)

                actor.invocations  must_==  0

                actor.start ()
                Thread.sleep (400)

                actor.invocations must beIn (6 to 10)

                Thread.sleep (400)

                actor.invocations must beIn (14 to 18)
            })
        }

        "provide recurrent scheduling" in {
            withStartedActor [RecurrentTestActor] (actor => {
                Thread.sleep (400)

                actor.invocations must beIn (6 to 10)

                Thread.sleep (400)

                actor.invocations must beIn (14 to 18)
            })
        }

        "provide recurrent scheduling of code blocks" in {
            withStartedActor [RecurrentCodeTestActor] (actor => {
                Thread.sleep (400)

                actor.invocations must beIn (6 to 10)

                Thread.sleep (400)

                actor.invocations must beIn (14 to 18)
            })
        }

        "provide one time scheduling" in {
            withStartedActors [OneTimeTestActor, OneTimeTestActor2] ((actor, actor2) => {
                TestModule.scheduler.in (actor, 123, 130.milliseconds)
                TestModule.scheduler.in (actor2, 234, 50.milliseconds)

                Thread.sleep (30)

                actor.executed   must_==  0
                actor2.executed  must_==  0

                Thread.sleep (60)

                actor.executed   must_==  0
                actor2.executed  must_==  1

                Thread.sleep (200)

                actor.executed   must_==  1
                actor2.executed  must_==  1
            })
        }

        "provide cancelation of one time scheduling" in {
            withStartedActor [OneTimeTestActor] (actor => {
                val control = TestModule.scheduler.in (actor, 123, 130.milliseconds)

                Thread.sleep (30)

                actor.executed   must_==  0
                control.cancel ()

                Thread.sleep (400)

                actor.executed   must_==  0
            })
        }

        "provide cancelation of recurrent scheduling" in {
            withStartedActor [RecurrentOutterTestActor] (actor => {
                val control = TestModule.scheduler.every (actor, "Hail", 100.milliseconds)

                Thread.sleep (400)

                actor.invocations must beIn (3 to 5)
                control.cancel ()

                Thread.sleep (400)

                actor.invocations must beLessThan (6)
            })
        }
    }
}

object SchedulerTest {
    class OneTimeTestActor extends TestActor {
        var executed = 0

        override def act () = {
            case TimeOut (x : Int) => {
                debug ("Received [Int] message: " + x)
                executed += 1
            }
        }
    }

    class OneTimeTestActor2 extends TestActor {
        var executed = 0

        override def act () = {
            case TimeOut (x : Int) => {
                debug ("Received [Int] message: " + x)
                executed += 1
            }
        }
    }

    class RecurrentTestActor extends TestActor {
        schedule every 50.milliseconds payload "Hi"

        var invocations = 0

        override def act () = {
            case TimeOut (x : String) => {
                debug ("Received message: " + x)
                invocations += 1
            }
        }
    }

    class RecurrentOutterTestActor extends TestActor {
        var invocations = 0

        override def act () = {
            case TimeOut (x : String) => {
                debug ("Received message: " + x)
                invocations += 1
            }
        }
    }

    class RecurrentCodeTestActor extends TestActor {
        var invocations = 0

        schedule every 50.milliseconds executionOf {
            debug ("Triggered")
            invocations += 1
        }
    }
}
