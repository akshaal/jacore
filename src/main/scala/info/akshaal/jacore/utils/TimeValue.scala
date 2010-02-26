/*
 * ActorScheduling.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

final class TimeValue (nano : Long) extends NotNull
{
    @inline
    def asNanoseconds       = nano

    lazy val asMicroseconds = nano / TimeValue.nsInUs
    lazy val asMilliseconds = nano / TimeValue.nsInMs
    lazy val asSeconds      = nano / TimeValue.nsInSec
    lazy val asMinutes      = nano / TimeValue.nsInMin
    lazy val asHours        = nano / TimeValue.nsInHour
    lazy val asDays         = nano / TimeValue.nsInDay

    override lazy val toString = {
        // Split into components
        var cur = nano
        var comps : List[String] = Nil

        for ((name : String, value : Long) <- TimeValue.units) {
            val div = cur / value
            cur = cur % value

            if (div != 0L) {
                comps = (div + name) :: comps
            }
        }

        // Return
        if (comps == Nil) "0ns" else comps.reverse.mkString(" ")
    }

    def + (that : TimeValue) = new TimeValue (nano + that.asNanoseconds)
    def - (that : TimeValue) = new TimeValue (nano - that.asNanoseconds)
    def * (that : TimeValue) = new TimeValue (nano * that.asNanoseconds)
    def * (that : Int) = new TimeValue (nano * that.asInstanceOf[Long])
    def / (that : TimeValue) = new TimeValue (nano / that.asNanoseconds)
    def / (that : Int) = new TimeValue (nano / that.asInstanceOf[Long])

    override def equals (that : Any) = that match {
        case thatTimeValue : TimeValue => nano == thatTimeValue.asNanoseconds
    }

    override def hashCode : Int = nano.asInstanceOf[Int]

    def compare (that: TimeValue) : Int =
        this.asNanoseconds compare that.asNanoseconds

    def equals (that: TimeValue) : Boolean = compare(that) == 0
    def <= (that: TimeValue)     : Boolean = compare(that) <= 0
    def >= (that: TimeValue)     : Boolean = compare(that) >= 0
    def <  (that: TimeValue)     : Boolean = compare(that) < 0
    def >  (that: TimeValue)     : Boolean = compare(that) > 0
}

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
     * Parse string ot time unit.
     */
    def parse (str : String) : TimeValue = TimeValueParser.parse (str)

    import scala.util.parsing.combinator._

    /**
     * Parser
     */
    private object TimeValueParser extends JavaTokenParsers {
        def parse (str : String) : TimeValue =
            parseAll (expr, str) match {
               case Success (l, _) => l

               case Failure (m, _) =>
                   throw new IllegalArgumentException (
                              "Failed to parse time: " + str + ": " + m)

               case Error (m, _)   =>
                   throw new IllegalArgumentException (
                              "Error while parsing time: " + str + ": " + m)
            }

        def expr : Parser[TimeValue] =
            timeValue ~ rep(timeValue) ^^ {
                case u1 ~ l => l.foldLeft (u1) {_ + _}
            }

        def timeValue : Parser[TimeValue] = (
              decimalNumber <~ "seconds"      ^^ (_.toLong.seconds)
            | decimalNumber <~ "milliseconds" ^^ (_.toLong.milliseconds)
            | decimalNumber <~ "nanoseconds"  ^^ (_.toLong.nanoseconds)
            | decimalNumber <~ "microseconds" ^^ (_.toLong.microseconds)
            | decimalNumber <~ "hours"        ^^ (_.toLong.hours)
            | decimalNumber <~ "minutes"      ^^ (_.toLong.minutes)
        )
    }
}
