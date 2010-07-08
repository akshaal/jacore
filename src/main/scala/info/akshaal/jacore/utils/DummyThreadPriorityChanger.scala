/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

final class DummyThreadPriorityChanger extends ThreadPriorityChanger {
    import ThreadPriorityChanger.Priority

    override def change (priority : Priority) = ()
}
