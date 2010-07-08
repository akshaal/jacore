/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils

import unit.UnitTestHelper._

import utils.DoubleValueFrameNaNIgnored

/**
 * Test Double Value Frame.
 */
class DoubleValuesFrameNaNIgnoredTest extends JacoreSpecWithJUnit ("DoubleValueFrameNaNIgnored class specification")
{
    "DoubleValueFrameNaNIgnored" should {
        "fail with zero width" in {
            new DoubleValueFrameNaNIgnored (0) must throwA[IllegalArgumentException]
        }

        "fail with negative width" in {
            new DoubleValueFrameNaNIgnored (-1) must throwA[IllegalArgumentException]
        }

        "support frames with size 1" in {
            val frame = new DoubleValueFrameNaNIgnored (1)
            frame.average.isNaN mustBe true
            frame.full must_== false
            frame.current.isNaN mustBe true
            frame.oldest.isNaN mustBe true

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
            frame.average must_== 3.0d
            frame.current must_== 3.0d
            frame.oldest must_== 3.0d

            frame.put (Double.NaN)
            frame.average.isNaN must_== true
            frame.current.isNaN must_== true
            frame.oldest.isNaN must_== true
        }

        "support frames with size 2" in {
            val frame = new DoubleValueFrameNaNIgnored (2)
            frame.average.isNaN mustBe true
            frame.full must_== false
            frame.current.isNaN mustBe true
            frame.oldest.isNaN mustBe true

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

            frame.put (Double.NaN)
            frame.average must_== 3.0d
            frame.full must_== true
            frame.current.isNaN must_== true
            frame.oldest must_== 3.0d

            frame.put (Double.NaN)
            frame.full must_== true
            frame.average.isNaN must_== true
            frame.current.isNaN must_== true
            frame.oldest.isNaN must_== true

            frame.put (Double.NaN)
            frame.full must_== true
            frame.average.isNaN must_== true
            frame.current.isNaN must_== true
            frame.oldest.isNaN must_== true

            frame.put (4.0d)
            frame.average must_== 4.0d
            frame.full must_== true
            frame.current must_== 4.0d
            frame.oldest.isNaN must_== true

            frame.put (2.0d)
            frame.average must_== 3.0d
            frame.full must_== true
            frame.current must_== 2.0d
            frame.oldest must_== 4.0d
        }

        "support frames with size 3" in {
            val frame = new DoubleValueFrameNaNIgnored (3)
            frame.average.isNaN mustBe true
            frame.full must_== false
            frame.current.isNaN mustBe true
            frame.oldest.isNaN mustBe true

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

            frame.put (Double.NaN)
            frame.average must_== 5.0d / 2.0d
            frame.full must_== true
            frame.current.isNaN must_== true
            frame.oldest must_== 2.0d
        }
    }
}
