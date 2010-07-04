/**
 * Akshaal (C) 2009. GNU GPL. http://akshaal.info
 */

package info.akshaal.jacore
package test
package unit.utils

import unit.UnitTestHelper._

class TimeValueTest extends JacoreSpecWithJUnit ("TimeValue class specification") {
    "TimeValue" should {
        "allow conversions to nanoseconds" in {
            10.nanoseconds.asNanoseconds   must_==  10L
            11.microseconds.asNanoseconds  must_==  11L * 1000L
            12.milliseconds.asNanoseconds  must_==  12L * 1000L * 1000L
            13.seconds.asNanoseconds       must_==  13L * 1000L * 1000L * 1000L
            14.minutes.asNanoseconds       must_==  14L * 1000L * 1000L * 1000L * 60L
            15.hours.asNanoseconds         must_==  15L * 1000L * 1000L * 1000L * 60L * 60L
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
        }

        "support arithmetics" in {
            23.hours + 1.hours                 must_==  24.hours
            50.minutes + 10.minutes + 3.hours  must_==  4.hours
            60.seconds + 1.minutes             must_==  120.seconds
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
        }

        "have meaningful toString method" in {
            "2days 23hours 1mins 45secs 15ms 10us 100ns"  must_==
                (48.hours + 23.hours + 1.minutes + 45.seconds
                 + 15.milliseconds + 10.microseconds + 100.nanoseconds).toString

            "2hours 5secs 7us"  must_==
                (2.hours + 5.seconds + 7.microseconds).toString
                          
            "11mins 33ms 55ns"  must_==
                (11.minutes + 33.milliseconds + 55.nanoseconds).toString
            
            "1mins"  must_==  60.seconds.toString

            "0ns"  must_==  0.seconds.toString
        }

        "support construction from string" in {
            "123 nanoseconds".asNanoseconds  must_==  123.nanoseconds.asNanoseconds
            "99 microseconds".asNanoseconds  must_==  99.microseconds.asNanoseconds
            "1 milliseconds".asNanoseconds   must_==  1.milliseconds.asNanoseconds
            "30 seconds".asNanoseconds       must_==  30.seconds.asNanoseconds
            "10 minutes".asNanoseconds       must_==  10.minutes.asNanoseconds
            "5 hours".asNanoseconds          must_==  5.hours.asNanoseconds

            "123 nanoseconds 1 hours".asNanoseconds  must_==
                          (123.nanoseconds + 1.hours).asNanoseconds

            "99 microseconds 45 seconds".asNanoseconds  must_==
                          (99.microseconds + 45.seconds).asNanoseconds

            "2 minutes 1 milliseconds 4 seconds".asNanoseconds  must_==
                          (2.minutes + 1.milliseconds + 4.seconds).asNanoseconds

            "30 seconds 1 milliseconds".asNanoseconds  must_==
                          (30.seconds + 1.milliseconds).asNanoseconds

            "10 minutes 11 hours".asNanoseconds  must_==
                          (10.minutes + 11.hours).asNanoseconds
        }

        "not parse broken strings" in {
            "30 secons 1 milliseconds".asNanoseconds must throwA[IllegalArgumentException]
        }

        "compare to null" in {
            (5.seconds == null) must beFalse
        }
    }
}
