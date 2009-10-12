
package info.akshaal.jacore
package system
package daemon

import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named

import Predefs._
import actor.{Actor, NormalPriorityActorEnv}
import scheduler.{Scheduler, TimeOut}
import utils.TimeUnit

@Singleton
private[system] final class DaemonStatusActor @Inject() (
                 normalPriorityActorEnv : NormalPriorityActorEnv,
                 daemonStatus : DaemonStatus,
                 @Named("jacore.status.update.interval") interval : TimeUnit,
                 @Named("jacore.status.file") statusFile : String)
            extends Actor (actorEnv = normalPriorityActorEnv)
{
    schedule payload UpdateStatus every interval

    /**
     * Process messages
     */
    final override def act () = {
        case TimeOut (UpdateStatus) => {
            // TODO
        }
    }

    private final case object UpdateStatus
}