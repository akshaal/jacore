/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.frame

import unit.UnitTestHelper._

import utils.frame.OptionDoubleValueFrame

/**
 * Test Double Value Frame.
 */
class OptionDoubleValuesFrameTest extends JacoreSpecWithJUnit ("OptionDoubleValueFrame class specification")
{
    "OptionDoubleValueFrame" should {
        "fail with zero width" in {
            new OptionDoubleValueFrame (0) must throwA[IllegalArgumentException]
        }

        "fail with negative width" in {
            new OptionDoubleValueFrame (-1) must throwA[IllegalArgumentException]
        }

        "support frames with size 1" in {
            val frame = new OptionDoubleValueFrame (1)
            frame.average must_== None
            frame.full must_== false
            frame.current must_== None
            frame.oldest must_== None

            frame.put (Some (1.0d))
            frame.average must_== Some(1.0d)
            frame.full must_== true
            frame.current must_== Some(1.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (Some (1.0d))
            frame.average must_== Some(1.0d)
            frame.full must_== true
            frame.current must_== Some(1.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (Some (2.0d))
            frame.average must_== Some(2.0d)
            frame.full must_== true
            frame.current must_== Some(2.0d)
            frame.oldest must_== Some(2.0d)

            frame.put (Some (3.0d))
            frame.average must_== Some(3.0d)
            frame.current must_== Some(3.0d)
            frame.oldest must_== Some(3.0d)

            frame.put (None)
            frame.average must_== None
            frame.current must_== None
            frame.oldest must_== None
        }

        "support frames with size 2" in {
            val frame = new OptionDoubleValueFrame (2)
            frame.average must_== None
            frame.full must_== false
            frame.current must_== None
            frame.oldest must_== None

            frame.put (Some(1.0d))
            frame.average must_== Some (1.0d)
            frame.full must_== false
            frame.current must_== Some(1.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (Some(1.0d))
            frame.average must_== Some (1.0d)
            frame.full must_== true
            frame.current must_== Some (1.0d)
            frame.oldest must_== Some (1.0d)

            frame.put (Some (2.0d))
            frame.average must_== Some (3.0d / 2.0d)
            frame.full must_== true
            frame.current must_== Some (2.0d)
            frame.oldest must_== Some (1.0d)

            frame.put (Some (3.0d))
            frame.average must_== Some (5.0d / 2.0d)
            frame.full must_== true
            frame.current must_== Some (3.0d)
            frame.oldest must_== Some (2.0d)

            frame.put (None)
            frame.average must_== Some (3.0d)
            frame.full must_== true
            frame.current must_== None
            frame.oldest must_== Some (3.0d)

            frame.put (None)
            frame.full must_== true
            frame.average must_== None
            frame.current must_== None
            frame.oldest must_== None

            frame.put (None)
            frame.full must_== true
            frame.average must_== None
            frame.current must_== None
            frame.oldest must_== None

            frame.put (Some(4.0d))
            frame.average must_== Some(4.0d)
            frame.full must_== true
            frame.current must_== Some(4.0d)
            frame.oldest must_== None

            frame.put (Some(2.0d))
            frame.average must_== Some(3.0d)
            frame.full must_== true
            frame.current must_== Some(2.0d)
            frame.oldest must_== Some(4.0d)
        }

        "support frames with size 3" in {
            val frame = new OptionDoubleValueFrame (3)
            frame.average must_== None
            frame.full must_== false
            frame.current must_== None
            frame.oldest must_== None

            frame.put (Some(1.0d))
            frame.average must_== Some(1.0d)
            frame.full must_== false
            frame.current must_== Some(1.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (Some(1.0d))
            frame.average must_== Some(1.0d)
            frame.full must_== false
            frame.current must_== Some(1.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (Some(2.0d))
            frame.average must_== Some(4.0d / 3.0d)
            frame.full must_== true
            frame.current must_== Some(2.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (Some(3.0d))
            frame.average must_== Some(6.0d / 3.0d)
            frame.full must_== true
            frame.current must_== Some(3.0d)
            frame.oldest must_== Some(1.0d)

            frame.put (None)
            frame.average must_== Some(5.0d / 2.0d)
            frame.full must_== true
            frame.current must_== None
            frame.oldest must_== Some(2.0d)
        }
    }
}
