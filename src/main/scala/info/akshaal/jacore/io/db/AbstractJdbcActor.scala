/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db

import actor.{Actor, LowPriorityActorEnv}

/**
 * Template for all actors that are interested in working with JDBC.
 * @param lowPriorityActorEnv low priority environment for this actor
 */
abstract class AbstractJdbcActor (lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{

}
