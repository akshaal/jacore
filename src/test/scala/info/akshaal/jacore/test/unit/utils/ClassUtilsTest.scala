/**
 * Akshaal (C) 2009. GNU GPL. http://akshaal.info
 */

package info.akshaal.jacore
package test.unit.utils

import org.specs.SpecificationWithJUnit

import utils.ClassUtils._

/**
 * Test Long Value Frame.
 */
class ClassUtilsTest extends SpecificationWithJUnit ("ClassUtils specification") {
    "ClassUtils.box" should {
        "box primitive java types to classes" in {
            box (classOf [ClassUtilsTest]).asInstanceOf[Object]  must_==  classOf [ClassUtilsTest]
            box (java.lang.Boolean.TYPE).asInstanceOf[Object]    must_==  classOf [java.lang.Boolean]
            box (java.lang.Byte.TYPE).asInstanceOf[Object]       must_==  classOf [java.lang.Byte]
            box (java.lang.Character.TYPE).asInstanceOf[Object]  must_==  classOf [java.lang.Character]
            box (java.lang.Double.TYPE).asInstanceOf[Object]     must_==  classOf [java.lang.Double]
            box (java.lang.Float.TYPE).asInstanceOf[Object]      must_==  classOf [java.lang.Float]
            box (java.lang.Integer.TYPE).asInstanceOf[Object]    must_==  classOf [java.lang.Integer]
            box (java.lang.Long.TYPE).asInstanceOf[Object]       must_==  classOf [java.lang.Long]
            box (java.lang.Short.TYPE).asInstanceOf[Object]      must_==  classOf [java.lang.Short]
            box (java.lang.Void.TYPE).asInstanceOf[Object]       must_==  classOf [java.lang.Void]
        }
    }

    "ClassUtils.javaName2fsName" should {
        "convert fully qualified jave name to path" in {
            javaName2fsName ("aaa.bbb.CCC")  must_==  "aaa/bbb/CCC"
        }
    }

    "ClassUtils.fsName2javaName" should {
        "convert path to fully qualified jave name" in {
            fsName2javaName ("aaa/bbb/CCC")  must_==  "aaa.bbb.CCC"
        }
    }
}
