package info.akshaal.jacore
package utils

import java.util.concurrent.atomic.{AtomicInteger, AtomicReferenceArray}

import Predefs._
import logger.Logger
import daemon.DaemonStatus

/**
 * Abstract support for classes that makes it possible to measure time latencies.
 */
private[utils] abstract class AbstractTiming
{
    protected def measure (startNano : Long, logger : Logger) (message : => String)

    /**
     * Get average timing.
     */
    def average : TimeUnit

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
private[jacore] final class Timing (limit : TimeUnit, daemonStatus : DaemonStatus)
                        extends AbstractTiming with NotNull
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
            if (time > limit.asNanoseconds) {
                logger.warn (message + ". Timing = " + time.nanoseconds)
            } else {
                logger.debugLazy (message + ". Timing = " + time.nanoseconds)
            }
        }
    }
    
    override def average : TimeUnit = frame.average.nanoseconds
}

/**
 * Helper to measure time. This is a thread safe version which doesn't use synchronization.
 * This is done by having Frame object for each thread. This may be innacurate.
 */
private[jacore] final class ThreadSafeTiming (limit : TimeUnit,
                                              daemonStatus : DaemonStatus,
                                              maxThreads : Int)
                        extends AbstractTiming with NotNull
{
    private[this] val valuesCount = 100

    // All frames
    val frames = new AtomicReferenceArray [LongValueFrame] (maxThreads)

    // Current free frame slot
    val threadFrameCounter = new AtomicInteger (-1)

    // Number of frame of the currently running thread
    val threadFrameNumber = new ThreadLocal [Int] {
        protected override def initialValue : Int = threadFrameCounter.incrementAndGet
    }

    // Current thread frame
    val threadFrame = new ThreadLocal [LongValueFrame] {
        protected override def initialValue : LongValueFrame = {
            val frame = new LongValueFrame (valuesCount)

            frames.set (threadFrameNumber.get, frame)

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
            if (time > limit.asNanoseconds) {
                logger.warn (message + ". Timing = " + time.nanoseconds)
            } else {
                logger.debugLazy (message + ". Timing = " + time.nanoseconds)
            }
        }
    }
    
    override def average : TimeUnit = {
        var totalSum = 0L
        var totalCount = 0L

        for (i <- 0 to threadFrameCounter.get) {
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

