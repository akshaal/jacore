/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.utils

/**
 * Simple function without arguments.
 */
trait SimpleFunction0 [A] {
    /**
     * Apply function.
     */
    def apply () : A
}

/**
 * Simple function with one arguments.
 */
trait SimpleFunction1 [A, B] {
    /**
     * Apply function.
     *
     * @param arg argument
     */
    def apply (arg: B) : A
}

/**
 * Simple function with two arguments.
 */
trait SimpleFunction2 [A, B, C] {
    /**
     * Apply function.
     *
     * @param arg1 argument
     * @param arg2 argument
     */
    def apply (arg1: B, arg2 : C) : A
}
