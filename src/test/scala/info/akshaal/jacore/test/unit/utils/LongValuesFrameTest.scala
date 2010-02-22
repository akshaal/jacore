/**
 * Akshaal (C) 2009. GNU GPL. http://akshaal.info
 */

package info.akshaal.jacore
package test.unit.utils

import org.specs.SpecificationWithJUnit

import utils.LongValueFrame

/**
 * Test Long Value Frame.
 */
class LongValuesFrameTest extends SpecificationWithJUnit ("LongValueFrame class specification") {
    "LongValueFrame" should {
        "fail with zero width" in {
            new LongValueFrame (0) must throwA[IllegalArgumentException]
        }

        "fail with negative width" in {
            new LongValueFrame (-1) must throwA[IllegalArgumentException]
        }

        "support frames with size 1" in {
            val frame = new LongValueFrame (1)
            frame.average must_== 0
            frame.isFull must_== false

            frame.put (1)
            frame.average must_== 1
            frame.isFull must_== true

            frame.put (1)
            frame.average must_== 1
            frame.isFull must_== true

            frame.put (2)
            frame.average must_== 2
            frame.isFull must_== true

            frame.put (3)
            frame.average must_== 3
            frame.isFull must_== true
        }

        "support frames with size 2" in {
            val frame = new LongValueFrame (2)
            frame.average must_== 0
            frame.isFull must_== false

            frame.put (1)
            frame.average must_== 1
            frame.isFull must_== false

            frame.put (1)
            frame.average must_== 1
            frame.isFull must_== true

            frame.put (2)
            frame.average must_== 1
            frame.isFull must_== true

            frame.put (3)
            frame.average must_== 2
            frame.isFull must_== true
        }

        "support frames with size 3" in {
            val frame = new LongValueFrame (3)
            frame.average must_== 0
            frame.isFull must_== false

            frame.put (1)
            frame.average must_== 1
            frame.isFull must_== false

            frame.put (1)
            frame.average must_== 1
            frame.isFull must_== false

            frame.put (2)
            frame.average must_== 1
            frame.isFull must_== true

            frame.put (3)
            frame.average must_== 2
            frame.isFull must_== true
        }
    }
}
