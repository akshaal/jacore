
package info.akshaal.jacore
package system
package daemon

import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named

import java.io.File

import Predefs._
import actor.{Actor, NormalPriorityActorEnv}
import scheduler.TimeOut
import utils.TimeUnit
import fs.{TextFile, WriteFileDone, WriteFileFailed}

/**
 * Actor that periodicly updates a file with a current status of daemon.
 * File consists of two values separated with the space character. The first value
 * represents when status file was updated. It is number of milliseconds
 * since January 1, 1970 UTC. The second value is either 'alive' or 'dying'.
 *
 * @param normalPriorityActorEnv actor environment
 * @param daemonStatus daemon status that this actor reflects in file
 * @param textFile text file service
 * @param interval interval between updates if negative or zero, then not updated
 * @param statusFileName name of status file
 */
@Singleton
private[system] class DaemonStatusActor @Inject() (
                 normalPriorityActorEnv : NormalPriorityActorEnv,
                 daemonStatus : DaemonStatus,
                 textFile : TextFile,
                 @Named("jacore.status.update.interval") interval : TimeUnit,
                 @Named("jacore.status.file") statusFileName : String)
            extends Actor (actorEnv = normalPriorityActorEnv)
{
    final val statusFile = new File (statusFileName)

    if (interval > 0.nanoseconds) {
        schedule payload UpdateStatus every interval
    }

    final override def act () = {
        case TimeOut (UpdateStatus) => {
                val passedSinceAlive = System.nanoTime.nanoseconds - daemonStatus.lastAlive
                val isReallyAlive = !daemonStatus.isDying && passedSinceAlive < interval

                val curTime = System.currentTimeMillis
                val statusString = if (isReallyAlive) "alive" else "dying"
                val content = curTime + " " + statusString
                textFile.writeFile (statusFile, content, null)
        }

        case WriteFileDone (_, _) => debug ("Status file has been updated")

        case WriteFileFailed (_, exc, _) => error ("Failed to write status into the file", exc)
    }

    private final case object UpdateStatus
}