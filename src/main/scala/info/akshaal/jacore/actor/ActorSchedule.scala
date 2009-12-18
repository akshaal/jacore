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
        def payload (payload : Any) = new Trigger (payload)

        def in (number : Long)    = new TimeSpec (number, scheduleIn)
        def every (number : Long) = new TimeSpec (number, scheduleEvery)

        def in (time : TimeUnit)    = scheduleIn (time)
        def every (time : TimeUnit) = scheduleEvery (time)

        protected def scheduleIn (time : TimeUnit) = {
            def doIt (code : => Unit) : Unit =
                actorEnv.scheduler.in (ActorSchedule.this, ScheduledCode (() => code), time)

            doIt _
        }

        protected def scheduleEvery (time : TimeUnit) = {
            def doIt (code : => Unit) : Unit =
                actorEnv.scheduler.every (ActorSchedule.this, ScheduledCode (() => code), time)

            doIt _
        }
    }

    protected final class Trigger (payload : Any) {
        def in (number : Long)    = new TimeSpec (number, scheduleIn)
        def every (number : Long) = new TimeSpec (number, scheduleEvery)

        def in (time : TimeUnit)    = scheduleIn (time)
        def every (time : TimeUnit) = scheduleEvery (time)

        protected def scheduleIn (time : TimeUnit) =
            actorEnv.scheduler.in (ActorSchedule.this, payload, time)

        protected def scheduleEvery (time : TimeUnit) =
            actorEnv.scheduler.every (ActorSchedule.this, payload, time)
    }

    final class TimeSpec[T] (number : Long, action : TimeUnit => T) extends NotNull {
        def nanoseconds  : T = action (number.nanoseconds)
        def microseconds : T = action (number.microseconds)
        def milliseconds : T = action (number.milliseconds)
        def seconds      : T = action (number.seconds)
        def minutes      : T = action (number.minutes)
        def hours        : T = action (number.hours)
    }

    protected final case class ScheduledCode (code : () => Unit)
}