/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

import java.util.{HashMap => JavaHashMap}
import javax.sql.DataSource

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.{SqlSessionFactory, SqlSessionFactoryBuilder}
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.io.Resources
import org.apache.ibatis.parsing.XNode

import Predefs._

/**
 * Helper methods for work with iBatis.
 * This helper works best if import like import IbatisUtils._
 */
object IbatisUtils {
    /**
     * Construct ppoled datasource using property file.
     * 
     * @param propertyFile file with properties
     */
    def createPooledDataSource (propertyFile : String) : DataSource = {
        val dataSourcePrefs = new Prefs ("jdbc.properties")
        val dataSourceFactory = new PooledDataSourceFactory
        
        dataSourceFactory.setProperties (dataSourcePrefs.properties)
        
        dataSourceFactory.getDataSource
    }

    /**
     * Construct simple jdbc configuration using given datasource.
     *
     * @param name name for environment
     * @param dataSource data source to use
     */
    def createJdbcConfiguration (name : String, dataSource : DataSource) : Configuration = {
        val transactionFactory = new JdbcTransactionFactory
        val sqlEnvironment = new Environment (name, transactionFactory, dataSource)

        new Configuration (sqlEnvironment)
    }

    /**
     * Construct SqlSessionFactory using given configuration.
     *
     * @param configuration session factory configuration
     */
    def createSqlSessionFactory (configuration : Configuration) : SqlSessionFactory = {
        (new SqlSessionFactoryBuilder).build (configuration)
    }

    /**
     * Implicit conversion to enrich ibatis configuration.
     */
    implicit def configuration2richConfiguration (configuration : Configuration) : RichConfiguration =
    {
        new RichConfiguration (configuration)
    }

    /**
     * Rich configuration (pimp my library pattern takes place here).
     */
    class RichConfiguration (configuration : Configuration) {
        def parseMapperXml (resourceFileNames : String*) : Unit = {
            val sqlFragments = new JavaHashMap [String, XNode]

            for (resourceFileName <- resourceFileNames) {
                val reader = Resources.getResourceAsReader (resourceFileName)
                val mapperBuilder =
                        new XMLMapperBuilder (reader,
                                              configuration,
                                              resourceFileName,
                                              sqlFragments)

                mapperBuilder.parse ()
            }
        }
    }
}
