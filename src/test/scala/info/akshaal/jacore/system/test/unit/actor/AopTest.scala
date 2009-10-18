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
import info.akshaal.jacore.system.actor.MessageExtractor
import java.math.BigInteger
import org.testng.Assert._
import system.annotation.{CallByMessage, Act, ExtractBy}
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
        assertFalse (actor.emptyString)

        // Test sending int
        actor ! 2
        sleep
        assertNull (actor.obj)
        assertNull (actor.str)
        assertEquals (actor.int, 2)
        assertFalse (actor.emptyString)

        // Test sending string
        actor ! "Hullo"
        sleep
        assertTrue (actor.never1)
        assertNull (actor.obj)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertFalse (actor.emptyString)

        // Test sending object
        actor ! 'ArbObject
        sleep
        assertTrue (actor.never1)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertFalse (actor.emptyString)

        // Test sending empty string
        actor ! ""
        sleep
        assertTrue (actor.never1)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertTrue (actor.emptyString)
        
        UnitTestModule.actorManager.stopActor (actor)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor1 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor1])
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

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor7 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor7])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor8 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor8])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor9 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor9])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor10 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor10])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor11 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor11])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor12 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor12])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor13 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor13])
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
    var emptyString = false
    var never1 = true

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

    @Act
    def onEmptyStringMessageWider (msg : String,
                              @ExtractBy(classOf[EmptyStringExtractor]) s : Object) : Unit =
    {
        this.never1 = false
    }

    @Act
    def onEmptyStringMessage (msg : String,
                              @ExtractBy(classOf[EmptyStringExtractor]) s : String) : Unit =
    {
        this.emptyString = true
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

/**
 * Invalid actor.
 */
class InvalidTestActor7 extends HiPriorityActor {
    @Act
    def onMessage (msg : Object,
                   @ExtractBy(classOf[StringIdentityExtractor]) y : String) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor8 extends HiPriorityActor {
    @Act
    def onMessage (msg : String,
                   @ExtractBy(classOf[StringIdentityExtractor]) y : String,
                   @ExtractBy(classOf[StringIdentityExtractor]) z : String) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor9 extends HiPriorityActor {
    @Act
    def onMessage (msg : String,
                   @ExtractBy(classOf[StringIdentityExtractor]) y : Int) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor10 extends HiPriorityActor {
    @Act
    def onMessage (msg : String,
                   @ExtractBy(classOf[StringIdentityExtractor]) y : String) : Unit =
    {
    }

    @Act
    def onMessage2 (msg : String,
                    @ExtractBy(classOf[StringIdentityExtractor]) y : String) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor11 extends HiPriorityActor {
    @Act
    def onMessage2 (msg : String,
                    @ExtractBy(classOf[BadExtractor]) y : String) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor12 extends HiPriorityActor {
    @Act
    def onMessage (msg : BigInteger,
                   @ExtractBy(classOf[StringIdentityExtractor]) y : String) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor13 extends HiPriorityActor {
    @Act
    def onMessage (msg : String,
                   @ExtractBy(classOf[StringIdentityExtractor]) y : BigInteger) : Unit =
    {
    }
}

class StringIdentityExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = msg
}

class EmptyStringExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = if (msg.isEmpty) "" else null
}

class BadExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = msg
    def extractFrom (msg : java.lang.Integer) = msg
}