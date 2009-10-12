/*
 * ActorTest.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit.actor

import system.test.unit.{BaseUnitTest, UnitTestModule, HiPriorityActor}
import org.testng.annotations.Test
import org.testng.Assert._
import system.annotation.{CallByMessage, Act}

/**
 * Test for aspected things of actors.
 */
class AopTest extends BaseUnitTest {
    @Test (groups=Array("unit"))
    def testMethodInterceptor () = {
        val injector = UnitTestModule.injector

        val aopTestActor = injector.getInstance (classOf[AopTestActor])
        assertEquals (aopTestActor.sum, 0)
        aopTestActor.inc ()
        assertEquals (aopTestActor.sum, 0)

        UnitTestModule.actorManager.startActor (aopTestActor)
        
        Thread.sleep (1000)
        assertEquals (aopTestActor.sum, 1)

        aopTestActor.inc ()

        Thread.sleep (1000)
        assertEquals (aopTestActor.sum, 2)

        UnitTestModule.actorManager.stopActor (aopTestActor)
    }

    @Test (groups=Array("unit"))
    def testActAnnotation () = {
        val injector = UnitTestModule.injector

        val actor = injector.getInstance (classOf[ActAnnotationTestActor])

        UnitTestModule.actorManager.startActor (actor)
        UnitTestModule.actorManager.stopActor (actor)
    }
}

/**
 * Actor to test @CallByMessage annotation.
 */
class AopTestActor extends HiPriorityActor {
    var sum = 0

    @CallByMessage
    def inc () = {
        sum = sum + 1
    }
}

/**
 * Actor to test @Act annotation.
 */
class ActAnnotationTestActor extends HiPriorityActor {
}