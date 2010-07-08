/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.scheduler

import java.util.Random

import unit.UnitTestHelper._
import scheduler.{TimeOut, UnfixedScheduling}
import utils.SimpleFunction0

class SchedulerTest extends JacoreSpecWithJUnit ("Scheduler specification") {
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

        "provide recurrent scheduling of simple functions" in {
            withStartedActor [RecurrentSimpleFunctionTestActor] (actor => {
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
                val control = TestModule.scheduler.in (actor, 123, 300.milliseconds)

                Thread.sleep (100)

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

        "work properly with negative hashcode" in {
            withNotStartedActor [RecurrentCodeWithNegativeHashcodeTestActor] (actor => {
                val started = System.currentTimeMillis

                actor.waitForMessageAfter {actor.start}
                actor.invocations  must_==  1

                actor.waitForMessageAfter {}
                actor.invocations  must_==  2

                val lasted = System.currentTimeMillis - started
                lasted  must beIn (360 to 960)
            })
        }

        "work properly with min hashcode" in {
            withNotStartedActor [RecurrentCodeWithMinHashcodeTestActor] (actor => {
                val started = System.currentTimeMillis

                actor.waitForMessageAfter {actor.start}
                actor.invocations  must_==  1

                actor.waitForMessageAfter {}
                actor.invocations  must_==  2

                val lasted = System.currentTimeMillis - started
                lasted  must beIn (360 to 960)
            })
        }

        "work properly with max hashcode" in {
            withNotStartedActor [RecurrentCodeWithMaxHashcodeTestActor] (actor => {
                val started = System.currentTimeMillis

                actor.waitForMessageAfter {actor.start}
                actor.invocations  must_==  1

                actor.waitForMessageAfter {}
                actor.invocations  must_==  2

                val lasted = System.currentTimeMillis - started
                lasted  must beIn (360 to 960)
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

    class RecurrentSimpleFunctionTestActor extends TestActor {
        var invocations = 0

        schedule every 50.milliseconds applicationOf new SimpleFunction0[Unit] {
            override def apply () = {
                debug ("Triggered")
                invocations += 1
            }
        }
    }

    class RecurrentCodeWithNegativeHashcodeTestActor extends TestActor with UnfixedScheduling {
        schedule every 400.milliseconds payload "Hi"

        var invocations = 0

        override def act () = {
            case TimeOut (x : String) => {
                debug ("Received message: " + x + ", actor's hashCode=" + hashCode)
                invocations += 1
            }
        }

        override val hashCode = -1 * new Random ().nextInt.abs
    }

    class RecurrentCodeWithMinHashcodeTestActor extends TestActor with UnfixedScheduling {
        schedule every 400.milliseconds payload "Hi"

        var invocations = 0

        override def act () = {
            case TimeOut (x : String) => {
                debug ("Received message: " + x + ", actor's hashCode=" + hashCode)
                invocations += 1
            }
        }

        override val hashCode = Integer.MIN_VALUE
    }

    class RecurrentCodeWithMaxHashcodeTestActor extends TestActor with UnfixedScheduling {
        var invocations = 0
        schedule every 400.milliseconds payload "Hi"


        override def act () = {
            case TimeOut (x : String) => {
                debug ("Received message: " + x + ", actor's hashCode=" + hashCode)
                invocations += 1
            }
        }

        override val hashCode = Integer.MAX_VALUE
    }
}
