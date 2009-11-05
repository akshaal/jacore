/**
 * Akshaal (C) 2009. GNU GPL. http://akshaal.info
 */

package info.akshaal.jacore
package system
package test.unit.utils

import org.specs.SpecificationWithJUnit

import system.utils.LongValueFrame

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
    }

    /**
     * Test 1 count.
     * @throws Exception if something goes wrong
     */
    /*@Test (groups = Array("unit"))
    def oneWidth () = {
        val frame = new LongValueFrame (1)
        assertEquals (frame.average (), 0L)

        frame.put (1)
        assertEquals (frame.average (), 1L)

        frame.put (1)
        assertEquals (frame.average (), 1L)

        frame.put (2)
        assertEquals (frame.average (), 2L)

        frame.put (MAGIC_3)
        assertEquals (frame.average (), MAGIC_3)
    }*/

    /**
     * Test 2 count.
     * @throws Exception if something goes wrong
     */
    /*@Test (groups = Array("unit"))
    def twoWidth () = {
        val frame = new LongValueFrame (2)
        assertEquals (frame.average (), 0L)

        frame.put (1)
        assertEquals (frame.average (), 1L)

        frame.put (1)
        assertEquals (frame.average (), 1L)

        frame.put (2)
        assertEquals (frame.average (), 1L)

        frame.put (MAGIC_3)
        assertEquals (frame.average (), 2L)
    }*/

    /**
     * Test 3 count.
     * @throws Exception if something goes wrong
     */
    /*@Test (groups = Array("unit"))
    def threeWidth () = {
        val frame = new LongValueFrame (3)
        assertEquals (frame.average (), 0L)

        frame.put (1)
        assertEquals (frame.average (), 1L)

        frame.put (1)
        assertEquals (frame.average (), 1L)

        frame.put (2)
        assertEquals (frame.average (), 1L)

        frame.put (MAGIC_3)
        assertEquals (frame.average (), 2L)
    }*/
}