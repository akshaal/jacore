/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package dao
package ibatis

import com.ibatis.sqlmap.client.{SqlMapClient, SqlMapSession}

import annotation.CallByMessage
import actor.{Actor, LowPriorityActorEnv}

/**
 * Implementation of data inserter using iBatis.
 */
abstract class IbatisDataInserterActor[T] (sqlMapClient : SqlMapClient,
                                           lowPriorityActorEnv : LowPriorityActorEnv)
                                extends Actor (actorEnv = lowPriorityActorEnv)
                                with DataInserter[T] {
    protected val insertStatementId : String
    protected var curSession : Option [SqlMapSession] = None

    /**
     * (@InheritDoc)
     */
    @CallByMessage
    def insert (data : T) : Unit = {
        val session =
            curSession match {
                case Some (ses) => ses
                case None       => openSession ()
            }

        session.insert (insertStatementId, data)
    }

    /**
     * Open session. curSession must be updated.
     */
    protected def openSession () : SqlMapSession = {
        val session = sqlMapClient.openSession ()
        session.startTransaction ()
        session.startBatch ()

        curSession = Some (session)

        session
    }

    // TODO: Implement commit
    // TODO: Implement optional feedback
    // TODO: Implement tests
}