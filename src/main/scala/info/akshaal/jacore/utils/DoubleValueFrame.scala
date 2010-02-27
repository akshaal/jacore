/*
 * ValueFrame.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

/**
 * Frame of values with maximum fixed number of elements.
 * Frame maintains sum of elements.
 */
final class DoubleValueFrame (maximum : Int) extends NotNull {
    require (maximum > 0, "maximum must be positive number")

    private val array = new Array[Double] (maximum)
    private var pos = -1
    private var count = 0
    private var sum = 0d

    /**
     * Add value.
     * @param value value
     */
    def put (value : Double) = {
        pos += 1
        
        if (pos == maximum) {
            pos = 0;
        }

        if (count < maximum) {
            count += 1
            sum += value
        } else {
            sum += value - array(pos)
        }

        array (pos) = value
    }

   /**
     * @return the average
     */
    def average () : Double = {
        if (count == 0) Double.NaN else sum / count
    }

    /**
     * Returns current count.
     */
    def currentCount : Int = count

    /**
     * Returns current sum.
     */
    def currentSum : Double = sum

    /**
     * Returns true if full.
     */
    def full : Boolean = return count >= maximum

    /**
     * Current (last inserted value).
     */
    def current : Double = if (count > 0) array (pos) else Double.NaN

    /**
     * Oldest value.
     */
    def oldest : Double =
        if (full && pos != maximum - 1) {
            array (pos + 1)
        } else {
            if (count > 0) {
                array (0)
            } else {
                Double.NaN
            }
        }
}
