/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test
package unit.actor

import java.util.concurrent.{CountDownLatch, TimeUnit => JavaTimeUnit}

import org.specs.SpecificationWithJUnit
import org.specs.mock.Mockito
import com.google.inject.{ProvisionException, Inject}

import unit.UnitTestHelper._
import annotation.{CallByMessage, Act, ExtractBy}
import actor.MessageExtractor

class ActorTest extends SpecificationWithJUnit ("Actor specification") with Mockito {
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

                actor.waitForMessageAfter {actor ! "Hi"}

                actor.received  must_==  1
            })
        }

        "speak to other actors and get feedback" in {
            withStartedActors [SpeakingStringTestActor,
                               SpeakingTestActor] ((stringActor, speakingActor) => {
                speakingActor.stringMaker = Some (stringActor)

                speakingActor.waitForMessageBatchesAfter (2) {speakingActor ! 1}
                speakingActor.waitForMessageBatchesAfter (2) {speakingActor ! 3}
                speakingActor.waitForMessageBatchesAfter (2) {speakingActor ! 7}

                speakingActor.accuInt      must_==  List (7, 3, 1)
                speakingActor.accuString   must_==  List ("x7", "x3", "x1")
            })
        }

        "be exception resistant" in {
            withStartedActor [UnstableTestActor] (actor => {
                for (i <- 1 to 10) {
                    actor.waitForMessageAfter {actor ! i}
                }

                actor.sum  must_==  (1 + 3 + 5 + 7 + 9)
            })
        }

        "support methods with call by message style" in {
            withStartedActor [CallByMessageTestActor] (actor => {
                actor.sum  must_==  0
                actor.waitForMessageAfter {actor.inc ()}
                actor.sum  must_==  1
                actor.waitForMessageAfter {actor.inc ()}
                actor.sum  must_==  2
            })
        }

        "support chained call by message invocations" in {
            withNotStartedActor [CallByMessageChainedTestActor2] (actor2 => {
                withStartedActor [CallByMessageChainedTestActor] (actor => {
                    actor.sum  must_==  0
                    actor2.sum  must_==  0

                    actor.waitForMessageAfter {actor.inc (actor2)}

                    actor.sum  must_==  1
                    actor2.sum  must_==  0

                    actor.waitForMessageAfter {actor.inc (actor2)}

                    actor.sum  must_==  2
                    actor2.sum  must_==  0

                    actor2.waitForMessageAfter {actor2.start ()}

                    actor.sum  must_==  2
                    actor2.sum  must_==  2
               })
            })
        }

        "work when both @Act and @CallByMessage annotation present" in {
            withStartedActor [CallByMessageWithActTestActor] (actor => {
                actor.strCount  must_==  0
                actor.intCount  must_==  0
                actor.incCount  must_==  0

                actor.waitForMessageAfter {actor.inc}

                actor.strCount  must_==  0
                actor.intCount  must_==  0
                actor.incCount  must_==  1

                actor.waitForMessageAfter {actor ! "hi"}

                actor.strCount  must_==  1
                actor.intCount  must_==  0
                actor.incCount  must_==  1

                actor.waitForMessageAfter {actor ! 123}

                actor.strCount  must_==  1
                actor.intCount  must_==  1
                actor.incCount  must_==  1
            })
        }

        "work with protected methods annotated with @Act" in {
            withStartedActor [AdoptedProtectedTestActor] (actor => {
                actor.intReceived  must beFalse

                actor.waitForMessageAfter {actor ! 123}

                actor.intReceived  must beTrue
            })
        }

        "support inheritance" in {
            withStartedActor [InheritanceTestActor] (actor => {
                actor.intReceived  must beFalse
                actor.strReceived  must beFalse

                actor.waitForMessageAfter {actor ! "Hi"}

                actor.intReceived  must beFalse
                actor.strReceived  must beTrue

                actor.waitForMessageAfter {actor ! 123}

                actor.intReceived  must beTrue
                actor.strReceived  must beTrue
            })
        }

        "support inheritance with override of @Act method" in {
            withStartedActor [InheritanceWithOverrideTestActor] (actor => {
                actor.strReceived   must beFalse
                actor.intReceived   must beFalse
                actor.intReceived2  must beFalse

                actor.waitForMessageAfter {actor ! "Hi"}

                actor.strReceived   must beTrue
                actor.intReceived   must beFalse
                actor.intReceived2  must beFalse

                actor.waitForMessageAfter {actor ! 123}

                actor.strReceived   must beTrue
                actor.intReceived   must beFalse
                actor.intReceived2  must beTrue
            })
        }

        "have @Act annotation support" in {
            withStartedActor [ActAnnotationTestActor] (actor => {
                // Initial values
                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must beNull
                actor.str               must beNull
                actor.int               must_==  -1
                actor.emptyString       must_==  0
                actor.orderMsgReceived  must_==  0
                actor.justException     must_==  0
                actor.npeException      must_==  0

                // Test sending int
                actor.waitForMessageAfter {actor ! 2}

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must beNull
                actor.str               must beNull
                actor.int               must_==  2
                actor.emptyString       must_==  0
                actor.orderMsgReceived  must_==  0
                actor.justException     must_==  0
                actor.npeException      must_==  0

                // Test sending string
                actor.waitForMessageAfter {actor ! "Hello"}

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must beNull
                actor.str               must_==  "Hello"
                actor.int               must_==  2
                actor.emptyString       must_==  0
                actor.orderMsgReceived  must_==  0
                actor.justException     must_==  0
                actor.npeException      must_==  0

                // Test sending object
                actor.waitForMessageAfter {actor ! 'ArbObject}

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must_==  'ArbObject
                actor.str               must_==  "Hello"
                actor.int               must_==  2
                actor.emptyString       must_==  0
                actor.orderMsgReceived  must_==  0
                actor.justException     must_==  0
                actor.npeException      must_==  0

                // Test sending empty string
                actor.waitForMessageAfter {actor ! ""}

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must_==  'ArbObject
                actor.str               must_==  "Hello"
                actor.int               must_==  2
                actor.emptyString       must_==  1
                actor.orderMsgReceived  must_==  0
                actor.justException     must_==  0
                actor.npeException      must_==  0

                // Test sending some object
                actor.waitForMessageAfter {actor ! (new OrderTestObj(5))}

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must_==  'ArbObject
                actor.str               must_==  "Hello"
                actor.int               must_==  2
                actor.emptyString       must_==  1
                actor.orderMsgReceived  must_==  1
                actor.justException     must_==  0
                actor.npeException      must_==  0

                // Test sending runtime exception see if custom annotation works
                actor.waitForMessageAfter {
                    actor ! (new RuntimeException ("123", new NullPointerException ()))
                }

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must_==  'ArbObject
                actor.str               must_==  "Hello"
                actor.int               must_==  2
                actor.emptyString       must_==  1
                actor.orderMsgReceived  must_==  1
                actor.justException     must_==  0
                actor.npeException      must_==  1

                // Test sending runtime exception see if custom annotation is skipped
                actor.waitForMessageAfter {
                    actor ! (new RuntimeException ("123", new IllegalArgumentException ()))
                }

                actor.never1            must_==  0
                actor.never2            must_==  0
                actor.obj               must_==  'ArbObject
                actor.str               must_==  "Hello"
                actor.int               must_==  2
                actor.emptyString       must_==  1
                actor.orderMsgReceived  must_==  1
                actor.justException     must_==  1
                actor.npeException      must_==  1
            })
        }

        def mustBeInvalidActor[T <: TestActor](implicit clazz : ClassManifest[T]) : Unit = {
            withStartedActor [T] (actor => ()) (clazz) must throwA[ProvisionException]
        }

        "not allow @Act method without argument" in {
            mustBeInvalidActor [InvalidTestActorWithoutArg]
        }

        "not allow @Act method have extractor with overloaded extractFrom method" in {
            mustBeInvalidActor [InvalidTestActorWithExtractorWithOverload]
        }

        "not allow @Act method have return type" in {
            mustBeInvalidActor [InvalidTestActorWithReturn]
        }

        "not allow @Act method be overloaded" in {
            mustBeInvalidActor [InvalidTestActorWithOverload]
        }

        "not allow @Act method have more than one argument without extractor" in {
            mustBeInvalidActor [InvalidTestActorWithoutExtractor]
        }

        "not allow two or more @Act methods have the same message type" in {
            mustBeInvalidActor [InvalidTestActorWithSameMessageType]
        }

        "not allow @Act method use extractor with incompatible argument type" in {
            mustBeInvalidActor [InvalidTestActorWithIncompatibleExtractor]
        }

        "not allow @Act method use same extractor more than once" in {
            mustBeInvalidActor [InvalidTestActorWithDuplicatedExtractor]
        }

        "not allow @Act method use an extractor with incompatible return type" in {
            mustBeInvalidActor [InvalidTestActorWithParamTypeIncompatibleExtractor]
        }

        "not allow two or more @Act methods with the same message matcher set" in {
            mustBeInvalidActor [InvalidTestActorWithDuplicatedMessageMatcher]
        }

        "not allow more than one extractor for the same argument on @Act method" in {
            mustBeInvalidActor [InvalidTestActorWithMoreThanOneExtractorOnOneArg]
        }

        "not allow @Act method specify same extractor on one method by @ExtractBy and user defined annotation" in {
            mustBeInvalidActor [InvalidTestActorWithDuplicatedExtracorInDifferentForms]
        }

        "manage managed actors" in {
            var managingActor =
                withNotStartedActor [ManagingTestActor] (actor => {
                    actor ! 1
                    actor.managedTestActor ! 2

                    actor.starts                     must_==  0
                    actor.stops                      must_==  0
                    actor.received                   must_==  0
                    actor.managedTestActor.received  must_==  0
                    actor.managedTestActor.starts    must_==  0
                    actor.managedTestActor.stops     must_==  0

                    actor.waitForMessageAfter {
                        actor.managedTestActor.waitForMessageAfter {actor.start ()}
                    }

                    actor.starts                     must_==  1
                    actor.stops                      must_==  0
                    actor.received                   must_==  1
                    actor.managedTestActor.received  must_==  1
                    actor.managedTestActor.starts    must_==  1
                    actor.managedTestActor.stops     must_==  0

                    actor
                }).asInstanceOf[ManagingTestActor]

            managingActor.starts                     must_==  1
            managingActor.stops                      must_==  1
            managingActor.managedTestActor.starts    must_==  1
            managingActor.managedTestActor.stops     must_==  1
        }

        "execute actors concurrently" in {
            // We need this for test
            TestModule.hiPriorityPoolThreads  must beGreaterThan (1)
            ConcurrentLoadTestActor.MSGS      must beGreaterThan (1)

            // Test
            withStartedActors [ConcurrentLoadTestActor, ConcurrentLoadTestActor] (
                (actor1, actor2) => {
                    var expectedSum : Long = 0

                    for (i <- 1 to ConcurrentLoadTestActor.MSGS) {
                        expectedSum += i

                        actor1 ! i
                        actor2 ! i
                    }

                    for (actor <- List(actor1, actor2)) {
                        actor.msgsLatch.await (unit.UnitTestHelper.timeout.asMilliseconds,
                                               JavaTimeUnit.MILLISECONDS)
                    }

                    actor1.sum  must_==  expectedSum
                    actor2.sum  must_==  expectedSum
                }
            )
        }

        "postpone method execution without annotation" in {
            withNotStartedActor [PostponedTestActor] (actor => {
                actor.called  must_==  0
                actor.test ()
                actor.called  must_==  0

                actor.waitForMessageAfter {actor.start ()}

                actor.called  must_==  1

                actor.waitForMessageAfter {actor.test ()}

                actor.called  must_==  2
            })
        }

        "not allow code blocks injections for postpone" in {
            withNotStartedActors [PostponedTestActor, PostponedBadTestActor] (
                (actor, badActor) => {
                    actor.called  must_==  0
                    badActor.called  must_==  0

                    badActor.test (actor)

                    actor.waitForMessageAfter {
                        actor.start ()
                        badActor.start ()
                        actor.test ()
                    }

                    actor.called     must_==  1
                    badActor.called  must_==  0
                }
            )
        }

        "provide a convenient way to handle/passing message processing result" in {
            withNotStartedActors [ResponseRequesterTestActor, ResponserTestActor] (
                (reqActor, respActor) => {
                    reqActor.responses  must_==  0
                    reqActor.start
                    reqActor.responses  must_==  0

                    reqActor.waitForMessageAfter {
                        reqActor.request (respActor)
                    }
                    
                    reqActor.responses  must_==  0

                    reqActor.waitForMessageAfter {
                        respActor.start ()
                    }

                    reqActor.responses  must_==  1

                    reqActor.waitForMessageBatchesAfter (2) {
                        reqActor.request (respActor)
                    }

                    reqActor.responses  must_==  2
                }
            )
        }

        "provide a convenient way to get result of operation using Future object" in {
            withNotStartedActor [ResponserTestActor] (actor => {
                val future = actor.justCallBack runWithFutureAsy

                future.isDone  must beFalse
                Thread.sleep (50)
                future.isDone  must beFalse

                actor.start ()
                Thread.sleep (200)
                future.isDone  must beTrue
                future.get     must_== 123
            })
        }

        "cast exception if operation called more than once using Future" in {
            withNotStartedActor [ResponserTestActor] (actor => {
                val op = actor.justCallBack

                op.runWithFutureAsy

                op.runWithFutureAsy must throwA[UnrecoverableError]
            })
        }

        "cast exception if actor tries to yield result more than once" in {
            withStartedActor [ResponserDupTestActor] (actor => {
                actor.errors   must_== 0
                
                val future = actor.justCallBack runWithFutureAsy
                
                future.get     must_== 123

                Thread.sleep (50)
                actor.errors   must_== 1
            })
        }

        "cast exception if operation called more than one time" in {
            withNotStartedActors [ResponseRequesterDupTestActor, ResponserTestActor] (
                (reqActor, respActor) => {
                    reqActor.responses   must_==  0
                    reqActor.exceptions  must_==  0
                    reqActor.start
                    reqActor.responses   must_==  0
                    reqActor.exceptions  must_==  0

                    reqActor.waitForMessageAfter {
                        reqActor.request (respActor)
                    }

                    reqActor.responses   must_==  0
                    reqActor.exceptions  must_==  1

                    reqActor.waitForMessageAfter {
                        respActor.start ()
                    }

                    reqActor.responses   must_==  1
                    reqActor.exceptions  must_==  1

                    reqActor.waitForMessageBatchesAfter (2) {
                        reqActor.request (respActor)
                    }
                    
                    reqActor.responses   must_==  2
                    reqActor.exceptions  must_==  2
                }
            )
        }
    }
}

object ActorTest {
    class CallByMessageChainedTestActor extends TestActor {
        var sum = 0

        @CallByMessage
        def inc (actor : CallByMessageChainedTestActor2) = {
            sum = sum + 1
            actor.inc ()
        }
    }

    class CallByMessageChainedTestActor2 extends TestActor {
        var sum = 0

        @CallByMessage
        def inc () = {
            sum = sum + 1
        }
    }

    class ResponseRequesterDupTestActor extends TestActor {
        var responses = 0
        var exceptions = 0

        @CallByMessage
        def request (responser : ResponserTestActor) : Unit = {
            val cbk = responser.justCallBack

            try {
                cbk runMatchingResultAsy {
                    case i : Int => responses += 1
                }
            } catch {
                case e : UnrecoverableError => exceptions += 1
            }

            try {
                cbk runMatchingResultAsy {
                    case i : Int => responses += 1
                }
            } catch {
                case e : UnrecoverableError => exceptions += 1
            }
        }
    }

    class ResponseRequesterTestActor extends TestActor {
        var responses = 0
        
        @CallByMessage
        def request (responser : ResponserTestActor) : Unit = {
            responser.justCallBack runMatchingResultAsy {
                case i : Int =>
                    responses += 1
            }
        }
    }

    class ResponserTestActor extends TestActor {
        def justCallBack =
            new AbstractOperation [Int] {
                override def processRequest () {
                    yieldResult (123)
                }
            }
    }

    class ResponserDupTestActor extends TestActor {
        var errors = 0

        def justCallBack =
            new AbstractOperation [Int] {
                override def processRequest () {
                    try {
                        yieldResult (123)
                    } catch {
                        case e : UnrecoverableError => errors += 1
                    }

                    try {
                        yieldResult (123)
                    } catch {
                        case e : UnrecoverableError => errors += 1
                    }
                }
            }
    }

    class PostponedTestActor extends TestActor {
        var called = 0
        
        def test () : Unit = {
            postponed {
                called += 1
            }
        }
    }

    class PostponedBadTestActor extends TestActor {
        var called = 0

        def test (someOtherActor : TestActor) : Unit = {
            val block = PostponedBlock (() => called += 1)

            someOtherActor ! block
        }
    }

    class ConcurrentLoadTestActor extends TestActor {
        import ConcurrentLoadTestActor._

        var msgsLatch : CountDownLatch = new CountDownLatch (MSGS)
        var sum : Long = 0

        @Act
        def handle (msg : Int) : Unit = {
            sum += msg
            msgsLatch.countDown
        }
    }

    object ConcurrentLoadTestActor {
        val MSGS = 10000
    }

    class ManagingTestActor @Inject() (val managedTestActor : ManagedTestActor) extends TestActor {
        manage (managedTestActor)

        var received = 0
        var starts = 0
        var stops = 0

        override def start () : Boolean = {
            val r = super.start ()
            starts += 1
            r
        }

        override def stop () : Boolean = {
            val r = super.stop ()
            stops += 1
            r
        }

        override def act () = {
            case i : Int => received += 1
        }
    }

    class ManagedTestActor extends TestActor {
        var received = 0
        var starts = 0
        var stops = 0

        override def start () : Boolean = {
            val r = super.start ()
            starts += 1
            r
        }

        override def stop () : Boolean = {
            val r = super.stop ()
            stops += 1
            r
        }

        override def act () = {
            case i : Int => received += 1
        }
    }

    class ConstructionTestActor extends TestActor

    class MessageReceivingTestActor extends TestActor {
        var received = 0

        override def act () = {
            case str : String => received += 1
        }
    }

    class SpeakingTestActor extends TestActor {
        var accuString : List[String] = Nil
        var accuInt : List[Int] = Nil
        var stringMaker : Option[TestActor] = None

        override def act () = {
            case x : Int => {
                accuInt = x :: accuInt
                stringMaker.foreach (_ ! x)
            }

            case x : String => {
                accuString = x :: accuString
            }
        }
    }

    class SpeakingStringTestActor extends TestActor {
        override def act () = {
            case x => sender.foreach (_ ! ("x" + x))
        }
    }

    class UnstableTestActor extends TestActor {
        var sum = 0

        override def act () = {
            case x : Int => {
                if (x % 2 == 0) {
                    throw new IllegalArgumentException ()
                } else {
                    sum += x
                }
            }
        }
    }

    class CallByMessageTestActor extends TestActor {
        var sum = 0

        @CallByMessage
        def inc () = {
            sum = sum + 1
        }
    }

    class CallByMessageWithActTestActor extends TestActor {
        var incCount = 0
        var strCount = 0
        var intCount = 0

        @CallByMessage
        def inc () = {
            incCount += 1
        }

        @Act
        def strHandler (str : String) = {
            strCount += 1
        }

        override def act = {
            case x : Int => intCount += 1
        }
    }

    class AdoptedProtectedTestActor extends ProtectedTestActor (TestModule.hiPriorityActorEnv)
                                    with Waitable

    class InheritanceTestActor extends ProtectedTestActor (TestModule.hiPriorityActorEnv)
                               with Waitable
    {
        var strReceived = false

        @Act
        def stringHandler (str : String) : Unit = {
            strReceived = true
        }
    }

    class InheritanceWithOverrideTestActor
                                extends ProtectedTestActor (TestModule.hiPriorityActorEnv)
                                with Waitable
    {
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

    class ActAnnotationTestActor extends TestActor {
        var obj : Object = null
        var int : Int = -1
        var str : String = null
        var emptyString = 0
        var never1 = 0
        var never2 = 0
        var orderNotZero = 0
        var orderMsgReceived = 0
        var justException = 0
        var npeException = 0

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
            this.never1 += 1
        }

        @Act
        def onEmptyStringMessage (msg : String,
                                  @ExtractBy(classOf[EmptyStringExtractor]) s : String) : Unit =
        {
            this.emptyString += 1
        }

        @Act (suborder = 0)
        def onOrderObjMessage (msg : OrderTestObj) : Unit =
        {
            this.orderMsgReceived += 1
        }

        @Act (suborder = 1)
        def onOrderObjMessage2 (msg : OrderTestObj,
                                @ExtractBy(classOf[OrderExtractor]) s : Int) : Unit =
        {
            this.never2 += 1
        }

        @Act
        def onNpeException (msg : Exception,
                            @CauseExtractTestAnnotation cause : NullPointerException) : Unit =
        {
            this.npeException += 1
        }

        @Act
        def onException (msg : Exception) : Unit =
        {
            this.justException += 1
        }
    }

    case class OrderTestObj (integer : Int)

    class InvalidTestActorWithoutArg extends TestActor {
        @Act
        def onMessage : Unit = {
        }
    }

    class InvalidTestActorWithExtractorWithOverload extends TestActor {
        @Act
        def onMessage2 (msg : String,
                        @ExtractBy(classOf[BadExtractorWithOverload]) y : String) : Unit =
        {
        }
    }

    class InvalidTestActorWithReturn extends TestActor {
        @Act
        def onMessage (x : Int) : Int = x
    }

    class InvalidTestActorWithOverload extends TestActor {
        @Act
        def onMessage (x : Int) : Unit = {}

        @Act
        def onMessage (x : String) : Unit = {}
    }

    class InvalidTestActorWithoutExtractor extends TestActor {
        @Act
        def onMessage (x : String, y : String) : Unit = {}
    }

    class InvalidTestActorWithSameMessageType extends TestActor {
        @Act
        def onMessage (x : String) : Unit = {}

        @Act
        def onMessage2 (x : String) : Unit = {}
    }

    class InvalidTestActorWithIncompatibleExtractor extends TestActor {
        @Act
        def onMessage (msg : Object,
                       @ExtractBy(classOf[StringIdentityExtractor]) y : String) : Unit =
        {
        }
    }

    class InvalidTestActorWithDuplicatedExtractor extends TestActor {
        @Act
        def onMessage (msg : String,
                       @ExtractBy(classOf[StringIdentityExtractor]) y : String,
                       @ExtractBy(classOf[StringIdentityExtractor]) z : String) : Unit =
        {
        }
    }

    class InvalidTestActorWithParamTypeIncompatibleExtractor extends TestActor {
        @Act
        def onMessage (msg : String,
                       @ExtractBy(classOf[StringIdentityExtractor]) y : Int) : Unit =
        {
        }
    }

    class InvalidTestActorWithDuplicatedMessageMatcher extends TestActor {
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

    class InvalidTestActorWithMoreThanOneExtractorOnOneArg extends TestActor {
        @Act
        def onMessage (msg : Exception,
                       @ExtractBy(classOf[CauseExtractorExample])
                       @CauseExtractTestAnnotation y : Exception) : Unit =
        {
        }
    }

    class InvalidTestActorWithDuplicatedExtracorInDifferentForms extends TestActor {
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

    class BadExtractorWithOverload extends MessageExtractor[String, String] {
        override def extractFrom (msg : String) = msg
        def extractFrom (msg : java.lang.Integer) = msg
    }

    class BadExtractor2 (s : String) extends MessageExtractor[String, String] {
        override def extractFrom (msg : String) = msg
    }
}
