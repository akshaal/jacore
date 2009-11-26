/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

/**
 * Class utils.
 */
object ClassUtils {
    /**
     * Makes boxed version of primitive primitive type classes.
     * @param clazz probably primitive class
     * @return boxed version of primitive type class
     */
    def box (clazz : Class[_]) : Class[_] = {
        clazz match {
            case _ if !clazz.isPrimitive     => clazz
            case java.lang.Boolean.TYPE      => classOf [java.lang.Boolean]
            case java.lang.Byte.TYPE         => classOf [java.lang.Byte]
            case java.lang.Character.TYPE    => classOf [java.lang.Character]
            case java.lang.Double.TYPE       => classOf [java.lang.Double]
            case java.lang.Float.TYPE        => classOf [java.lang.Float]
            case java.lang.Integer.TYPE      => classOf [java.lang.Integer]
            case java.lang.Long.TYPE         => classOf [java.lang.Long]
            case java.lang.Short.TYPE        => classOf [java.lang.Short]
            case java.lang.Void.TYPE         => classOf [java.lang.Void]
        }
    }
}
