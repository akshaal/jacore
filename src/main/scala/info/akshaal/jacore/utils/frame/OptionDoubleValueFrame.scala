/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils
package frame

/**
 * Frame of values with maximum fixed number of elements.
 * Frame maintains sum of elements. None values are supported.
 */
final class OptionDoubleValueFrame (maximum : Int) extends NotNull {
    require (maximum > 0, "maximum must be positive number")

    private val array = new Array[Option[Double]] (maximum)
    private var pos = -1
    private var count = 0
    private var sum = 0d
    private var nans = 0

    /**
     * Add value.
     * @param value value
     */
    def put (option : Option[Double]) = {
        pos += 1

        if (count < maximum) {
            count += 1

            option match {
                case None         => nans += 1
                case Some (value) => sum += value
            }

            array (pos) = option
        } else {
            if (pos == maximum) {
                pos = 0;
            }

            val old_slot_option = array(pos)
            option match {
                case None =>
                    for (old_slot_val <- old_slot_option) {
                        sum -= old_slot_val
                        nans += 1
                        array (pos) = option
                    }

                case Some (value) =>
                    old_slot_option match {
                        case None =>
                            sum += value
                            nans -= 1

                        case Some (old_slot_val) =>
                            sum += value - old_slot_val
                    }

                    array (pos) = option
            }
        }
    }

   /**
     * @return the average
     */
    def average () : Option[Double] = {
        val real_numbers = count - nans

        if (real_numbers == 0) {
            None
        } else {
            Some (sum / real_numbers)
        }
    }

    /**
     * Returns current count.
     */
    def currentCount : Int = count

    /**
     * Returns current sum.
     */
    def currentSum : Option[Double] = {
        val real_numbers = count - nans

        if (real_numbers == 0) {
            None
        } else {
            Some (sum)
        }
    }

    /**
     * Returns true if full.
     */
    def full : Boolean = return count >= maximum

    /**
     * Current (last inserted value).
     */
    def current : Option[Double] = if (count > 0) array (pos) else None

    /**
     * Oldest value.
     */
    def oldest : Option[Double] =
        if (full && pos != maximum - 1) {
            array (pos + 1)
        } else {
            if (count > 0) {
                array (0)
            } else {
                None
            }
        }
}
