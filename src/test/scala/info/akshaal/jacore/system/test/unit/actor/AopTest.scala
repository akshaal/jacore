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
import com.google.inject.ProvisionException

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
        
        sleep
        assertEquals (aopTestActor.sum, 1)

        aopTestActor.inc ()

        sleep
        assertEquals (aopTestActor.sum, 2)

        UnitTestModule.actorManager.stopActor (aopTestActor)
    }

    @Test (groups=Array("unit"))
    def testActAnnotation () = {
        val injector = UnitTestModule.injector

        val actor = injector.getInstance (classOf[ActAnnotationTestActor])

        UnitTestModule.actorManager.startActor (actor)

        // Initial values
        assertNull (actor.obj)
        assertNull (actor.str)
        assertEquals (actor.int, -1)

        // Test sending int
        actor ! 2
        sleep
        assertNull (actor.obj)
        assertNull (actor.str)
        assertEquals (actor.int, 2)

        // Test sending string
        actor ! "Hullo"
        assertNull (actor.obj)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)

        // Test sending object
        actor ! 'ArbObject
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        
        UnitTestModule.actorManager.stopActor (actor)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor1 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor1])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor2 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor2])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor3 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor3])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor4 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor4])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor5 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor5])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor6 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor6])
        assertTrue (false)
    }

    def sleep : Unit = Thread.sleep (1000)
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
    var obj : Object = null
    var int : Int = -1
    var str : String = null

    @Act
    def onObjectMessage (msg : Object) : Unit = {
        this.obj = msg
    }

    @Act
    def onIntegerMessage (msg : Int) : Unit = {
        this.int = msg
    }

    @Act
    def onStringMessage (msg : String) : Unit = {
        this.str = msg
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor1 extends HiPriorityActor {
    @Act
    def onMessage : Unit = {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor2 extends HiPriorityActor {
    @Act
    private def onMessage : Unit = {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor3 extends HiPriorityActor {
    @Act
    def onMessage (x : Int) : Int = x
}

/**
 * Invalid actor.
 */
class InvalidTestActor4 extends HiPriorityActor {
    @Act
    def onMessage (x : Int) : Unit = {}
    
    @Act
    def onMessage (x : String) : Unit = {}
}

/**
 * Invalid actor.
 */
class InvalidTestActor5 extends HiPriorityActor {
    @Act
    def onMessage (x : String, y : String) : Unit = {}
}

/**
 * Invalid actor.
 */
class InvalidTestActor6 extends HiPriorityActor {
    @Act
    def onMessage (x : String) : Unit = {}

    @Act
    def onMessage2 (x : String) : Unit = {}
}