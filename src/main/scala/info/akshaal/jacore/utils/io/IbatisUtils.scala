/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils
package io

import java.util.{HashMap => JavaHashMap}
import java.net.URL
import java.io.InputStreamReader
import javax.sql.DataSource
import java.sql.{CallableStatement, PreparedStatement, ResultSet}

import org.apache.ibatis.datasource.pooled.PooledDataSourceFactory
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.{SqlSessionFactory, SqlSessionFactoryBuilder}
import org.apache.ibatis.builder.xml.XMLMapperBuilder
import org.apache.ibatis.io.Resources
import org.apache.ibatis.parsing.XNode
import org.apache.ibatis.`type`.{TypeHandler, JdbcType}

/**
 * Helper methods for work with iBatis.
 * This helper works best if import like import IbatisUtils._
 */
object IbatisUtils {
    /**
     * Construct pooled datasource using property file.
     * 
     * @param propertyFile file with properties
     */
    def createPooledDataSource (propertyFile : String) : DataSource = {
        val dataSourcePrefs = new Prefs (propertyFile)
        val dataSourceProperties = dataSourcePrefs.properties
        val dataSourceFactory = new PooledDataSourceFactory
        
        dataSourceFactory.setProperties (dataSourceProperties)
        
        val datasource = dataSourceFactory.getDataSource

        // Return datasource
        datasource
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
        /**
         * Parse given mapper xmls.
         */
        def parseMapperXmls (resourceFileNames : String*) : Unit = {
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

        /**
         * Find and parse mapper xmls under the given directories.
         */
        def parseMapperXmlsInPackages (packages : String*) : Unit = {
            val sqlFragments = new JavaHashMap [String, XNode]
            val loader = defaultClassLoader
            val xmlPred = (_ : URL).getPath.endsWith (".xml")

            for (pkg <- packages; url <- ClassUtils.findResources (pkg, loader, xmlPred)) {
                val reader = new InputStreamReader (url.openConnection ().getInputStream ())

                val mapperBuilder =
                        new XMLMapperBuilder (reader,
                                              configuration,
                                              url.getPath,
                                              sqlFragments)

                mapperBuilder.parse ()
            }
        }

        /**
         * Setup type handler for scala enumeration.
         */
        def addTypeHandler [T <: JacoreEnum] (enum : T, clazz : Class[_ <: T#Value]) : Unit = {
            val registry = configuration.getTypeHandlerRegistry

            val typeHandler = new TypeHandler () {
                 override def setParameter (ps : PreparedStatement,
                                            i : Int,
                                            parameter : java.lang.Object,
                                            jdbcType : JdbcType) : Unit =
                 {
                     ps.setInt (i, parameter.asInstanceOf[T#Value].id)
                 }

                 override def getResult (rs : ResultSet, columnName : String) : java.lang.Object = {
                     enum (rs.getInt(columnName))
                 }

                 override def getResult (cs : CallableStatement, columnIndex : Int) : java.lang.Object = {
                     enum (cs.getInt(columnIndex))
                 }
            }

            registry.register (clazz, typeHandler)
        }
    }
}