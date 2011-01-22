/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package scheduler

import java.util.PriorityQueue
import java.util.concurrent.locks.ReentrantLock

import logger.Logging
import daemon.DaemonStatus
import utils.{Timing, TimeValue, ThreadPriorityChanger}

/**
 * Scheduler thread is responsible for maintaing queue of scheduled tasks and firing tasks on time.
 */
private[scheduler] final class SchedulerThread
                             (latencyLimit : TimeValue,
                              threadPriorityChanger : ThreadPriorityChanger,
                              schedulerDrift : TimeValue,
                              daemonStatus : DaemonStatus)
                         extends Thread with Logging
{
    // True if shutdown is in progress
    @volatile
    private var shutdownFlag = false

    // Lock that is used to protect access to the condition of updating event.
    private val updateConditionLock = new ReentrantLock

    // Condition that happens when a thread updated queue and so sheduler thread must
    // wake up in order to check new update. All access to this value must
    // be locked using updateConditionLock.
    private val updateCondition = updateConditionLock.newCondition

    // Holds scheduled tasks. All access must be synchronized on queue object
    private val queue = new PriorityQueue[Schedule]

    // Number of nanoseconds that must be ignored
    private val schedulerDriftNanos = schedulerDrift.inNanoseconds

    // Accounting of latencies
    val latencyTiming = new Timing (limit = latencyLimit, daemonStatus = daemonStatus)

    /**
     * Schedule a task.
     * @param task item
     */
    def schedule (item : Schedule) : Unit = {
        val signalNeeded = queue.synchronized {
            queue.offer (item)
            queue.peek eq item
        }

        // We need to reschedule thread if we added something to the head of the list
        if (signalNeeded) {
            signalUpdate ()
        }
    }

    /**
     * Shutdown scheduler thread.
     */
    def shutdown () : Unit = {
        shutdownFlag = true
        signalUpdate ()

        // Join thread
        this.join (1000) // Give it a second to join
    }

    /**
     * Main method of the scheduler thread.
     */
    override def run () : Unit = {
        debug ("Starting scheduler")

        // Set name of the thread in order to identify it in thread dumps
        this.setName("Scheduler")

        // Change priority of the thread
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

    /**
     * Method for waiting and processing of schedule queue. Any exceptions coming out
     * of this method are logged and ignored.
     */
    private def waitAndProcess () : Unit = {
        val processingNeeded = doWithLockedUpdateCondition {
            val item = queue.synchronized { queue.peek }

            if (item == null) {
                // No items to process, sleep until signal
                updateCondition.await ()
                false // Nothing to process during this iteration
            } else {
                val delay = item.nanoTime - System.nanoTime

                if (delay < schedulerDriftNanos) {
                    // We don't pass peeked value into the method, because
                    // queue might be updated between peeking and processing.
                    // So it is safer to poll for the item in the method.
                    true // We have something to process
                } else {
                    updateCondition.awaitNanos (delay)
                    false // Nothing to process during this iteration
                }
            }
        }

        if (processingNeeded) {
            processFromHead ()
        }
    }

    /**
     * Process queue from its head
     */
    private def processFromHead () : Unit = {
        // Get item from head of the queue, because this method is called, we know for sure
        // that a time for the item has come to be processed.
        val item = queue.synchronized { queue.poll }
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

    /**
     * Signal that queue has been updated.
     */
    @inline
    private def signalUpdate () : Unit = {
        doWithLockedUpdateCondition {
            updateCondition.signal ()
        }
    }

    /**
     * Lock for code to protect access to the 'updateCondition' value.
     */
    @inline
    private def doWithLockedUpdateCondition[T] (code : => T) : T = {
        updateConditionLock.lock ()
        try {
            code
        } finally {
            updateConditionLock.unlock ()
        }
    }
}
