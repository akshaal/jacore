/*
 * ActorScheduling.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

import Predefs._

/**
 * Scheduling for actor.
 */
trait ActorSchedule { this : Actor =>
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
                case ScheduleIn => actorEnv.scheduler.in (ActorSchedule.this, payload, when)
                case ScheduleEvery => actorEnv.scheduler.every (ActorSchedule.this, payload, when)
            }

        def executionOf (code : => Unit) : Unit =
            payload (ScheduledCode (() => code))
    }

    protected final case class ScheduledCode (code : () => Unit)

    private[ActorSchedule] abstract sealed class ScheduleOption
    private[ActorSchedule] case object ScheduleIn extends ScheduleOption
    private[ActorSchedule] case object ScheduleEvery extends ScheduleOption
}