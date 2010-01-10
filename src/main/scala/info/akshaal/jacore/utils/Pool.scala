/** Akshaal (C) 2009. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}

import Predefs._

import ThreadPriorityChanger.{HiPriority, NormalPriority, LowPriority}
import daemon.DaemonStatus

/**
 * Hi priority pool.
 */
@Singleton
final class HiPriorityPool @Inject()
               (@Named("jacore.pool.hi.threads") threads : Int,
                @Named("jacore.pool.hi.latency") latencyLimit : TimeUnit,
                @Named("jacore.pool.hi.execution") executionLimit : TimeUnit,
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
                @Named("jacore.pool.normal.latency") latencyLimit : TimeUnit,
                @Named("jacore.pool.normal.execution") executionLimit : TimeUnit,
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
                @Named("jacore.pool.low.latency") latencyLimit : TimeUnit,
                @Named("jacore.pool.low.execution") executionLimit : TimeUnit,
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
                            latencyLimit : TimeUnit,
                            executionLimit : TimeUnit,
                            daemonStatus : DaemonStatus,
                            threadPriorityChanger : ThreadPriorityChanger)
{
    final val latencyTiming =
        new ThreadSafeTiming (limit = latencyLimit,
                              daemonStatus = daemonStatus,
                              maxThreads = threads)

    final val executionTiming =
        new ThreadSafeTiming (limit = latencyLimit,
                              daemonStatus = daemonStatus,
                              maxThreads = threads)

    private val threadFactory = new ThreadFactory {
        val counter = new AtomicInteger (0)

        def newThread (r : Runnable) : Thread = {
            val threadNumber = counter.incrementAndGet

            val proxy = mkRunnable {
                threadPriorityChanger.change (priority)
                r.run
            }

            val thread = new Thread (proxy)
            thread.setName (name + "-" + threadNumber)
            
            thread
        }
    }

    final val executors = Executors.newFixedThreadPool (threads, threadFactory)
}
