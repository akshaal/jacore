/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test
package unit.dao

import org.specs.SpecificationWithJUnit
import org.specs.mock.Mockito
import org.apache.ibatis.session.{SqlSessionFactory, SqlSession, ExecutorType}

import Predefs._
import unit.UnitTestHelper._
import dao.ibatis.AbstractIbatisDataInserterActor

class IbatisTest extends SpecificationWithJUnit ("iBatis support specification") with Mockito {
    import IbatisTest._

    "IbatisDataInserterActor" should {
        // We use the same mockedSqlMapClientForInserter so test must not be run in parallel
        setSequential ()

        "insert data" in {
            mockedSqlSessionFactoryForInserter = mock [SqlSessionFactory]
            
            withNotStartedActor [InserterTestActor] (actor => {
                val client = mockedSqlSessionFactoryForInserter
                val session = mock[SqlSession]

                client.openSession (ExecutorType.BATCH, true) returns session

                actor insert ("Hello")
                actor insert ("Actor")
                actor insert ("Bye")

                waitForMessageAfter (actor) {actor.start}

                (client.openSession (ExecutorType.BATCH, true)      on client)   then
                (session.insert ("testInsert", "Hello")             on session)  then
                (session.insert ("testInsert", "Actor")             on session)  then
                (session.insert ("testInsert", "Bye")               on session)  then
                (session.close ()                                   on session)  were calledInOrder
            })
        }

        "close session even if exception occured" in {
            mockedSqlSessionFactoryForInserter = mock [SqlSessionFactory]

            withNotStartedActor [InserterTestActor] (actor => {
                val client = mockedSqlSessionFactoryForInserter
                val session = mock[SqlSession]

                client.openSession (ExecutorType.BATCH, true) returns session
                session.insert ("testInsert", "Hello") throws new RuntimeException ("test exc")

                actor insert "Hello"

                waitForMessageAfter (actor) {actor.start}

                (client.openSession (ExecutorType.BATCH, true)      on client)   then
                (session.insert ("testInsert", "Hello")             on session)  then
                (session.close ()                                   on session)  were calledInOrder
            })
        }
    }
}

object IbatisTest {
    var mockedSqlSessionFactoryForInserter : SqlSessionFactory = null

    class InserterTestActor extends AbstractIbatisDataInserterActor[String] (
                                 lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                 sqlSessionFactory = mockedSqlSessionFactoryForInserter)
                            with Waitable
    {
        protected override val insertStatementId = "testInsert"
    }
}
