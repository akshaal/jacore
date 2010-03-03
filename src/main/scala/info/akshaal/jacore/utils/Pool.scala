/** Akshaal (C) 2009. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

import java.util.concurrent.{Executors, ThreadFactory, ThreadPoolExecutor,
                             LinkedBlockingQueue, TimeUnit}

import ThreadPriorityChanger.{HiPriority, NormalPriority, LowPriority}
import daemon.DaemonStatus
import logger.DummyLogging

/**
 * Hi priority pool.
 */
@Singleton
final class HiPriorityPool @Inject()
               (@Named("jacore.pool.hi.threads") threads : Int,
                @Named("jacore.pool.hi.latency") latencyLimit : TimeValue,
                @Named("jacore.pool.hi.execution") executionLimit : TimeValue,
                daemonStatus : DaemonStatus,
                threadPriorityChanger : ThreadPriorityChanger)
      extends Pool (name = "HiPriorityPool",
                    daemonStatus = daemonStatus,
                    priority = HiPriority,
                    threads = threads,
                    latencyLimit = latencyLimit,
                    executionLimit = executionLimit,
                    threadPriorityChanger = threadPriorityChanger)

/**
 * Normal priority pool.
 */
@Singleton
final class NormalPriorityPool @Inject()
               (@Named("jacore.pool.normal.threads") threads : Int,
                @Named("jacore.pool.normal.latency") latencyLimit : TimeValue,
                @Named("jacore.pool.normal.execution") executionLimit : TimeValue,
                daemonStatus : DaemonStatus,
                threadPriorityChanger : ThreadPriorityChanger)
          extends Pool (name = "NormalPriorityPool",
                        daemonStatus = daemonStatus,
                        priority = NormalPriority,
                        threads = threads,
                        latencyLimit = latencyLimit,
                        executionLimit = executionLimit,
                        threadPriorityChanger = threadPriorityChanger)

/**
 * Low priority pool.
 */
@Singleton
final class LowPriorityPool @Inject()
               (@Named("jacore.pool.low.threads") threads : Int,
                @Named("jacore.pool.low.latency") latencyLimit : TimeValue,
                @Named("jacore.pool.low.execution") executionLimit : TimeValue,
                daemonStatus : DaemonStatus,
                threadPriorityChanger : ThreadPriorityChanger)
          extends Pool (name = "LowPriorityPool",
                        daemonStatus = daemonStatus,
                        priority = LowPriority,
                        threads = threads,
                        latencyLimit = latencyLimit,
                        executionLimit = executionLimit,
                        threadPriorityChanger = threadPriorityChanger)

/**
 * Pool class to be used by actors.
 */
abstract sealed class Pool (name : String,
                            priority : ThreadPriorityChanger.Priority,
                            threads : Int,
                            latencyLimit : TimeValue,
                            executionLimit : TimeValue,
                            daemonStatus : DaemonStatus,
                            threadPriorityChanger : ThreadPriorityChanger)
                      extends DummyLogging
{
    // Access to this field must be synchronized
    var availableIndexes : List[Int] = 0 to (threads - 1) toList

    // Number of the current thread. This is one of values removed from list availableIndexes.
    // When thread is finished its work, the number is moved back to the thread.
    final val currentThreadNumber = new ThreadLocal [java.lang.Integer]

    final val latencyTiming =
        new ThreadSafeTiming (limit = latencyLimit,
                              daemonStatus = daemonStatus,
                              currentThreadNumber = currentThreadNumber,
                              maxThreads = threads)

    final val executionTiming =
        new ThreadSafeTiming (limit = latencyLimit,
                              daemonStatus = daemonStatus,
                              currentThreadNumber = currentThreadNumber,
                              maxThreads = threads)

    private val threadFactory =
        new ThreadFactory {
            def newThread (r : Runnable) : Thread = {
                // Get this thread number out of available indexes
                val threadNumber = allocateThreadNumber

                val proxy =
                    mkRunnable {
                        currentThreadNumber.set (threadNumber.asInstanceOf [java.lang.Integer])
                        threadPriorityChanger.change (priority)

                        try {
                            r.run
                        } finally {
                            releaseCurrentThreadNumber ()
                        }
                    }

                val thread = new Thread (proxy)
                thread.setName (name + "-" + (threadNumber + 1))

                thread
            }
        }

    final val executors =
        new ThreadPoolExecutor (threads,
                                threads,
                                0L,
                                TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue[Runnable],
                                threadFactory) {
            protected override def afterExecute (runnable : Runnable, throwable : Throwable) = {
                if (throwable != null) {
                    releaseCurrentThreadNumber ()
                    error ("Uncatched error in worker thread!")
                }

                super.afterExecute (runnable, throwable)
            }
        }

    /**
     * Returns one of available thread numbers from list.
     */
    def allocateThreadNumber () : Int = {
        availableIndexes synchronized {
            availableIndexes match {
                case idx :: xs =>
                    availableIndexes = xs
                    idx

                case Nil =>
                    throw new UnrecoverableError ("No indexes available for a new thread!")
            }
        }
    }

    /**
     * Returns current thread number (if any) to list of available thread numbers.
     */
    def releaseCurrentThreadNumber () : Unit = {
        val threadNumber = currentThreadNumber.get

        if (threadNumber ne null) {
            availableIndexes synchronized {
                availableIndexes = threadNumber.asInstanceOf[Int] :: availableIndexes
            }
        }
    }
}
