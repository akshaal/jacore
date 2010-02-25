/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

import java.text.DateFormat
import java.util.Date

import collection.immutable.{List, Nil}
import scala.collection.JavaConversions._
import scala.collection.mutable.Map

import logger.DummyLogging

object ThreadUtils extends DummyLogging {
    /**
     * Dump threads stack.
     * @param message message to show before dump
     */
    def dumpThreads (message : String) = {
        val traces : Map[Thread, Array[StackTraceElement]] = Thread.getAllStackTraces ()

        var threadDumpList : List[String] = Nil
        for ((thread, stackTraceElements) <- traces) {
            val name = thread.getName
            val stack = stackTraceElements.mkString (",\n    ")

            threadDumpList = (name + ":\n    " + stack + "\n") :: threadDumpList
        }

        val dt = DateFormat.getDateTimeInstance (DateFormat.FULL, DateFormat.FULL)
                           .format (new Date)
        error (dt + " ::: " + message + ":\n" + threadDumpList.mkString ("\n"))
    }
}
