/*
 * Prefs.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

import java.util.Properties

final class Prefs (file : String) {
    final val properties = new Properties ()

    withCloseableIO {
        throwIfNull (this.getClass.getClassLoader.getResourceAsStream (file)) {
            new IllegalArgumentException ("Failed to find preferences: " + file)
        }
    } {
        properties.load (_)
    }

    final def getString (name : String) : String =
        throwIfNull (properties.getProperty (name)) {
            new IllegalArgumentException (file + ": Property " + name + " is required")
        }

    final def getTimeValue (name : String) : TimeValue =
        TimeValue.parse (getString(name))

    final def getInt (name : String) : Int =
        Integer.valueOf(getString (name)).intValue
}
