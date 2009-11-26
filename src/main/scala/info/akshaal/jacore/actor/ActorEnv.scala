/*
 * ActorEnv.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import com.google.inject.{Inject, Singleton}

import utils.{LowPriorityPool, NormalPriorityPool, HiPriorityPool,
              Pool, TimeUnit}
import scheduler.Scheduler

/**
 * Abstract environment for actors. This class is used to provide
 * shceduler and pool to be used by actor.
 */
sealed abstract class ActorEnv {
    private[actor] val pool : Pool
    private[actor] val scheduler : Scheduler
    private[actor] val broadcaster : Broadcaster
}

/**
 * Environment for low priority actors.
 */
@Singleton
final class LowPriorityActorEnv @Inject() (
                private[actor] val pool : LowPriorityPool,
                private[actor] val scheduler : Scheduler,
                private[actor] val broadcaster : Broadcaster)
             extends ActorEnv

/**
 * Environment for normal priority actors.
 */
@Singleton
final class NormalPriorityActorEnv @Inject() (
                private[actor] val pool : NormalPriorityPool,
                private[actor] val scheduler : Scheduler,
                private[actor] val broadcaster : Broadcaster)
             extends ActorEnv

/**
 * Environment for hi priority actors.
 */
@Singleton
final class HiPriorityActorEnv @Inject() (
                private[actor] val pool : HiPriorityPool,
                private[actor] val scheduler : Scheduler,
                private[actor] val broadcaster : Broadcaster)
             extends ActorEnv
