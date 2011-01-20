/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.utils.io.db

import unit.UnitTestHelper._

import utils.io.db.H2Url

class H2UrlTest extends JacoreSpecWithJUnit ("H2Url class specification") {
    import H2Url._

    def s (url : H2Url) : String = url

    "H2Url" should {
        "construct url for file location" in {
            s (H2Url (File ("a/b/c")))  must_==  "jdbc:h2:file:a/b/c"
            s (H2Url (File ("~/test")))  must_==  "jdbc:h2:file:~/test"
            s (H2Url (File ("/data/sample")))  must_==  "jdbc:h2:file:/data/sample"
            s (H2Url (File ("C:/data/sample")))  must_==  "jdbc:h2:file:C:/data/sample"
        }

        "construct url for private db in memory" in {
            s (H2Url (Memory ()))  must_==  "jdbc:h2:mem:"
        }

        "construct url for named db in memory" in {
            s (H2Url (Memory (name = "test_db")))  must_==  "jdbc:h2:mem:test_db"
        }

        "construct url for connection by tcp" in {
            s (H2Url (Tcp ("localhost", "~/test")))  must_==  "jdbc:h2:tcp://localhost/~/test"
            s (H2Url (Tcp ("dbserv", "~/sample", port = Some(8084))))  must_==  "jdbc:h2:tcp://dbserv:8084/~/sample"
            s (H2Url (Tcp ("localhost", "mem:test")))  must_==  "jdbc:h2:tcp://localhost/mem:test"
            s (H2Url (Tcp ("localhost", "/mem")))  must_==  "jdbc:h2:tcp://localhost/mem"
        }

        "construct url for connection by ssl" in {
            s (H2Url (Ssl ("secureserv", "/sample")))  must_==  "jdbc:h2:ssl://secureserv/sample"
            s (H2Url (Ssl ("secureserv2.foo", "~/sample",
                           port = Some(8085))))  must_==  "jdbc:h2:ssl://secureserv2.foo:8085/~/sample"
        }

        "construct url with cipher" in {
            s (H2Url (Ssl ("secureserv", "~/testdb"),
                      cipher = Some(AES)))  must_==  "jdbc:h2:ssl://secureserv/~/testdb;CIPHER=AES"

            s (H2Url (File ("~/secure"),
                      cipher = Some(XTEA)))  must_==  "jdbc:h2:file:~/secure;CIPHER=XTEA"
        }

        "construct url with file locking specified" in {
            s (H2Url (File ("~/private"),
                      cipher = Some(XTEA),
                      fileLock = Some(SocketLocking)))  must_==  "jdbc:h2:file:~/private;CIPHER=XTEA;FILE_LOCK=SOCKET"

            s (H2Url (File ("~/private"),
                      fileLock = Some(FileLocking)))  must_==  "jdbc:h2:file:~/private;FILE_LOCK=FILE"

            s (H2Url (File ("~/private"),
                      fileLock = Some(NoLocking)))  must_==  "jdbc:h2:file:~/private;FILE_LOCK=NO"
        }

        "construct url with IFEXISTS option" in {
            s (H2Url (File ("abc"), ifExists = true))  must_==  "jdbc:h2:file:abc;IFEXISTS=TRUE"
        }

        "construct url with DB_CLOSE_ON_EXIT option" in {
            s (H2Url (File ("abc"), closeOnExit = false))  must_==  "jdbc:h2:file:abc;DB_CLOSE_ON_EXIT=FALSE"
        }

        "construct url with initalizing sql" in {
            s (H2Url (Memory (),
                      initSql = Some("RUNSCRIPT FROM '~/create.sql'")))  must_==  "jdbc:h2:mem:;INIT=RUNSCRIPT FROM '~/create.sql'"

            s (H2Url (Memory (), initSql = Some("X")))  must_==  "jdbc:h2:mem:;INIT=X"
        }

        "construct url with username" in {
            s (H2Url (Memory (), user = Some("x66")))  must_==  "jdbc:h2:mem:;USER=x66"
            s (H2Url (Memory (), user = Some("no")))  must_==  "jdbc:h2:mem:;USER=no"
        }

        "construct url with password" in {
            s (H2Url (File ("b"), password = Some("pwd")))  must_==  "jdbc:h2:file:b;PASSWORD=pwd"
            s (H2Url (File ("b"), password = Some("dwp")))  must_==  "jdbc:h2:file:b;PASSWORD=dwp"
        }

        "construct url with IGNORECASE" in {
            s (H2Url (File ("c"), ignoreCase = true))  must_==  "jdbc:h2:file:c;IGNORECASE=TRUE"
        }

        "construct url with IGNORE_UNKNOWN_SETTINGS" in {
            s (H2Url (File ("d"), ignoreUnknownSettings = true))  must_==  "jdbc:h2:file:d;IGNORE_UNKNOWN_SETTINGS=TRUE"
        }

        "construct url with access mode specified" in {
            s (H2Url (File ("e"),
                      accessModeData = Some(AccessModeDataR)))  must_==  "jdbc:h2:file:e;ACCESS_MODE_DATA=r"

            s (H2Url (File ("e"),
                      accessModeData = Some(AccessModeDataRW)))  must_==  "jdbc:h2:file:e;ACCESS_MODE_DATA=rw"

            s (H2Url (File ("e"),
                      accessModeData = Some(AccessModeDataRWS)))  must_==  "jdbc:h2:file:e;ACCESS_MODE_DATA=rws"

            s (H2Url (File ("e"),
                      accessModeData = Some(AccessModeDataRWD)))  must_==  "jdbc:h2:file:e;ACCESS_MODE_DATA=rwd"
        }

        "construct url with compatibility mode specified" in {
            s (H2Url (File ("f"), mode = Some(DB2)))  must_==  "jdbc:h2:file:f;MODE=DB2"
            s (H2Url (File ("f"), mode = Some(Derby)))  must_==  "jdbc:h2:file:f;MODE=Derby"
            s (H2Url (File ("f"), mode = Some(HSQLDB)))  must_==  "jdbc:h2:file:f;MODE=HSQLDB"
            s (H2Url (File ("f"), mode = Some(MSSQLServer)))  must_==  "jdbc:h2:file:f;MODE=MSSQLServer"
            s (H2Url (File ("f"), mode = Some(MySQL)))  must_==  "jdbc:h2:file:f;MODE=MySQL"
            s (H2Url (File ("f"), mode = Some(Oracle)))  must_==  "jdbc:h2:file:f;MODE=Oracle"
            s (H2Url (File ("f"), mode = Some(PostgreSQL)))  must_==  "jdbc:h2:file:f;MODE=PostgreSQL"
        }

        "construct url with autoReconnect on" in {
            s (H2Url (File ("g"), autoReconnect = true))  must_==  "jdbc:h2:file:g;AUTO_RECONNECT=TRUE"
        }

        "construct url with autoServer on" in {
            s (H2Url (File ("h"), autoServer = true))  must_==  "jdbc:h2:file:h;AUTO_SERVER=TRUE"
        }

        "construct url with page size specified" in {
            s (H2Url (File ("i"), pageSize = Some(512)))  must_==  "jdbc:h2:file:i;PAGE_SIZE=512"
            s (H2Url (File ("i"), pageSize = Some(1024)))  must_==  "jdbc:h2:file:i;PAGE_SIZE=1024"
        }

        "construct url with specific trace level for System.out" in {
            s (H2Url (File ("k"), traceLevelSystemOut = Some(Off)))  must_==  "jdbc:h2:file:k;TRACE_LEVEL_SYSTEM_OUT=0"
            s (H2Url (File ("k"), traceLevelSystemOut = Some(H2Url.Error)))  must_==  "jdbc:h2:file:k;TRACE_LEVEL_SYSTEM_OUT=1"
            s (H2Url (File ("k"), traceLevelSystemOut = Some(H2Url.Info)))  must_==  "jdbc:h2:file:k;TRACE_LEVEL_SYSTEM_OUT=2"
            s (H2Url (File ("k"), traceLevelSystemOut = Some(H2Url.Debug)))  must_==  "jdbc:h2:file:k;TRACE_LEVEL_SYSTEM_OUT=3"
        }

        "construct url with specific trace level for file" in {
            s (H2Url (File ("m"), traceLevelFile = Some(Off)))  must_==  "jdbc:h2:file:m;TRACE_LEVEL_FILE=0"
            s (H2Url (File ("m"), traceLevelFile = Some(H2Url.Error)))  must_==  "jdbc:h2:file:m;TRACE_LEVEL_FILE=1"
            s (H2Url (File ("m"), traceLevelFile = Some(H2Url.Info)))  must_==  "jdbc:h2:file:m;TRACE_LEVEL_FILE=2"
            s (H2Url (File ("m"), traceLevelFile = Some(H2Url.Debug)))  must_==  "jdbc:h2:file:m;TRACE_LEVEL_FILE=3"
            s (H2Url (File ("m"), traceLevelFile = Some(Slf4j)))  must_==  "jdbc:h2:file:m;TRACE_LEVEL_FILE=4"
        }

        "construct url with cache size specified" in {
            s (H2Url (File ("n"), pageSize = Some(5)))  must_==  "jdbc:h2:file:n;PAGE_SIZE=5"
            s (H2Url (File ("n"), pageSize = Some(1)))  must_==  "jdbc:h2:file:n;PAGE_SIZE=1"
        }

        "construct url with other options" in {
            s (H2Url (File ("x"), otherOptions = ";blahblah"))  must_==  "jdbc:h2:file:x;blahblah"
        }
    }
}
