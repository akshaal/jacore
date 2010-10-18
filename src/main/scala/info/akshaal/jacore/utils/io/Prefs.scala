/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils
package io

import java.util.Properties

final class Prefs (file : String) {
    final val properties = new Properties ()

    withCloseableIO {
        throwIfNull (this.getClass.getClassLoader.getResourceAsStream (file)) {
            new IllegalArgumentException ("Failed to find preferences" +:+ file)
        }
    } {
        properties.load (_)
    }

    final def getString (name : String) : String =
        throwIfNull (properties.getProperty (name)) {
            new IllegalArgumentException (file +:+ "Property " + name + " is required")
        }

    final def getTimeValue (name : String) : TimeValue =
        TimeValue.parse (getString(name))

    final def getInt (name : String) : Int =
        Integer.valueOf(getString (name)).intValue
}
