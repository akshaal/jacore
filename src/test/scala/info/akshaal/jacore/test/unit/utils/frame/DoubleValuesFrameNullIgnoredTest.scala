/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.frame

import unit.UnitTestHelper._

import utils.frame.DoubleValueFrameNullIgnored

/**
 * Test Double Value Frame.
 */
class DoubleValuesFrameNullIgnoredTest extends JacoreSpecWithJUnit ("DoubleValueFrameNullIgnored class specification")
{
    "DoubleValueFrameNullIgnored" should {
        "fail with zero width" in {
            new DoubleValueFrameNullIgnored (0) must throwA[IllegalArgumentException]
        }

        "fail with negative width" in {
            new DoubleValueFrameNullIgnored (-1) must throwA[IllegalArgumentException]
        }

        "support frames with size 1" in {
            val frame = new DoubleValueFrameNullIgnored (1)
            frame.average must beNull
            frame.full must_== false
            frame.current must beNull
            frame.oldest must beNull

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.full must_== true
            frame.current must_== 1.0d
            frame.oldest must_== 1.0d

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.full must_== true
            frame.current must_== 1.0d
            frame.oldest must_== 1.0d

            frame.put (2.0d)
            frame.average must_== 2.0d
            frame.full must_== true
            frame.current must_== 2.0d
            frame.oldest must_== 2.0d

            frame.put (3.0d)
            frame.full must_== true
            frame.average must_== 3.0d
            frame.current must_== 3.0d
            frame.oldest must_== 3.0d

            frame.put (null)
            frame.full must_== true
            frame.average must beNull
            frame.current must beNull
            frame.oldest must beNull
        }

        "support frames with size 2" in {
            val frame = new DoubleValueFrameNullIgnored (2)
            frame.average must beNull
            frame.full must_== false
            frame.current must beNull
            frame.oldest must beNull

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.full must_== false
            frame.current must_== 1.0d
            frame.oldest must_== 1.0d

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.full must_== true
            frame.current must_== 1.0d
            frame.oldest must_== 1.0d

            frame.put (2.0d)
            frame.average must_== 3.0d / 2.0d
            frame.full must_== true
            frame.current must_== 2.0d
            frame.oldest must_== 1.0d

            frame.put (3.0d)
            frame.average must_== 5.0d / 2.0d
            frame.full must_== true
            frame.current must_== 3.0d
            frame.oldest must_== 2.0d

            frame.put (null)
            frame.average must_== 3.0d
            frame.full must_== true
            frame.current must beNull
            frame.oldest must_== 3.0d

            frame.put (null)
            frame.full must_== true
            frame.average must beNull
            frame.current must beNull
            frame.oldest must beNull

            frame.put (null)
            frame.full must_== true
            frame.average must beNull
            frame.current must beNull
            frame.oldest must beNull

            frame.put (4.0d)
            frame.average must_== 4.0d
            frame.full must_== true
            frame.current must_== 4.0d
            frame.oldest must beNull

            frame.put (2.0d)
            frame.average must_== 3.0d
            frame.full must_== true
            frame.current must_== 2.0d
            frame.oldest must_== 4.0d
        }

        "support frames with size 3" in {
            val frame = new DoubleValueFrameNullIgnored (3)
            frame.average must beNull
            frame.full must_== false
            frame.current must beNull
            frame.oldest must beNull

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.full must_== false
            frame.current must_== 1.0d
            frame.oldest must_== 1.0d

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.full must_== false
            frame.current must_== 1.0d
            frame.oldest must_== 1.0d

            frame.put (2.0d)
            frame.average must_== 4.0d / 3.0d
            frame.full must_== true
            frame.current must_== 2.0d
            frame.oldest must_== 1.0d

            frame.put (3.0d)
            frame.average must_== 6.0d / 3.0d
            frame.full must_== true
            frame.current must_== 3.0d
            frame.oldest must_== 1.0d

            frame.put (null)
            frame.average must_== 5.0d / 2.0d
            frame.full must_== true
            frame.current must beNull
            frame.oldest must_== 2.0d
        }
    }
}
