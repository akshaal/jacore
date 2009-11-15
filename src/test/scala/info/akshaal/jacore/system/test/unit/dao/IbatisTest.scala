/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system.test.unit
package dao

import org.specs.SpecificationWithJUnit
import com.ibatis.sqlmap.client.{SqlMapClient, SqlMapSession}

import Predefs._
import UnitTestHelper._
import system.dao.ibatis.IbatisDataInserterActor

class IbatisTest extends SpecificationWithJUnit ("iBatis support specification") {
    import IbatisTest._

    "IbatisDataInserterActor" should {
        "insert data" in {
            // TODO
        }
    }
}

object IbatisTest {
    var mockedSqlMapClient : SqlMapClient = null

    class InserterTestActor extends IbatisDataInserterActor[String] (
                                 lowPriorityActorEnv = TestModule.lowPriorityActorEnv,
                                 sqlMapClient = mockedSqlMapClient)
    {
        protected override val insertStatementId = "testInsert"
    }
}