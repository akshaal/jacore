/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.io.db

import unit.UnitTestHelper._

import utils.io.db.SqlUtils

class SqlUtilsTest extends JacoreSpecWithJUnit ("SqlUtils class specification") {
    import SqlUtils._

    "SqlUtils" should {
        "count placeholders" in {
            countPlaceholders ("select * from blah")  must_==  0
            countPlaceholders ("select * from blah where x=?")  must_==  1
            countPlaceholders ("select * from blah where x=? and y=?")  must_==  2
            countPlaceholders ("select * from blah where ?=?")  must_==  2
            countPlaceholders ("select * from blah where ?=? and ?=?")  must_==  4
            countPlaceholders ("select ? from blah where ?=? and ?=?")  must_==  5
            countPlaceholders ("select \"'?\\\"\" from blah where ?=? and ?=?")  must_==  4
            countPlaceholders ("select '?? ??  ?' from y")  must_==  0
            countPlaceholders ("select '?\\'?\" ?? \" ?' from y where x=?")  must_==  1
        }
    }
}
