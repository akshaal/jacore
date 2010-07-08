/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

import logger.Logging

trait ThreadPriorityChanger {
    import ThreadPriorityChanger._

    def change (priority : Priority) : Unit
}

object ThreadPriorityChanger {
    abstract sealed class Priority
    case object LowPriority extends Priority
    case object NormalPriority extends Priority
    case object HiPriority extends Priority
}
