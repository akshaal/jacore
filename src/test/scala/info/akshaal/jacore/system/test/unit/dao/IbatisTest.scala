/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit
package dao

import org.specs.SpecificationWithJUnit
import org.specs.mock.Mockito
import com.ibatis.sqlmap.client.{SqlMapClient, SqlMapSession}

import Predefs._
import UnitTestHelper._
import system.dao.ibatis.AbstractIbatisDataInserterActor

class IbatisTest extends SpecificationWithJUnit ("iBatis support specification") with Mockito {
    import IbatisTest._

    "IbatisDataInserterActor" should {
        // We use the same mockedSqlMapClientForInserter so test must not be run in parallel
        setSequential ()

        "insert data" in {
            mockedSqlMapClientForInserter = mock[SqlMapClient]
            
            withNotStartedActor [InserterTestActor] (actor => {
                val client = mockedSqlMapClientForInserter
                val session = mock[SqlMapSession]

                client.openSession() returns session

                actor insert ("Hello")
                actor insert ("Actor")
                actor insert ("Bye")

                waitForMessageAfter (actor) {actor.start}

                (client.openSession()                   on client)   then
                (session.startTransaction ()            on session)  then
                (session.startBatch ()                  on session)  then
                (session.insert ("testInsert", "Hello") on session)  then
                (session.insert ("testInsert", "Actor") on session)  then
                (session.insert ("testInsert", "Bye")   on session)  then
                (session.executeBatch ()                on session)  then
                (session.commitTransaction ()           on session)  then
                (session.endTransaction ()              on session)  then
                (session.close ()                       on session)  were calledInOrder
            })
        }

        "end transaction and close session even if exception during execute batch and end transaction" in {
            mockedSqlMapClientForInserter = mock[SqlMapClient]

            withNotStartedActor [InserterTestActor] (actor => {
                val client = mockedSqlMapClientForInserter
                val session = mock[SqlMapSession]

                client.openSession() returns session
                session.executeBatch() throws new RuntimeException ("Error executing batch")
                session.endTransaction() throws new RuntimeException ("Error ending transaction")

                actor insert "Hello"

                waitForMessageAfter (actor) {actor.start}

                (client.openSession()                   on client)   then
                (session.startTransaction ()            on session)  then
                (session.startBatch ()                  on session)  then
                (session.insert ("testInsert", "Hello") on session)  then
                (session.executeBatch ()                on session)  then
                (session.endTransaction ()              on session)  then
                (session.close ()                       on session)  were calledInOrder
            })
        }
    }
}

object IbatisTest {
    var mockedSqlMapClientForInserter : SqlMapClient = null

    class InserterTestActor extends AbstractIbatisDataInserterActor[String] (
                                 lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                 sqlMapClient = mockedSqlMapClientForInserter)
                            with Waitable
    {
        protected override val insertStatementId = "testInsert"
    }
}