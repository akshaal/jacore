/*
 * Schedule.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package scheduler

import actor.Actor

/**
 * Control for scheduled task.
 */
final class ScheduleControl {
    @volatile
    private[scheduler] var cancelled = false

    @volatile
    private[scheduler] var currentSchedule : Option [Schedule] = None

    /**
     * Cancel sheduled task.
     */
    def cancel () : Unit = {
        currentSchedule match {
            case None => ()
            case Some (schedule) =>
                cancelled = true
                currentSchedule = None
        }
    }
}

/**
 * Abstract schedule item.
 */
private[scheduler] abstract sealed class Schedule (val actor : Actor,
                                                   val payload : Any,
                                                   val nanoTime : Long,
                                                   control : ScheduleControl)
                            extends Comparable[Schedule] with NotNull
{
    control.currentSchedule = Some (this)

    def nextSchedule () : Option[Schedule]

    override def compareTo (that : Schedule) = nanoTime compare that.nanoTime

    def getControl : ScheduleControl = control
}

/**
 * Object represent schedule item which will be run only once.
 */
final private[scheduler] class OneTimeSchedule (actor : Actor,
                                                payload : Any,
                                                nanoTime : Long,
                                                control : ScheduleControl)
                            extends Schedule (actor, payload, nanoTime, control)
{
    override def nextSchedule () = {
        control.currentSchedule = None
        
        None
    }

    override def toString =
        ("OneTimeSchedule(actor=" + actor
         + ", payload=" + payload
         + ", nanoTime=" + nanoTime + ")")
}

/**
 * Object represent schedule item which for recurrent events.
 */
final private[scheduler] class RecurrentSchedule (actor : Actor,
                                                  payload : Any,
                                                  nanoTime : Long,
                                                  period : Long,
                                                  control : ScheduleControl)
                            extends Schedule (actor,
                                              payload,
                                              nanoTime,
                                              control)
{
    override def nextSchedule () = {
        val newSchedule =
            new RecurrentSchedule (actor,
                                   payload,
                                   nanoTime + period,
                                   period,
                                   control)

        Some (newSchedule)
    }

    override def toString =
        ("RecurrentSchedule(actor=" + actor
         + ", payload=" + payload
         + ", nanoTime=" + nanoTime
         + ", period=" + period + ")")
}
