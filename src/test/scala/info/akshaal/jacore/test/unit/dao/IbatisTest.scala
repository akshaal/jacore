/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.dao

import org.specs.mock.Mockito
import org.apache.ibatis.session.{SqlSessionFactory, SqlSession, ExecutorType}

import unit.UnitTestHelper._
import dao.ibatis.AbstractIbatisDataInserterActor

class IbatisTest extends JacoreSpecWithJUnit ("iBatis support specification") with Mockito {
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

                actor.waitForMessageAfter {actor.start}

                there was one(client).openSession (ExecutorType.BATCH, true)  then
                          one(session).insert ("testInsert", "Hello")         then
                          one(session).insert ("testInsert", "Actor")         then
                          one(session).insert ("testInsert", "Bye")           then
                          one(session).commit ()                              then
                          one(session).close () orderedBy (client, session)
            })
        }

        "close session even if exception occured" in {
            mockedSqlSessionFactoryForInserter = mock [SqlSessionFactory]

            withNotStartedActor [InserterTestActor] (actor => {
                val client = mockedSqlSessionFactoryForInserter
                val session = mock[SqlSession]

                client.openSession (ExecutorType.BATCH, true) returns session
                session.insert ("testInsert", "Hello") throws new RuntimeException ("test exc")
                session.commit () throws new RuntimeException ("test exc2")

                actor insert "Hello"

                actor.waitForMessageAfter {actor.start}

                there was one(client).openSession (ExecutorType.BATCH, true)  then
                          one(session).insert ("testInsert", "Hello")         then
                          one(session).commit ()                              then
                          one(session).close ()       orderedBy (client, session)
            })
        }
    }
}

object IbatisTest {
    var mockedSqlSessionFactoryForInserter : SqlSessionFactory = null

    class InserterTestActor extends {
        // Early initialization
        protected override val insertStatementId = "testInsert"
    } with AbstractIbatisDataInserterActor[String] (
                                 lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                 sqlSessionFactory = mockedSqlSessionFactoryForInserter)
                            with Waitable
}
