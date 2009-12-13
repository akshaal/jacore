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
final class LongValueFrame (maximum : Int) extends NotNull {
    require (maximum > 0, "maximum must be positive number")

    private val array = new Array[Long] (maximum)
    private var pos = -1
    private var count = 0
    private var sum = 0L

    /**
     * Add value.
     * @param value value
     */
    def put (value : Long) = {
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
    def average () : Long = {
        if (count == 0) 0L else sum / count
    }

    /**
     * Returns current count.
     */
    def currentCount : Int = count

    /**
     * Returns current sum.
     */
    def currentSum : Long = sum
}
