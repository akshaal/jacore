/*
 * ActorTest.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit.actor

import java.io.IOException
import org.testng.annotations.Test
import java.math.BigInteger
import org.testng.Assert._
import com.google.inject.ProvisionException

import system.test.unit.{BaseUnitTest, UnitTestModule, HiPriorityActor}
import system.actor.MessageExtractor
import system.annotation.{CallByMessage, Act, ExtractBy}

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

        aopTestActor.start
        
        sleep
        assertEquals (aopTestActor.sum, 1)

        aopTestActor.inc ()

        sleep
        assertEquals (aopTestActor.sum, 2)

        aopTestActor.stop
    }

    @Test (groups=Array("unit"))
    def testJavaProtectedMethodAct () = {
        val injector = UnitTestModule.injector

        val actor = injector.getInstance (classOf[ProtectedTestActor])

        actor.start
        assertFalse (actor.intReceived)

        actor ! "Hi"
        sleep
        assertFalse (actor.intReceived)

        actor ! 123
        sleep
        assertTrue (actor.intReceived)

        actor.stop
    }

    @Test (groups=Array("unit"))
    def testInheritance () = {
        val injector = UnitTestModule.injector

        val actor = injector.getInstance (classOf[InheritanceTestActor])

        actor.start
        assertFalse (actor.intReceived)
        assertFalse (actor.strReceived)

        actor ! "Hi"
        sleep
        assertFalse (actor.intReceived)
        assertTrue (actor.strReceived)

        actor ! 123
        sleep
        assertTrue (actor.intReceived)
        assertTrue (actor.strReceived)

        actor.stop
    }

    @Test (groups=Array("unit"))
    def testInheritance2 () = {
        val injector = UnitTestModule.injector

        val actor = injector.getInstance (classOf[InheritanceTestActor2])

        actor.start
        assertFalse (actor.intReceived2)
        assertFalse (actor.intReceived)
        assertFalse (actor.strReceived)

        actor ! "Hi"
        sleep
        assertFalse (actor.intReceived2)
        assertFalse (actor.intReceived)
        assertTrue (actor.strReceived)

        actor ! 123
        sleep
        assertTrue (actor.intReceived2)
        assertFalse (actor.intReceived)
        assertTrue (actor.strReceived)

        actor.stop
    }


    @Test (groups=Array("unit"))
    def testActAnnotation () = {
        val injector = UnitTestModule.injector

        val actor = injector.getInstance (classOf[ActAnnotationTestActor])

        actor.start

        // Initial values
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertNull (actor.obj)
        assertNull (actor.str)
        assertEquals (actor.int, -1)
        assertFalse (actor.emptyString)
        assertFalse (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertFalse (actor.npeException)

        // Test sending int
        actor ! 2
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertNull (actor.obj)
        assertNull (actor.str)
        assertEquals (actor.int, 2)
        assertFalse (actor.emptyString)
        assertFalse (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertFalse (actor.npeException)

        // Test sending string
        actor ! "Hullo"
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertNull (actor.obj)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertFalse (actor.emptyString)
        assertFalse (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertFalse (actor.npeException)

        // Test sending object
        actor ! 'ArbObject
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertFalse (actor.emptyString)
        assertFalse (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertFalse (actor.npeException)

        // Test sending empty string
        actor ! ""
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertTrue (actor.emptyString)
        assertFalse (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertFalse (actor.npeException)

        // Test sending some object
        actor ! (new OrderTestObj(5))
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertTrue (actor.emptyString)
        assertTrue (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertFalse (actor.npeException)

        // Test sending runtime exception see if custom annotation works
        actor ! (new RuntimeException ("123", new NullPointerException ()))
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertTrue (actor.emptyString)
        assertTrue (actor.orderMsgReceived)
        assertFalse (actor.justException)
        assertTrue (actor.npeException)

        // Test sending runtime exception see if custom annotation is skipped
        actor ! (new RuntimeException ("123", new IllegalArgumentException ()))
        sleep
        assertTrue (actor.never1)
        assertTrue (actor.never2)
        assertEquals (actor.obj, 'ArbObject)
        assertEquals (actor.str, "Hullo")
        assertEquals (actor.int, 2)
        assertTrue (actor.emptyString)
        assertTrue (actor.orderMsgReceived)
        assertTrue (actor.justException)
        assertTrue (actor.npeException)

        actor.stop
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

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor14 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor14])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testInvalidActor15 () = {
        UnitTestModule.injector.getInstance(classOf[InvalidTestActor15])
        assertTrue (false)
    }

    @Test (groups=Array("unit"), expectedExceptions = Array(classOf[ProvisionException]))
    def testPrivateTestActor () = {
        UnitTestModule.injector.getInstance(classOf[PrivateTestActor])
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

class InheritanceTestActor extends ProtectedTestActor (UnitTestModule.hiPriorityActorEnv) {
    var strReceived = false

    @Act
    def stringHandler (str : String) : Unit = {
        strReceived = true
    }
}

class InheritanceTestActor2 extends ProtectedTestActor (UnitTestModule.hiPriorityActorEnv) {
    var strReceived = false
    var intReceived2 = false

    @Act
    def stringHandler (str : String) : Unit = {
        strReceived = true
    }

    @Act
    override def test (msg : Int) : Unit = {
        intReceived2 = true
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
    var never2 = true
    var orderNotZero = false
    var orderMsgReceived = false
    var justException = false;
    var npeException = false;

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

    @Act (suborder = 0)
    def onOrderObjMessage (msg : OrderTestObj) : Unit =
    {
        this.orderMsgReceived = true
    }

    @Act (suborder = 1)
    def onOrderObjMessage2 (msg : OrderTestObj,
                            @ExtractBy(classOf[OrderExtractor]) s : Int) : Unit =
    {
        this.never2 = false
    }

    @Act
    def onNpeException (msg : Exception,
                        @CauseExtractTestAnnotation cause : NullPointerException) : Unit =
    {
        this.npeException = true
    }

    @Act
    def onException (msg : Exception) : Unit =
    {
        this.justException = true
    }
}

case class OrderTestObj (integer : Int)

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
    def onMessage2 (msg : String,
                    @ExtractBy(classOf[BadExtractor2]) y : String) : Unit =
    {
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

/**
 * Invalid actor.
 */
class InvalidTestActor14 extends HiPriorityActor {
    @Act
    def onMessage (msg : Exception,
                   @ExtractBy(classOf[CauseExtractorExample])
                   @CauseExtractTestAnnotation y : Exception) : Unit =
    {
    }
}

/**
 * Invalid actor.
 */
class InvalidTestActor15 extends HiPriorityActor {
    @Act
    def onMessage (msg : Exception,
                   @ExtractBy(classOf[CauseExtractorExample]) y : Exception,
                   @CauseExtractTestAnnotation z : Exception) : Unit =
    {
    }
}

class StringIdentityExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = msg
}

class EmptyStringExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = if (msg.isEmpty) "" else null
}

class OrderExtractor extends MessageExtractor[OrderTestObj, Int] {
    override def extractFrom (msg : OrderTestObj) = msg.integer
}

class BadExtractor extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = msg
    def extractFrom (msg : java.lang.Integer) = msg
}

class BadExtractor2 (s : String) extends MessageExtractor[String, String] {
    override def extractFrom (msg : String) = msg
}