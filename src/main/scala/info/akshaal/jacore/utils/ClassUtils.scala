/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

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

    /**
     * Convert fully qualified java name to a way java class can be read on filesystem.
     */
    def javaName2fsName (pkg : String) : String = pkg.replace ('.', '/')

    /**
     * Convert file path to class into a java fully qualified name.
     */
    def fsName2javaName (path : String) : String = path.replace ('/', '.')

    /**
     * Find all classes under the given package. Classes are not initialized!
     * @param pkg package
     * @param loader loader
     */
    def findClasses (pkg : String, loader : ClassLoader) : List[Class[_]] = {
        val buf = new ListBuffer [Class[_]]

        for (url <- loader.getResources (javaName2fsName (pkg))) {

        }

        buf.toList
    }
}
