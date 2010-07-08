/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

// We use custom double2Double method, so the default one must be hidden
import Predef.{require, double2Double => _}

/**
 * Frame of values with maximum fixed number of elements.
 * Frame maintains sum of elements.
 */
final class DoubleValueFrameNullIgnored (maximum : Int) extends NotNull {
    require (maximum > 0, "maximum must be positive number")

    private val backedFrame = new DoubleValueFrameNaNIgnored (maximum)

    private implicit def toBacked (value : java.lang.Double) : Double =
        if (value == null) Double.NaN else value.asInstanceOf[Double]

    private implicit def fromBacked (value : Double) : java.lang.Double = {
        if (value != value) null else java.lang.Double.valueOf (value)
    }

    /**
     * Add value.
     * @param value value
     */
    def put (value : java.lang.Double) = backedFrame.put (value)

   /**
     * @return the average
     */
    def average () : java.lang.Double = backedFrame.average

    /**
     * Returns current count.
     */
    def currentCount : Int = backedFrame.currentCount

    /**
     * Returns current sum.
     */
    def currentSum : java.lang.Double = backedFrame.currentSum

    /**
     * Returns true if full.
     */
    def full : Boolean = backedFrame.full

    /**
     * Current (last inserted value).
     */
    def current : java.lang.Double = backedFrame.current

    /**
     * Oldest value.
     */
    def oldest : java.lang.Double = backedFrame.oldest
}
