/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils

import unit.UnitTestHelper._

class TimeValueTest extends JacoreSpecWithJUnit ("TimeValue class specification") {
    "TimeValue" should {
        "allow conversions to nanoseconds" in {
            10.nanoseconds.inNanoseconds   must_==  10L
            11.microseconds.inNanoseconds  must_==  11L * 1000L
            12.milliseconds.inNanoseconds  must_==  12L * 1000L * 1000L
            13.seconds.inNanoseconds       must_==  13L * 1000L * 1000L * 1000L
            14.minutes.inNanoseconds       must_==  14L * 1000L * 1000L * 1000L * 60L
            15.hours.inNanoseconds         must_==  15L * 1000L * 1000L * 1000L * 60L * 60L
            15.days.inNanoseconds          must_==  15L * 1000L * 1000L * 1000L * 60L * 60L * 24L
        }

        "be convertable to different units" in {
            15.days.inMicroseconds         must_==  15L * 1000L * 1000L * 60L * 60L * 24L
            15.days.inMilliseconds         must_==  15L * 1000L * 60L * 60L * 24L
            15.days.inSeconds              must_==  15L * 60L * 60L * 24L
            15.days.inMinutes              must_==  15L * 60L * 24L
            15.days.inHours                must_==  15L * 24L
            15.days.inDays                 must_==  15L
        }

        "support singular definitions" in {
            1.nanoseconds   must_==  1.nanosecond
            1.microseconds  must_==  1.microsecond
            1.milliseconds  must_==  1.millisecond
            1.seconds       must_==  1.second
            1.minutes       must_==  1.minute
            1.hours         must_==  1.hour
            1.days          must_==  1.day
        }

        "support equality operation" in {
            1.milliseconds  must_==  1.milliseconds
            1.milliseconds  must_!=  10.milliseconds
            2.hours         must_==  2.hours
            2.hours         must_!=  1.hours
            3.minutes       must_==  3.minutes
            3.minutes       must_!=  1.minutes
            30.seconds      must_==  30.seconds
            30.seconds      must_==  30000.milliseconds
            30.seconds      must_!=  1.seconds
            30.seconds      must_!=  30.minutes
            1.day           must_==  24.hours
            10.day          must_!=  24.hours
        }

        "support summation" in {
            23.hours + 1.hours                 must_==  24.hours
            50.minutes + 10.minutes + 3.hours  must_==  4.hours
            60.seconds + 1.minutes             must_==  120.seconds
        }

        "support differences" in {
            25.hours - 1.hours                 must_==  24.hours
            4.hours - 10.minutes - 50.minutes  must_==  3.hours
            60.seconds - 2.minutes             must_==  (-1).minute
        }

        "provide multiplication" in {
            5.hours * 2                        must_==  10.hours
            60.seconds * 10                    must_==  10.minute
        }

        "provide division" in {
            5.hours / 5                        must_==  1.hour
            60.seconds / 6                     must_==  10.second
        }

        "have compare method" in {
            5.hours compare 1.hour             must beGreaterThan (0)
            1.hours compare 5.hour             must beLessThan (0)
            1.minutes compare 60.seconds       must_== 0
        }

        "provide greater-than method" in {
            5.hours > 1.hour                   must beTrue
            1.hours > 5.hour                   must beFalse
            1.minutes > 60.seconds             must beFalse
        }

        "provide less-than method" in {
            5.hours < 1.hour                   must beFalse
            1.hours < 5.hour                   must beTrue
            1.minutes < 60.seconds             must beFalse
        }

        "provide greater-or-equal method" in {
            5.hours >= 1.hour                   must beTrue
            1.hours >= 5.hour                   must beFalse
            1.minutes >= 60.seconds             must beTrue
        }

        "provide less-or-equal method" in {
            5.hours <= 1.hour                   must beFalse
            1.hours <= 5.hour                   must beTrue
            1.minutes <= 60.seconds             must beTrue
        }

        "have min/max methods" in {
            (12 hours) max (11 hours)   must_== (12 hours)
            (1 hours) max (11 hours)   must_== (11 hours)

            (12 hours) min (11 hours)   must_== (11 hours)
            (1 hours) min (11 hours)   must_== (1 hours)
        }

        "have unary - operator" in {
            -(1 minutes)  must_==  ((-1) minutes)
            -(15 hours)  must_==  ((-15) hours)
            -(6 seconds)  must_==  ((-6) seconds)
            -(-(1 second))  must_==  1.second
        }

        "have min constant" in {
            TimeValue.MinValue  must_==  (Long.MinValue).nanoseconds
        }

        "have max constant" in {
            TimeValue.MaxValue  must_==  (Long.MaxValue).nanoseconds
        }

        "have meaningful toString method" in {
            "2days 23hours 1min 45secs 15ms 10us 100ns"  must_==
                (48.hours + 23.hours + 1.minutes + 45.seconds
                 + 15.milliseconds + 10.microseconds + 100.nanoseconds).toString

            "2hours 5secs 7us"  must_==
                (2.hours + 5.seconds + 7.microseconds).toString
                          
            "11mins 33ms 55ns"  must_==
                (11.minutes + 33.milliseconds + 55.nanoseconds).toString
            
            "1min"  must_==  60.seconds.toString

            "0ns"  must_==  0.seconds.toString
        }

        "use singular forms in toString method" in {
            1.days.toString             must_== "1day"
            1.day.toString              must_== "1day"
            1.hours.toString            must_== "1hour"
            1.hour.toString             must_== "1hour"
            1.minutes.toString          must_== "1min"
            1.minute.toString           must_== "1min"
            1.seconds.toString          must_== "1sec"
            1.second.toString           must_== "1sec"
            1.milliseconds.toString     must_== "1ms"
            1.millisecond.toString      must_== "1ms"
            1.microseconds.toString     must_== "1us"
            1.microsecond.toString      must_== "1us"
            1.nanoseconds.toString      must_== "1ns"
            1.nanosecond.toString       must_== "1ns"
        }

        "support construction from string" in {
            TimeValue.parse ("1 nanoseconds")     must_==  1.nanosecond
            TimeValue.parse ("1 nanosecond")      must_==  1.nanosecond
            TimeValue.parse ("1 microseconds")    must_==  1.microsecond
            TimeValue.parse ("1 microsecond")     must_==  1.microsecond
            TimeValue.parse ("1 milliseconds")    must_==  1.millisecond
            TimeValue.parse ("1 millisecond")     must_==  1.millisecond
            TimeValue.parse ("1 seconds")         must_==  1.second
            TimeValue.parse ("1 second")          must_==  1.second
            TimeValue.parse ("1 minutes")         must_==  1.minute
            TimeValue.parse ("1 minute")          must_==  1.minute
            TimeValue.parse ("1 hours")           must_==  1.hour
            TimeValue.parse ("1 hour")            must_==  1.hour
            TimeValue.parse ("1 days")            must_==  1.day
            TimeValue.parse ("1 day")             must_==  1.day

            "123 nanoseconds".inNanoseconds  must_==  123.nanoseconds.inNanoseconds
            "99 microseconds".inNanoseconds  must_==  99.microseconds.inNanoseconds
            "1 milliseconds".inNanoseconds   must_==  1.milliseconds.inNanoseconds
            "30 seconds".inNanoseconds       must_==  30.seconds.inNanoseconds
            "10 minutes".inNanoseconds       must_==  10.minutes.inNanoseconds
            "5 hours".inNanoseconds          must_==  5.hours.inNanoseconds

            "123 nanoseconds 1 hours".inNanoseconds  must_==
                          (123.nanoseconds + 1.hours).inNanoseconds

            "99 microseconds 45 seconds".inNanoseconds  must_==
                          (99.microseconds + 45.seconds).inNanoseconds

            "2 minutes 1 milliseconds 4 seconds".inNanoseconds  must_==
                          (2.minutes + 1.milliseconds + 4.seconds).inNanoseconds

            "30 seconds 1 milliseconds".inNanoseconds  must_==
                          (30.seconds + 1.milliseconds).inNanoseconds

            "10 minutes 11 hours".inNanoseconds  must_==
                          (10.minutes + 11.hours).inNanoseconds
        }

        "not parse broken strings" in {
            "30 secons 1 milliseconds".inNanoseconds must throwA[IllegalArgumentException]
        }

        "compare to null" in {
            (5.seconds == null) must beFalse
        }
    }
}
