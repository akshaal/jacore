/**
 * Akshaal (C) 2009. GNU GPL. http://akshaal.info
 */

package info.akshaal.jacore
package test.unit.utils

import org.specs.SpecificationWithJUnit

import utils.DoubleValueFrame

/**
 * Test Double Value Frame.
 */
class DoubleValuesFrameTest extends SpecificationWithJUnit ("DoubleValueFrame class specification")
{
    "DoubleValueFrame" should {
        "fail with zero width" in {
            new DoubleValueFrame (0) must throwA[IllegalArgumentException]
        }

        "fail with negative width" in {
            new DoubleValueFrame (-1) must throwA[IllegalArgumentException]
        }

        "support frames with size 1" in {
            val frame = new DoubleValueFrame (1)
            frame.average must_== 0.0d
            frame.isFull must_== false

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.isFull must_== true

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.isFull must_== true

            frame.put (2.0d)
            frame.average must_== 2.0d
            frame.isFull must_== true

            frame.put (3.0d)
            frame.average must_== 3.0d
        }

        "support frames with size 2" in {
            val frame = new DoubleValueFrame (2)
            frame.average must_== 0.0d
            frame.isFull must_== false

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.isFull must_== false

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.isFull must_== true

            frame.put (2.0d)
            frame.average must_== 3.0d / 2.0d
            frame.isFull must_== true

            frame.put (3.0d)
            frame.average must_== 5.0d / 2.0d
            frame.isFull must_== true
        }

        "support frames with size 3" in {
            val frame = new DoubleValueFrame (3)
            frame.average must_== 0.0d
            frame.isFull must_== false

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.isFull must_== false

            frame.put (1.0d)
            frame.average must_== 1.0d
            frame.isFull must_== false

            frame.put (2.0d)
            frame.average must_== 4.0d / 3.0d
            frame.isFull must_== true

            frame.put (3.0d)
            frame.average must_== 6.0d / 3.0d
            frame.isFull must_== true
        }
    }
}
