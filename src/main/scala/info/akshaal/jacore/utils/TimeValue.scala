/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

/**
 * Class to represnets time value.
 *
 * @param nano time given in nanoseconds
 */
final class TimeValue (nano : Long) extends NotNull
{
    /**
     * Time value in nanoseconds.
     */
    @inline
    def inNanoseconds : Long = nano

    /**
     * Time value in microseconds.
     */
    lazy val inMicroseconds : Long = nano / TimeValue.nsInUs

    /**
     * Time value in milliseconds.
     */
    lazy val inMilliseconds : Long = nano / TimeValue.nsInMs

    /**
     * Time value in seconds.
     */
    lazy val inSeconds : Long = nano / TimeValue.nsInSec

    /**
     * Time value in minutes.
     */
    lazy val inMinutes : Long = nano / TimeValue.nsInMin

    /**
     * Time value in hours.
     */
    lazy val inHours : Long = nano / TimeValue.nsInHour

    /**
     * Time value in days.
     */
    lazy val inDays : Long = nano / TimeValue.nsInDay

    /**
     * {InheritedDoc}
     */
    override lazy val toString : String = {
        // Split into components
        var cur = nano
        var comps : List[String] = Nil

        for ((name : String, value : Long) <- TimeValue.units) {
            val div = cur / value
            cur = cur % value

            if (div != 0L) {
                val nameFixed =
                    if (div == 1 && name.length > 2)
                        name.substring (0, name.length - 1)
                    else
                        name

                comps = (div + nameFixed) :: comps
            }
        }

        // Return
        if (comps == Nil) "0ns" else comps.reverse.mkString(" ")
    }

    /**
     * Calculate sum of this time value and some other ('that') time value.
     * 
     * @param that value to add
     * @return sum
     */
    def + (that : TimeValue) : TimeValue = new TimeValue (nano + that.inNanoseconds)

    /**
     * Calculate difference between this time value and some other ('that') time value.    
     *
     * @param that value to substruct from this one
     * @return difference
     */
    def - (that : TimeValue) : TimeValue = new TimeValue (nano - that.inNanoseconds)

    /**
     * Multiply (upscale) this time value by the given value 'n'.
     * 
     * @param n a value to multiply by
     * @return scaled version of this time value
     */
    def * (n : Int) : TimeValue = new TimeValue (nano * n.asInstanceOf[Long])

    /**
     * Divide (downscale) this time value by the given value 'n'.
     *
     * @param n a value to divide by
     * @return scaled version of this time value
     */
    def / (n : Int) : TimeValue = new TimeValue (nano / n.asInstanceOf[Long])

    /**
     * Unary minus. Construct a new time from this one by multiplication by -1..
     *
     * @return negated version of this time value
     */
    def unary_-() : TimeValue = new TimeValue (-nano)

    /**
     * Rerturns true if other object might be equal to this one.
     * 
     * @param other value to test
     * @return true if this object can be compared with 'other' object
     */
    def canEqual (other : Any) : Boolean = other.isInstanceOf [TimeValue]

    /**
     * {InheritedDoc}
     */
    override def equals (that : Any) : Boolean =
        that match {
            case thatTimeValue : TimeValue =>
                canEqual (that) && nano == thatTimeValue.inNanoseconds

            case _ => false
        }

    /**
     * {InheritedDoc}
     */
    override def hashCode : Int = nano.asInstanceOf[Int]

    /**
     * Compare this time value to 'that' time value.
     *
     * @param that a value to compare with
     * @return 0 if both values are equal, negative integer if this value is less
     *           than 'that' or positive integer otherwise
     */
    def compare (that: TimeValue) : Int = nano compare that.inNanoseconds

    /**
     * Check whether this value is less or equal than that value.
     *
     * @param that value to compare with
     * @return true if this value is less or equal this this value
     */
    def <= (that: TimeValue) : Boolean = nano <= that.inNanoseconds

    /**
     * Check whether this value is greater or equal than that value.
     *
     * @param that value to compare with
     * @return true if this value is greater or equal than that value
     */ 
    def >= (that: TimeValue) : Boolean = nano >= that.inNanoseconds

    /**
     * Check whether this value is less than that value.
     *
     * @param that value to compare with
     * @return true if this value is less than this value
     */
    def < (that: TimeValue) : Boolean = nano < that.inNanoseconds

    /**
     * Check whether this value is greater than that value.
     *
     * @param that value to compare with
     * @return true if this value is greater than that value
     */ 
    def > (that: TimeValue) : Boolean = nano > that.inNanoseconds

    /**
     * Find smallest time value and return it.
     *
     * @return this time value if it is less than 'that' time value, return 'that' otherwise
     */
    def min (that: TimeValue) : TimeValue = if (nano < that.inNanoseconds) this else that

    /**
     * Find bigest time value and return it.
     *
     * @return this time value if it is greater than 'that' time value, return 'that' otherwise
     */
    def max (that: TimeValue) : TimeValue = if (nano > that.inNanoseconds) this else that
}

/**
 * Object with utils for TimeValue class.
 */
private[jacore] object TimeValue {
    private[utils] val nsInUs   = 1000L
    private[utils] val nsInMs   = 1000000L
    private[utils] val nsInSec  = 1000000000L
    private[utils] val nsInMin  = 60000000000L
    private[utils] val nsInHour = 3600000000000L
    private[utils] val nsInDay  = 86400000000000L

    private[utils] val units : List[(String, Long)] =
            List (("days", nsInDay),
                  ("hours", nsInHour),
                  ("mins", nsInMin),
                  ("secs", nsInSec),
                  ("ms", nsInMs),
                  ("us", nsInUs),
                  ("ns", 1L))

    /**
     * Constant holding maximum time value.
     */
    val MaxValue : TimeValue = new TimeValue (Long.MaxValue)

    /**
     * Constant holding minimum time value.
     */
    val MinValue : TimeValue = new TimeValue (Long.MinValue)

    /**
     * Parse string ot time unit.
     *
     * @param str string to parse
     * @return parsed string
     * @throws IllegalArgumentException if failed to parse
     */
    def parse (str : String) : TimeValue = TimeValueParser.parse (str)

    import scala.util.parsing.combinator._

    /**
     * Parser.
     */
    private object TimeValueParser extends JavaTokenParsers {
        def parse (str : String) : TimeValue =
            parseAll (expr, str) match {
               case Success (l, _) => l

               case Failure (m, _) =>
                   throw new IllegalArgumentException (
                              "Failed to parse time" +:+ str +:+ m)

               case Error (m, _)   =>
                   throw new IllegalArgumentException (
                              "Error while parsing time" +:+ str +:+ m)
            }

        def expr : Parser[TimeValue] =
            timeValue ~ rep(timeValue) ^^ {
                case u1 ~ l => l.foldLeft (u1) {_ + _}
            }

        def timeValue : Parser[TimeValue] = (
              decimalNumber <~ "seconds"      ^^ (_.toLong.seconds)
            | decimalNumber <~ "second"       ^^ (_.toLong.seconds)
            | decimalNumber <~ "milliseconds" ^^ (_.toLong.milliseconds)
            | decimalNumber <~ "millisecond"  ^^ (_.toLong.milliseconds)
            | decimalNumber <~ "nanoseconds"  ^^ (_.toLong.nanoseconds)
            | decimalNumber <~ "nanosecond"   ^^ (_.toLong.nanoseconds)
            | decimalNumber <~ "microseconds" ^^ (_.toLong.microseconds)
            | decimalNumber <~ "microsecond"  ^^ (_.toLong.microseconds)
            | decimalNumber <~ "hours"        ^^ (_.toLong.hours)
            | decimalNumber <~ "hour"         ^^ (_.toLong.hours)
            | decimalNumber <~ "days"         ^^ (_.toLong.days)
            | decimalNumber <~ "day"          ^^ (_.toLong.days)
            | decimalNumber <~ "minutes"      ^^ (_.toLong.minutes)
            | decimalNumber <~ "minute"       ^^ (_.toLong.minutes)
        )
    }
}
