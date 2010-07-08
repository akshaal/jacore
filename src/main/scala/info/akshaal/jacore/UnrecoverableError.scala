/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore

class UnrecoverableError (message : String, cause : Throwable)
                            extends RuntimeException (message, cause)
{
    def this (message : String) = this (message, null)
}
