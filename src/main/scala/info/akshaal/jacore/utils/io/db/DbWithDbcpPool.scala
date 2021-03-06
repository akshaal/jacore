/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

import org.apache.commons.pool.{ObjectPool, KeyedObjectPoolFactory}
import org.apache.commons.pool.impl.{GenericObjectPool, GenericKeyedObjectPoolFactory,
                                     GenericKeyedObjectPool}
import org.apache.commons.dbcp.{ConnectionFactory, DriverManagerConnectionFactory,
                                PoolableConnectionFactory, PoolingDataSource,
                                AbandonedConfig}

import java.sql.Connection
import java.io.PrintWriter

import collection.JavaConversions._

import logger.Logging
import utils.io.CustomLineWriter

/**
 * Database managed by DBCP framework
 */
class DbWithDbcpPool (val url : String,
                      val username : String = null,
                      val password : String = null,
                      val driverClass : Option[String] = None,
                      val maxOpenConnections : Int = 4,
                      val minOpenConnections : Int = 1,
                      val poolPreparedStatements : Boolean = true,
                      val maxOpenPreparedStatements : Int = 100,
                      val validationQuery : Option[String] = None,
                      val validationQueryTimeout : TimeValue = 2 seconds,
                      val evictConnectionIdleFor : TimeValue = 5 minutes,
                      val maxWaitForConnection : TimeValue = 3 seconds,
                      val connectionInitSqls : List[String] = Nil,
                      val defaultReadonly : Boolean = false,
                      val defaultAutocommit : Boolean = true,
                      val defaultTransactionIsolation : Int = -1,
                      val logAbandoned : Boolean = true,
                      val removeAbandoned : Boolean = true,
                      val removeAbandonedTimeout : TimeValue = 15 minutes)
                                    extends Db with Logging
{
    require (removeAbandonedTimeout.inSeconds > 0, "removeAbandonedTimeout must be > 0 seconds")
    require (validationQueryTimeout.inSeconds > 0, "validationQueryTimeout must be > 0 seconds")

    // Load DB driver if given (since JDBC 4 there is autoloading of drivers)
    driverClass map Class.forName

    // Create pool for connections
    protected val connectionPool : ObjectPool = createPoolForConnections ()

    // Poolable connection factory that is used to obtain connections to the database
    protected val poolableConnectionFactory : PoolableConnectionFactory =
        createPoolableConnectionFactory ()

    /**
     * Populate pooling datasource.
     */
    protected val poolingDataSource : PoolingDataSource = new PoolingDataSource (connectionPool)

    /**
     * {InheritedDoc}
     */
    override def open () : Connection = poolingDataSource.getConnection ()

    /**
     * {InheritedDoc}
     */
    override def close () : Unit = connectionPool.close ()

    /**
     * Construct new poolable connection factory.
     * This method is invoked during the class object construction.
     * Default implementation constructs PoolableConnectionFactory using
     * parameters of this class constructor and other methods of this class.
     */
    protected def createPoolableConnectionFactory () : PoolableConnectionFactory =
        new PoolableConnectionFactory (
                    createConnectionFactory (),
                    connectionPool,
                    if (poolPreparedStatements) createPoolFactoryForStatements () else null,
                    validationQuery getOrElse null,
                    validationQueryTimeout.inSeconds.asInstanceOf [Int],
                    connectionInitSqls,
                    defaultReadonly,
                    defaultAutocommit,
                    defaultTransactionIsolation,
                    /* catalog = */ null,
                    createAbandonedConfig ())

    /**
     * Construct new configuration to trace abandoned connections.
     * Default implementation creates config based on provided parameters.
     * If logging is enabled, then information about abandoned connection
     * is written as a warning by the logger.
     */
    protected def createAbandonedConfig () : AbandonedConfig = {
        val loggingWriter = new CustomLineWriter ((s : String) => warn (s))
        val logWriter = new PrintWriter (loggingWriter)

        val config = new AbandonedConfig
        config.setLogAbandoned (logAbandoned)
        config.setRemoveAbandoned (removeAbandoned)
        config.setRemoveAbandonedTimeout (removeAbandonedTimeout.inSeconds.asInstanceOf [Int])
        config.setLogWriter (logWriter)

        config
    }

    /**
     * Construct new connection factory.
     * This method is invoked during the class object construction.
     * Default implementation constructs DriverManagerConnectionFactory using
     * parameters of this class constructor.
     */
    protected def createConnectionFactory () : ConnectionFactory =
        new DriverManagerConnectionFactory (url, username, password)

    /**
     * Construct new connection pool to be used to keep database connections.
     * Default implementation construct GenericObjectPool using configuration
     * provided by 'createGenericObjectPoolConfigForConnections' method.
     * This method is invoked during the class object construction.
     */
    protected def createPoolForConnections () : ObjectPool =
        new GenericObjectPool (null, createGenericObjectPoolConfigForConnections ())

    /**
     * Construct new connection pool config. This method is invoked during the class
     * object construction. Default implementation uses class parameters to consturct
     * pool configuration.
     */
    protected def createGenericObjectPoolConfigForConnections () : GenericObjectPool.Config = {
        val config = new GenericObjectPool.Config
        config.maxActive = maxOpenConnections
        config.maxIdle = maxOpenConnections
        config.minIdle = minOpenConnections
        config.testOnBorrow = validationQuery.isDefined
        config.testOnReturn = false
        config.timeBetweenEvictionRunsMillis = evictConnectionIdleFor.inMilliseconds / 2
        config.minEvictableIdleTimeMillis = evictConnectionIdleFor.inMilliseconds
        config.testWhileIdle = false
        config.maxWait = maxWaitForConnection.inMicroseconds
        config.lifo = false

        config
    }

    /**
     * Construct new connection pool to be used to keep prepared statements.
     * Default implementation constructs GenericKeyedObjectPoolFactory using paremeters
     * provided in constructor of class.
     * This method is invoked during the class object construction.
     */
    protected def createPoolFactoryForStatements () : KeyedObjectPoolFactory =
        new GenericKeyedObjectPoolFactory (/* object factory  = */ null,
                                           /* maxActive       = */ -1,
                                           GenericKeyedObjectPool.WHEN_EXHAUSTED_FAIL,
                                           /* maxWait         = */ 0,
                                           /* maxIdle per key = */ 1,
                                           maxOpenPreparedStatements)

    /**
     * {InheritedDoc}
     */
    override def toString = "DbWithDbcpPool(url='" + url + "')"
}
