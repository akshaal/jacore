/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.utils

/**
 * Wrapper for Long that makes it possible to convert
 * it to TimeValue object.
 */
final class TimeValueFromNumberCreator (x : Long) extends NotNull {
    @inline
    def nanoseconds  = mk (x)

    @inline
    def microseconds = mk (x * 1000L)

    @inline
    def milliseconds = mk (x * 1000L * 1000L)

    @inline
    def seconds      = mk (x * 1000L * 1000L * 1000L)

    @inline
    def minutes      = mk (x * 1000L * 1000L * 1000L * 60L)

    @inline
    def hours        = mk (x * 1000L * 1000L * 1000L * 60L * 60L)

    @inline
    def mk (nano : Long) = new TimeValue (nano)
}
