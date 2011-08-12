/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

import H2Url._

/**
 * JdbcUrl for h2 Database.
 *
 * @param location of the database
 * @param cipher optional cipher for encrypted files
 * @param fileLock kind of locking to use to protect database during concrurrent access.
 * @param ifExists open database only if it exists
 * @param closeOneExit set to false if database must not be closed on VM exit
 * @param initSql optional sql to run on db initialization
 * @param user optional user for connection to db
 * @param password optional password for connection to db
 * @param ignoreUnknownSettings if true, then unknown settings are ignored
 * @param accessModeData defines how to open database file
 * @param ignoreCase if true then case of entities is ignored
 * @param mode optional compatibility mode
 * @param autoReconnect enable auto reconnect for lost connection if this parameter is true
 * @param autoServer start server automatically if true
 * @param pageSize optional page size for new databases (in bytes)
 * @param systemOutTraceLevel optional trace level for System.out
 * @param fileTraceLevel optional trace level for file
 * @param cacheSizeKb optional cache size in kilobytes
 * @param otherOptions other options (with leading ';')
 */
case class H2Url (location : Location,
                  cipher : Option[Cipher] = None,
                  fileLock : Option[FileLock] = None,
                  ifExists : Boolean = false,
                  closeOnExit : Boolean = true,
                  initSql : Option[String] = None,
                  user : Option[String] = None,
                  password : Option[String] = None,
                  ignoreUnknownSettings : Boolean = false,
                  accessModeData : Option[AccessModeData] = None,
                  ignoreCase : Boolean = false,
                  mode : Option[Mode] = None,
                  autoReconnect : Boolean = false,
                  autoServer : Boolean = false,
                  pageSize : Option[Int] = None,
                  traceLevelSystemOut : Option[TraceLevelSystemOut] = None,
                  traceLevelFile : Option[TraceLevelFile] = None,
                  cacheSizeKb : Option[Int] = None,
                  otherOptions : String = "")
            extends JdbcUrl
{
    /**
     * Url constructed from parameters.
     */
    private val url : String = {
        val locationSegment =
            location match {
                case File (path) => "file:" + path
                case Memory (name) => "mem:" + name

                case Tcp (host, path, None) =>
                    "tcp://" + host + "/" + path.stripPrefix ("/")

                case Tcp (host, path, Some(port)) =>
                    "tcp://" + host + ":" + port + "/" + path.stripPrefix ("/")

                case Ssl (host, path, None) =>
                    "ssl://" + host + "/" + path.stripPrefix ("/")

                case Ssl (host, path, Some(port)) =>
                    "ssl://" + host + ":" + port + "/" + path.stripPrefix ("/")
            }

        ("jdbc:h2:" + locationSegment)
            .appendOptional (cipher) (";CIPHER=" + _)
            .appendOptional (fileLock) (";FILE_LOCK=" + _)
            .appendIf (ifExists, ";IFEXISTS=TRUE")
            .appendIf (!closeOnExit, ";DB_CLOSE_ON_EXIT=FALSE")
            .appendOptional (initSql) (";INIT=" + _)
            .appendOptional (user) (";USER=" + _)
            .appendOptional (password) (";PASSWORD=" + _)
            .appendIf (ignoreUnknownSettings, ";IGNORE_UNKNOWN_SETTINGS=TRUE")
            .appendOptional (accessModeData) (";ACCESS_MODE_DATA=" + _)
            .appendIf (ignoreCase, ";IGNORECASE=TRUE")
            .appendOptional (mode) (";MODE=" + _)
            .appendIf (autoReconnect, ";AUTO_RECONNECT=TRUE")
            .appendIf (autoServer, ";AUTO_SERVER=TRUE")
            .appendOptional (pageSize) (";PAGE_SIZE=" + _)
            .appendOptional (traceLevelSystemOut) (";TRACE_LEVEL_SYSTEM_OUT=" + _)
            .appendOptional (traceLevelFile) (";TRACE_LEVEL_FILE=" + _)
            .appendOptional (cacheSizeKb) (";CACHE_SIZE=" + _) + otherOptions
    }

    /**
     * {InheritedDoc}
     */
    override def toString = url
}

/**
 * JdbcUrl for h2 Database.
 */
object H2Url {
    // - - - - - -- - -- --- - - - -- - ------ - - -  - - - - - - - - - -
    // Location cases

    /**
     * Location of h2 database.
     */
    sealed abstract trait Location

    /**
     * File based h2 database.
     *
     * @param path path for database file
     */
    case class File (path : String) extends Location

    /**
     * Momoery based h2 database.
     *
     * @param name optional database name
     */
    case class Memory (name : String = "") extends Location

    /**
     * Tcp connection to h2 server.
     *
     * @param host hostname or ip of the server to connect to
     * @param path path or database name on the server
     * @param port optional port
     */
    case class Tcp (host : String, path : String, port : Option[Int] = None) extends Location

    /**
     * Secure ssl connection to h2 server.
     *
     * @param host hostname or ip of the server to connect to
     * @param path path or database name on the server
     * @param port optional port
     */
    case class Ssl (host : String, path : String, port : Option[Int] = None) extends Location

    // - - - - - -- - -- --- - - - -- - ------ - - -  - - - - - - - - - -
    // Ciphers

    /**
     * Cipher for encrypted files.
     */
    sealed abstract trait Cipher

    /**
     * AES cipher for encrypted files.
     */
    case object AES extends Cipher

    /**
     * AES cipher for encrypted files.
     */
    case object XTEA extends Cipher

    // - - - - - -- - -- --- - - - -- - ------ - - -  - - - - - - - - - -
    // File lock

    /**
     * Kind of locking to use to protect database during concrurrent access.
     */
    sealed abstract trait FileLock

    /**
     * Lock using file.
     */
    case object FileLocking extends FileLock {override def toString = "FILE"}

    /**
     * Lock using socket.
     */
    case object SocketLocking extends FileLock {override def toString = "SOCKET"}

    /**
     * Lock using socket.
     */
    case object NoLocking extends FileLock {override def toString = "NO"}

    // - - - - - -- - -- --- - - - -- - ------ - - -  - - - - - - - - - -
    // Access mode

    /**
     * Defines how to open database file.
     */
    sealed abstract trait AccessModeData

    /**
     * 'r' access mode.
     */
    case object AccessModeDataR extends AccessModeData {override def toString = "r"}

    /**
     * 'rw' access mode.
     */
    case object AccessModeDataRW extends AccessModeData {override def toString = "rw"}

    /**
     * 'rws' access mode.
     */
    case object AccessModeDataRWS extends AccessModeData {override def toString = "rws"}

    /**
     * 'rwd' access mode.
     */
    case object AccessModeDataRWD extends AccessModeData {override def toString = "rwd"}

    // - - - - - -- - -- --- - - - -- - ------ - - -  - - - - - - - - - -
    // Compatibility mode

    /**
     * Compatibility mode
     */
    sealed abstract trait Mode

    /**
     * DB2 mode.
     */
    case object DB2 extends Mode

    /**
     * Derby mode.
     */
    case object Derby extends Mode

    /**
     * HSQLDB mode.
     */
    case object HSQLDB extends Mode

    /**
     * MSSQLServer mode.
     */
    case object MSSQLServer extends Mode

    /**
     * MySql server mode.
     */
    case object MySQL extends Mode

    /**
     * Oracle mode.
     */
    case object Oracle extends Mode

    /**
     * PostgreSQL mode.
     */
    case object PostgreSQL extends Mode

    // - - - - - -- - -- --- - - - -- - ------ - - -  - - - - - - - - - -
    // Trace levels

    /**
     * Trace levels for system out.
     */
    sealed abstract trait TraceLevelSystemOut

    /**
     * Trace levels for system out.
     */
    sealed abstract trait TraceLevelFile

    /**
     * OFF trace level.
     */
    case object Off extends TraceLevelSystemOut with TraceLevelFile {override def toString = "0"}

    /**
     * ERROR trace level.
     */
    case object Error extends TraceLevelSystemOut with TraceLevelFile {override def toString = "1"}

    /**
     * INFO trace level.
     */
    case object Info extends TraceLevelSystemOut with TraceLevelFile {override def toString = "2"}

    /**
     * DEBUG trace level.
     */
    case object Debug extends TraceLevelSystemOut with TraceLevelFile {override def toString = "3"}

    /**
     * SLF4J trace level.
     */
    case object Slf4j extends TraceLevelFile {override def toString = "4"}
}
