/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

import java.util.concurrent.atomic.{AtomicInteger, AtomicReferenceArray}

import logger.Logger
import daemon.DaemonStatus
import utils.frame.LongValueFrame

/**
 * Abstract support for classes that makes it possible to measure time latencies.
 */
private[utils] abstract class AbstractTiming
{
    protected def measure (startNano : Long, logger : Logger) (message : => String)

    /**
     * Get average timing.
     */
    def average : TimeValue

    /**
     * Measure time passed since <code>startNano<code> til now.
     */
    def finishedButExpected (startNano : Long, message : => String)
                            (implicit logger : Logger) =
    {
        measure (startNano, logger) (message)
    }

    /**
     * Start measure time and return a function that must be called
     * when timing is done.
     */
    def createFinisher (implicit logger : Logger) = {
        // Get current time
        val startNano = System.nanoTime

        // Return function
        measure (startNano, logger) _
    }
}

/**
 * Not thread safe version of class to track of execution performance.
 */
private[jacore] final class Timing (limit : TimeValue, daemonStatus : DaemonStatus)
                        extends AbstractTiming
{
    private[this] val valuesCount = 100
    private[this] val frame = new LongValueFrame (valuesCount)

    protected override def measure (startNano : Long, logger : Logger) (message : => String) =
    {
        if (daemonStatus.isQosAllowed) {
            val stopNano = System.nanoTime
            val time = stopNano - startNano
            frame.put (time)

            // Inform
            if (time > limit.inNanoseconds) {
                logger.warn (message + ". Timing = " + time.nanoseconds)
            } else {
                logger.debugLazy (message + ". Timing = " + time.nanoseconds)
            }
        }
    }

    override def average : TimeValue = frame.average.nanoseconds
}

/**
 * Helper to measure time. This is a thread safe version which doesn't use synchronization.
 * This is done by having Frame object for each thread. This may be innacurate.
 */
private[jacore] final class ThreadSafeTiming (limit : TimeValue,
                                              daemonStatus : DaemonStatus,
                                              currentThreadNumber : ThreadLocal [java.lang.Integer],
                                              maxThreads : Int)
                        extends AbstractTiming
{
    private[this] val valuesCount = 100

    // All frames
    private[this] val frames = new AtomicReferenceArray [LongValueFrame] (maxThreads)

    // Current free frame slot
    private[this] val threadFrameLastIndex = new AtomicInteger (-1)

    // Current thread frame
    private[this] val threadFrame = new ThreadLocal [LongValueFrame] {
        protected override def initialValue : LongValueFrame = {
            val frame = new LongValueFrame (valuesCount)

            frames.set (currentThreadNumber.get.asInstanceOf [Int], frame)

            frame
        }
    }

    protected override def measure (startNano : Long, logger : Logger) (message : => String) =
    {
        if (daemonStatus.isQosAllowed) {
            val frame = threadFrame.get
            val stopNano = System.nanoTime
            val time = stopNano - startNano
            frame.put (time)

            // Inform
            if (time > limit.inNanoseconds) {
                logger.warn (message + ". Timing = " + time.nanoseconds)
            } else {
                logger.debugLazy (message + ". Timing = " + time.nanoseconds)
            }
        }
    }

    override def average : TimeValue = {
        var totalSum = 0L
        var totalCount = 0L

        for (i <- 0 to maxThreads - 1) {
            val frame = frames.get (i)
            if (frame != null) {
                // Because these can be changed concurrently and not atomically
                // we get not accurate calculation here
                totalSum += frame.currentSum
                totalCount += frame.currentCount
            }
        }

        if (totalCount == 0L) 0.nanoseconds else (totalSum / totalCount).nanoseconds
    }
}
