/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package dao
package ibatis

import org.apache.ibatis.session.{SqlSessionFactory, SqlSession, ExecutorType}

import annotation.CallByMessage
import actor.{Actor, LowPriorityActorEnv}

/**
 * Implementation of data inserter using iBatis.
 */
abstract class AbstractIbatisDataInserterActor[T] (sqlSessionFactory : SqlSessionFactory,
                                                   lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
                                with DataInserter[T]
{
    protected val insertStatementId : String
    protected var curSession : Option [SqlSession] = None
    protected var notifications : List[(Actor, Any)] = Nil

    /**
     * (@InheritDoc)
     */
    @CallByMessage
    def insert (data : T) : Unit = {
        doInsert (data)
    }

    /**
     * (@InheritDoc)
     */
    @CallByMessage
    def insert (data : T, payload : Any) : Unit = {
        doInsert (data)

        sender.foreach (actor => notifications = (actor, payload) :: notifications)
    }

    /**
     * Perform insert operation.
     */
    protected def doInsert (data : T) : Unit = {
        curSession match {
            case Some (session) =>
                session.insert (insertStatementId, data)

            case None =>
                curSession = Some (openSession ())
                doInsert (data)
        }
    }

    /**
     * Open session.
     */
    protected def openSession () : SqlSession = {
        sqlSessionFactory.openSession (ExecutorType.BATCH, true)
    }

    /**
     * (@InheritDoc)
     *
     * Used to commit transaction and send notification if any.
     */
    protected override def afterActs () : Unit = {
        for (session <- curSession) {
            curSession = None

            try {
                session.commit ()

                for ((actor, payload) <- notifications) {
                    actor ! InsertFinished (payload)
                }
            } finally {
                session.close ()
            }
        }
    }
}
