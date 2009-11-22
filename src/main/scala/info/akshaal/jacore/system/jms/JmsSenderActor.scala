/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package jms

import javax.jms.{Connection, ConnectionFactory}

import Predefs._
import actor.{Actor, LowPriorityActorEnv}

/**
 * Template for all actors that are supposed to send messages to some JMS destination.
 */
abstract class AbstractJmsSenderActor (lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
{

}
