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

    "ClassUtils.findResources" should {
        "find resources of file package" in {
            val urls = findResources ("info.akshaal.jacore.test.unit.utils.findclasses",
                                      this.getClass.getClassLoader,
                                      _ => true)

            urls.size  must beGreaterThan (3)
        }

        "find resources of jar package" in {
            val urls = findResources ("com.google.inject.name",
                                      this.getClass.getClassLoader,
                                      _ => true)

            urls.size  must beGreaterThan (3)
        }
    }

    "ClassUtils.findClasses" should {
        "find classes of file package" in {
            val classes = findClasses ("info.akshaal.jacore.test.unit.utils.findclasses",
                                       this.getClass.getClassLoader,
                                       _ => true)

            classes  must contain (classOf[findclasses.B])
            classes  must contain (classOf[findclasses.C])
            classes  must contain (classOf[findclasses.sub.A])
            classes  must contain (classOf[findclasses.sub.subsub.D])
            classes  must haveSize (4)
        }

        "find classes of file package filtering results" in {
            val classes = findClasses ("info.akshaal.jacore.test.unit.utils.findclasses",
                                       this.getClass.getClassLoader,
                                       classOf[findclasses.B].isAssignableFrom (_))

            classes  must contain (classOf[findclasses.B])
            classes  must contain (classOf[findclasses.C])
            classes  must contain (classOf[findclasses.sub.subsub.D])
            classes  must haveSize (3)
        }

        "find classes of jar package" in {
            val classes = findClasses ("com.google.inject.name",
                                       this.getClass.getClassLoader,
                                       _ => true)

            classes  must contain (classOf[com.google.inject.name.Named])
        }
    }

    "ClassUtils.isModule" should {
        "return true for scala object classes (modules)" in {
            isModule (ismodule.A.getClass)  must beTrue
            isModule (ismodule.A.B.getClass)  must beTrue
        }

        "return false for not scala object classes (modules)" in {
            isModule (classOf [ismodule.C])  must beFalse
            isModule (classOf [ismodule.A.D])  must beFalse
        }
    }

    "ClassUtils.getModuleInstance" should {
        "return module instance for object classes (modules)" in {
            (getModuleInstance (ismodule.A.getClass) == Some(ismodule.A))  must beTrue
            (getModuleInstance (ismodule.A.B.getClass) == Some(ismodule.A.B))  must beTrue
        }

        "return module None for not object classes (modules)" in {
            (getModuleInstance (classOf [ismodule.C])  == None)  must beTrue
            (getModuleInstance (classOf [ismodule.A.D])  == None)  must beTrue
        }
    }
}

package ismodule {
    object A {
        object B
        
        class D
    }

    class C
}

package findclasses {
    package sub {
        class A

        package subsub {
            class D extends B
        }
    }

    class B

    class C extends B
}