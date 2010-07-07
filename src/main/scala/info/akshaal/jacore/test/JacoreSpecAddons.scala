/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test

import java.util.{Timer, TimerTask}

import org.specs.Specification
import org.specs.specification.{Example, Examples}

import scala.collection.mutable.{Map, HashMap}

import logger.Logger
import utils.ThreadUtils

/**
 * Support for logging and timeouts in specification.
 */
trait JacoreSpecAddons extends Specification {
    import JacoreSpecAddons._

    protected implicit val jacoreLogger : Logger = Logger.get (this)
    protected val timeoutForOneExample : TimeValue = 5 minutes
    private val runningExamples : Map[Example, TimerTask] = new HashMap

    override def beforeExample (ex: Examples) = {
        ex match {
            case example : Example =>
                beforeOneExample (example)
                super.beforeExample (ex)

            case other =>
                super.beforeExample (other)
        }
    }

    override def afterExample (ex: Examples) = {
        ex match {
            case example : Example =>
                afterOneExample (example)
                super.afterExample (ex)

            case other =>
                super.afterExample (other)
        }
    }

    /**
     * Called right before an example is executed.
     */
    def beforeOneExample (example : Example) : Unit = {
        jacoreLogger.debugLazy ("== == == About to run example: " + example.description)

        val thread = Thread.currentThread
        val task =
            new TimerTask () {
                override def run () : Unit = {
                    ThreadUtils.dumpThreads (
                        "#=#=#=#=#=#=#=# Dumping threads before killing not responding test")

                    try {
                        thread.interrupt ()
                    } catch {
                        case t : Throwable => ()
                    }
                }
            }

        runningExamples (example) = task
        testGuardingTimer.schedule (task, timeoutForOneExample.asMilliseconds)
    }

    /**
     * Called right after an example is executed.
     */
    def afterOneExample (example : Example) : Unit = {
        jacoreLogger.debugLazy ("== == == Example execution finished: " + example.description)

        for (task <- runningExamples.get (example)) {
            task.cancel ()
            runningExamples.remove (example)
        }
    }
}

object JacoreSpecAddons {
    private val testGuardingTimer = new Timer ("Test guarding timer")
}