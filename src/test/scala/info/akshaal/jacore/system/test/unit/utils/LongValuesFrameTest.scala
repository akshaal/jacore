/**
 * Akshaal (C) 2009. GNU GPL. http://akshaal.info
 */

package info.akshaal.jacore
package system
package test.unit.utils

import system.utils.LongValueFrame
import system.test.unit.BaseUnitTest

import org.testng.annotations.Test
import org.testng.Assert._

/**
 * Test Long Value Frame.
 */
class LongValuesFrameTest extends BaseUnitTest {
    val MAGIC_3 = 3L
    val MAGIC_4 = 4L
    val MAGIC_5 = 5L
    val MAGIC_6 = 6L

    /**
     * Test zero count.
     * @throws Exception if something goes wrong
     */
    @Test (expectedExceptions = Array(classOf[IllegalArgumentException]),
           groups = Array("unit"))
    def zeroWidth () = {
        val frame = new LongValueFrame (0)
        frame.put (1)
        assertFalse (true)
    }

    /**
     * Test negative count.
     * @throws Exception if something goes wrong
     */
    @Test (expectedExceptions = Array(classOf[IllegalArgumentException]),
           groups = Array("unit"))
    def negativeWidth () = {
        val frame = new LongValueFrame (-2)
        frame.put (1)
        assertFalse (true)
    }

    /**
     * Test 1 count.
     * @throws Exception if something goes wrong
     */
    @Test (groups = Array("unit"))
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
    }

    /**
     * Test 2 count.
     * @throws Exception if something goes wrong
     */
    @Test (groups = Array("unit"))
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
    }

    /**
     * Test 3 count.
     * @throws Exception if something goes wrong
     */
    @Test (groups = Array("unit"))
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
    }
}