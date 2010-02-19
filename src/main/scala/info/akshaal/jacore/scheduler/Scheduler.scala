/*
 * Scheduler.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package scheduler

import utils.{TimeUnit, ThreadPriorityChanger}
import logger.Logging
import actor.Actor
import daemon.DaemonStatus

/**
 * Marks an actor for which scheduling shift can be selected at random
 * (depending on actor's hashcode).
 */
trait UnfixedScheduling

/**
 * Scheduler.
 */
trait Scheduler {
    /**
     * Get average latency of the scheduler.
     */
    def averageLatency : TimeUnit

    /**
     * Schedule payload for actor to be delivered in timeUnit.
     */
    def in (actor : Actor, payload : Any, timeUnit : TimeUnit) : ScheduleControl

    /**
     * Schedule payload for actor to be delivered every timeUnit.
     */
    def every (actor : Actor, payload : Any, period : TimeUnit) : ScheduleControl
}

/**
 * Scheduler class.
 */
@Singleton
class SchedulerImpl @Inject() (
                  @Named("jacore.scheduler.latency") latencyLimit : TimeUnit,
                  @Named("jacore.scheduler.drift") schedulerDrift : TimeUnit,
                  threadPriorityChanger : ThreadPriorityChanger,
                  daemonStatus : DaemonStatus)
            extends Logging with Scheduler
{
    private[this] val schedulerThread =
            new SchedulerThread (latencyLimit = latencyLimit,
                                 threadPriorityChanger = threadPriorityChanger,
                                 schedulerDrift = schedulerDrift,
                                 daemonStatus = daemonStatus)

    /**
     * Get average latency of the scheduler.
     */
    final def averageLatency = schedulerThread.latencyTiming.average

    /**
     * Shutdown scheduler.
     */
    final def shutdown () = schedulerThread.shutdown

    /**
     * Start scheduler.
     */
    final def start () = schedulerThread.start

    /**
     * Schedule payload for actor to be delivered in timeUnit.
     */
    final def in (actor : Actor, payload : Any, timeUnit : TimeUnit) : ScheduleControl = {
        val control = new ScheduleControl

        val schedule =
            new OneTimeSchedule (actor,
                                 payload,
                                 timeUnit.asNanoseconds + System.nanoTime,
                                 control)

        schedulerThread.schedule (schedule)

        control
    }

    /**
     * Schedule payload for actor to be delivered every timeUnit.
     */
    final def every (actor : Actor, payload : Any, period : TimeUnit) : ScheduleControl = {
        val periodNano = period.asNanoseconds
        val curNanoTime = System.nanoTime
        val semiStableNumber =
                (
                    actor match {
                        case unfixed : UnfixedScheduling => actor
                        case _                           => actor.getClass.getName.toString
                    }
                ).hashCode.asInstanceOf [Long].abs

        def calc (shift : Long) =
            ((curNanoTime / periodNano + shift) * periodNano + semiStableNumber % periodNano)

        val variantOfNanoTime = calc(0)
        val nanoTime = if (variantOfNanoTime < curNanoTime) calc(1) else variantOfNanoTime

        val control = new ScheduleControl

        schedulerThread.schedule (new RecurrentSchedule (actor,
                                                         payload,
                                                         nanoTime,
                                                         periodNano,
                                                         control))

        control
    }
}

/**
 * Object of this class will be used as a holder of payload when message
 * is delivered.
 */
sealed case class TimeOut (val payload : Any) extends NotNull
