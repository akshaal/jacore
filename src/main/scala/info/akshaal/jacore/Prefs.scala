/*
 * Prefs.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore

import java.util.Properties

import Predefs._
import system.utils.TimeUnit

// TODO: No classes must use this by default. Move to system

final class Prefs (file : String) {
    private[this] val properties = new Properties ()

    withCloseableIO {
        throwIfNull (this.getClass.getResourceAsStream (file)) {
            new IllegalArgumentException ("Failed to find preferences: " + file)
        }
    } {
        properties.load (_)
    }

    final def getString (name : String) : String =
        throwIfNull (properties.getProperty (name)) {
            new IllegalArgumentException ("Property " + name + " is required")
        }

    final def getTimeUnit (name : String) : TimeUnit =
        TimeUnit.parse (getString(name))

    final def getInt (name : String) : Int =
        Integer.valueOf(getString (name)).intValue
}
