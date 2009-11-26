/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test
package unit.jmx

import java.lang.management.ManagementFactory
import javax.management.{ObjectName, Attribute}

import org.specs.SpecificationWithJUnit

import jmx.{JmxAttr, JmxOper, SimpleJmx}

class JmxTest extends SpecificationWithJUnit ("JMX support specification") {
    def expose = addToSusVerb ("expose")

    "SimpleJmx" should expose {
        val srv = ManagementFactory.getPlatformMBeanServer()
        val obj = new ObjectName (JmxTestObject.jmxObjectName)
        doLast { JmxTestObject.unregisterJmxBean }

        "readable attributes" in {
            JmxTestObject.r              must_==  1
            srv.getAttribute (obj, "r")  must_==  1

            JmxTestObject.r = 55
            srv.getAttribute (obj, "r")  must_==  55
        }

        "writable attributes" in {
            JmxTestObject.w   must_==  2

            srv.setAttribute (obj, new Attribute ("w", 10))
            JmxTestObject.w   must_==  10
        }

        "read-write attributes" in {
            JmxTestObject.rw              must_==  3
            srv.getAttribute (obj, "rw")  must_==  3

            JmxTestObject.rw = 66
            srv.getAttribute (obj, "rw")  must_==  66

            srv.setAttribute (obj, new Attribute ("rw", 123))
            srv.getAttribute (obj, "rw")  must_== 123
            JmxTestObject.rw              must_== 123
        }

        "invokable methods" in {
            JmxTestObject.operCalled  must beFalse
            
            srv.invoke (obj, "invoke", Array(), Array())

            JmxTestObject.operCalled  must beTrue
        }
    }

    "SimpleJmx" should {
        "unregister jmx bean by call" in {
            val srv = ManagementFactory.getPlatformMBeanServer()
            val name = "jacore:name=jmxTestUnregister" + hashCode
            val objName = new ObjectName (name)
            val inst1 = new JmxTestUnregister (name)

            srv.getAttribute (objName, "r")  must_==  1
            srv.getAttribute (objName, "r")  must_==  2
            srv.getAttribute (objName, "r")  must_==  3

            inst1.unregisterJmxBean

            val inst2 = new JmxTestUnregister (name)

            srv.getAttribute (objName, "r")  must_==  1
            srv.getAttribute (objName, "r")  must_==  2
            srv.getAttribute (objName, "r")  must_==  3

            inst2.unregisterJmxBean
        }
    }

    /**
     * Test object.
     */
    object JmxTestObject extends SimpleJmx {
        var r = 1
        var w = 2
        var rw : Int = 3

        var operCalled = false

        override lazy val jmxObjectName = "jacore:name=jmxTestObject" + hashCode

        override lazy val jmxAttributes = List (
            JmxAttr ("r",    Some (() => r),   None),
            JmxAttr ("w",    None,             Some ((x : Int) => w = x)),
            JmxAttr ("rw",   Some(() => rw),   Some ((x : Int) => rw = x))
        )

        override lazy val jmxOperations = List (
            JmxOper ("invoke", () => operCalled = true)
        )
    }

    /**
     * Test object to test unregister
     */
    class JmxTestUnregister (val jmxObjectName : String) extends SimpleJmx {
        private var r = 0

        override lazy val jmxAttributes = List (
            JmxAttr ("r",    Some (() => {r+=1; r}),   None)
        )
    }
}
