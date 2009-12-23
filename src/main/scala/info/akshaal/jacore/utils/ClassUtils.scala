/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

import java.lang.reflect.Modifier

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

import java.net.URL

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
     * Find all resource under the given package matching predicate
     * @param pkg package
     * @param loader loader
     */
    def findResources (pkg : String,
                       loader : ClassLoader,
                       pred : URL => Boolean) : List[URL] =
    {
        val buf = new ListBuffer [URL]

        for (url <- loader.getResources (javaName2fsName (pkg))) {
            ResourceUtils.findResources (buf, url, pred)
        }

        buf.toList
    }

    /**
     * Find all resource under the given package matching predicate. Classes are not initialized!
     * @param pkg package
     * @param loader loader
     */
    def findClasses (pkg : String,
                     loader : ClassLoader,
                     pred : Class[_] => Boolean) : List[Class[_]] =
    {
        val buf = new ListBuffer [Class[_]]

        for (pkgUrl <- loader.getResources (javaName2fsName (pkg))) {
            val pkgUrlPath = pkgUrl.getPath
            val pkgUrlPathSize = pkgUrlPath.size
            val urlBuf = new ListBuffer [URL]
            
            ResourceUtils.findResources (urlBuf, pkgUrl, _.getPath.endsWith(".class"))

            for (url <- urlBuf) {
                val urlPath = url.getPath
                val urlPathRelative = urlPath.substring (pkgUrlPathSize, urlPath.length - 6)
                val fqClassName = pkg + fsName2javaName (urlPathRelative)

                val clazz = Class.forName (fqClassName, false, loader)
                if (pred (clazz)) {
                    buf += clazz
                }
            }
        }

        buf.toList
    }

    /**
     * Check if the given class represents a scala object (module).
     *
     * @param clazz class to check
     */
    def isModule (clazz : Class [_]) : Boolean = {
        try {
            val moduleField = clazz.getField ("MODULE$")
            val moduleFieldModifiers = moduleField.getModifiers
            val moduleFieldType = moduleField.getType

            Modifier.isPublic (moduleFieldModifiers) &&
                Modifier.isStatic (moduleFieldModifiers) &&
                moduleFieldType.isAssignableFrom (clazz)
        } catch {
            case exc : NoSuchFieldException => false
        }
    }

    /**
     * Try to get module instance.
     *
     * @param clazz to get module of
     * @return Some(instnace) or None if not module
     */
    def getModuleInstance [A] (clazz : Class [A]) : Option [A] = {
        if (isModule (clazz)) {
            val realClazz = Class.forName (clazz.getName)
            val moduleField = realClazz.getField ("MODULE$")
            
            Some (moduleField.get (null).asInstanceOf [A])
        } else {
            None
        }
    }
}
