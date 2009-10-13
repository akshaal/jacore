/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore

class UnrecoverableError (message : String, cause : Throwable)
                            extends RuntimeException (message, cause)
{
    def this (message : String) = this (message, null)
}
