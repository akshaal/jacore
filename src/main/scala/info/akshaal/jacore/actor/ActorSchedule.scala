/*
 * ActorScheduling.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

import java.util.WeakHashMap
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

import scheduler.ScheduleControl

/**
 * Scheduling for actor.
 */
trait ActorSchedule {
    this : Actor =>

    private val scheduleControls = new WeakHashMap [ScheduleControl, Null]
    private val recurrentSchedules = new ListBuffer [(Any, TimeUnit)]

    /**
     * Schedule to be used by this actor.
     */
    protected object schedule {        
        def in (number : Long)      = new ScheduleWhen (number, ScheduleIn)
        def every (number : Long)   = new ScheduleWhen (number, ScheduleEvery)

        def in (time : TimeUnit)    = new ScheduleWhat (time, ScheduleIn)
        def every (time : TimeUnit) = new ScheduleWhat (time, ScheduleEvery)
    }

    protected final class ScheduleWhen (number : Long, option : ScheduleOption) extends NotNull {
        def nanoseconds  = new ScheduleWhat (number.nanoseconds, option)
        def microseconds = new ScheduleWhat (number.microseconds, option)
        def milliseconds = new ScheduleWhat (number.milliseconds, option)
        def seconds      = new ScheduleWhat (number.seconds, option)
        def minutes      = new ScheduleWhat (number.minutes, option)
        def hours        = new ScheduleWhat (number.hours, option)
    }

    protected final class ScheduleWhat (when : TimeUnit, option : ScheduleOption) extends NotNull {
        def payload (payload : Any) : Unit =
            option match {
                case ScheduleIn =>
                    val control = actorEnv.scheduler.in (ActorSchedule.this, payload, when)
                    scheduleControls.put (control, null)

                case ScheduleEvery =>
                    if (actorStarted) {
                        val control =
                            actorEnv.scheduler.every (ActorSchedule.this, payload, when)
                        scheduleControls.put (control, null)
                    }
                    recurrentSchedules += ((payload, when))
            }

        def executionOf (code : => Unit) : Unit =
            payload (ScheduledCode (() => code))
    }

    /**
     * Cancel all schedules that were created by using 'schedule' object.
     */
    private[actor] def cancelSchedules () : Unit = {
        scheduleControls.keySet.foreach (_.cancel ())
        scheduleControls.clear ()
    }

    /**
     * Start recurrent schedules. That were scheduled before actor was started.
     */
    private[actor] def startRecurrentSchedules () : Unit = {
        for ((payload, when) <- recurrentSchedules) {
            val control = actorEnv.scheduler.every (ActorSchedule.this, payload, when)
            scheduleControls.put (control, null)
        }
    }

    private[actor] final case class ScheduledCode (code : () => Unit)

    private[ActorSchedule] abstract sealed class ScheduleOption
    private[ActorSchedule] case object ScheduleIn extends ScheduleOption
    private[ActorSchedule] case object ScheduleEvery extends ScheduleOption
}
