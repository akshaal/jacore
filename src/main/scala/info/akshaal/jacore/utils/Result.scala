/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.utils

/**
 * Result of some operation.
 */
abstract sealed class Result [A]

case class Success [A] (payload : A) extends Result [A]
case class Failure [A] (exception : Throwable) extends Result [A]