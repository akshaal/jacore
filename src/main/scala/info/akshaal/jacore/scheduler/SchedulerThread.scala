/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package scheduler

import java.util.concurrent.locks.ReentrantLock
import java.util.PriorityQueue

import logger.Logging
import daemon.DaemonStatus
import utils.{Timing, TimeValue, ThreadPriorityChanger}

private[scheduler] final class SchedulerThread
                             (latencyLimit : TimeValue,
                              threadPriorityChanger : ThreadPriorityChanger,
                              schedulerDrift : TimeValue,
                              daemonStatus : DaemonStatus)
                         extends Thread with Logging
{
    @volatile
    private var shutdownFlag = false
    private val lock = new ReentrantLock
    private val condition = lock.newCondition
    private val queue = new PriorityQueue[Schedule]
    private val schedulerDriftNanos = schedulerDrift.asNanoseconds
    
    val latencyTiming = new Timing (limit = latencyLimit, daemonStatus = daemonStatus)

    def schedule (item : Schedule) : Unit = {
        synchronized {
            queue.offer (item)

            // We need to reschedule thread if we added something to the head of the list
            if (queue.peek eq item) {
                locked { condition.signal () }
            }
        }
    }

    def shutdown () : Unit = {
        shutdownFlag = true
        locked { condition.signal () }

        // Join thread
        this.join (1000) // Give it a second to join
    }

    override def run () : Unit = {
        debug ("Starting scheduler")
        this.setName("Scheduler")

        threadPriorityChanger.change (ThreadPriorityChanger.HiPriority)

        // Main loop
        while (!shutdownFlag) {
            logIgnoredException ("Ignored exception during wait and process") {
                waitAndProcess
            }
        }

        // Bye-bye
        debug ("Stopping scheduler")
    }

    private def waitAndProcess () : Unit = {
        val item = synchronized { queue.peek }

        if (item == null) {
            // No items to process, sleep until signal
            locked { condition.await }
        } else {
            val delay = item.nanoTime - System.nanoTime

            if (delay < schedulerDriftNanos) {
                locked { processFromHead }
            } else {
                locked { condition.awaitNanos (delay) }
            }
        }
    }

    private def processFromHead () : Unit = {
        // Get item from head
        val item = synchronized { queue.poll }
        if (item.getControl.cancelled) {
            return
        }

        // Measure latency
        latencyTiming.finishedButExpected (item.nanoTime, "Event triggered" +:+ item)

        // Send message to actor
        item.actor ! (TimeOut (item.payload))

        // Reschedule if needed
        item.nextSchedule match {
            case None            => ()
            case Some (nextItem) => schedule (nextItem)
        }
    }

    @inline
    private def locked[T] (code : => T) : T = {
        lock.lock
        try {
            code
        } finally {
            lock.unlock
        }
    }
}
