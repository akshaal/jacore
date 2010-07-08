/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils

import unit.UnitTestHelper._

class JacoreEnumTest extends JacoreSpecWithJUnit ("JacoreEnum class specification") {
    "JacoreEnum" should {
        "work" in {
            object Enum extends JacoreEnum (initial = 0) {
                class Type extends Value

                val a = new Type
                val b = new Type
            }

            Enum.a.id  must_==  0
            Enum.b.id  must_==  1
        }
    }
}
