/*
 * DummyThreadPriorityChanger.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

final class DummyThreadPriorityChanger extends ThreadPriorityChanger {
    import ThreadPriorityChanger.Priority

    override def change (priority : Priority) = ()
}
