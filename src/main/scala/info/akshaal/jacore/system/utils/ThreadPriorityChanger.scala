/*
 * newObject.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package utils

import Predefs._
import logger.Logging

trait ThreadPriorityChanger {
    import ThreadPriorityChanger._

    def change (priority : Priority) : Unit
}

private[system] object ThreadPriorityChanger {
    abstract sealed class Priority
    case object LowPriority extends Priority
    case object NormalPriority extends Priority
    case object HiPriority extends Priority
}