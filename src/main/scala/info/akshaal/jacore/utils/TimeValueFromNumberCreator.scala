/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.utils

/**
 * Wrapper for Long that makes it possible to convert
 * it to TimeValue object.
 *
 * @param x long to be used as a quantity
 */
final class TimeValueFromNumberCreator (x : Long) extends NotNull {
    /**
     * Use given long as a number nanoseconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def nanoseconds : TimeValue = mk (x)

    /**
     * Use given long as a number nanoseconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def nanosecond : TimeValue = nanoseconds

    /**
     * Use given long as a number microseconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def microseconds : TimeValue = mk (x * 1000L)

    /**
     * Use given long as a number microseconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def microsecond : TimeValue = microseconds

    /**
     * Use given long as a number milliseconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def milliseconds : TimeValue = mk (x * 1000L * 1000L)

    /**
     * Use given long as a number milliseconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def millisecond : TimeValue = milliseconds

    /**
     * Use given long as a number seconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def seconds : TimeValue = mk (x * 1000L * 1000L * 1000L)

    /**
     * Use given long as a number seconds and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def second : TimeValue = seconds

    /**
     * Use given long as a number minutes and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def minutes : TimeValue = mk (x * 1000L * 1000L * 1000L * 60L)

    /**
     * Use given long as a number minutes and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def minute : TimeValue = minutes

    /**
     * Use given long as a number hours and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def hours : TimeValue = mk (x * 1000L * 1000L * 1000L * 60L * 60L)

    /**
     * Use given long as a number hours and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def hour : TimeValue = hours

    /**
     * Use given long as a number days and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def days : TimeValue = mk (x * 1000L * 1000L * 1000L * 60L * 60L * 24L)

    /**
     * Use given long as a number days and construct appropriate time value.
     *
     * @return new time value
     */
    @inline
    def day : TimeValue = days

    /**
     * Construct new time value using given number of nanoseconds.
     * Just a shortcurt to constructor.
     *
     * @return constructed time value.
     */
    @inline
    private def mk (nano : Long) : TimeValue = new TimeValue (nano)
}
