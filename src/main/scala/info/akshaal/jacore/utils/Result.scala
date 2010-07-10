/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.utils

/**
 * Result of some operation.
 */
abstract sealed class Result [A]

case class Success [A] (payload : A) extends Result [A]
case class Failure [A] (msg : String, exception : Option[Throwable] = None) extends Result [A]
