/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.logger

import unit.UnitTestHelper._

import logger.Logging

class LoggerTest extends JacoreSpecWithJUnit ("Logging trait specification")
{
    "Logging" should {
        "provide Logger instance" in {
            object T extends Logging {
                logger.debug ("Debug level")
                logger.info ("Info level")
                logger.warn ("Warn level")
                logger.error ("Error level")

                logger.debugLazy (sideEffect ("debug lazy"))
                logger.infoLazy (sideEffect ("info lazy"))
                logger.warnLazy (sideEffect ("warn lazy"))
                logger.errorLazy (sideEffect ("error lazy"))
            }
            
            T must not be null
        }

        "have support for business logic logging" in {
            object T extends Logging {
                logger.businessLogicInfo ("Business Logic Info level")
                logger.businessLogicWarning ("Business Logic Warning level")
                logger.businessLogicProblem ("Business Logic Problem level")

                businessLogicInfo ("2 Business Logic Info level")
                businessLogicWarning ("2 Business Logic Warning level")
                businessLogicProblem ("2 Business Logic Problem level")
            }

            T must not be null
        }

        "provide Logger methods as mixins" in {
            object T extends Logging {
                debug ("2 Debug level")
                info ("2 Info level")
                warn ("2 Warn level")
                error ("2 Error level")

                debugLazy (sideEffect ("2 debug lazy"))
                infoLazy (sideEffect ("2 info lazy"))
                warnLazy (sideEffect ("2 warn lazy"))
                errorLazy (sideEffect ("2 error lazy"))
            }
            
            T must not be null
        }
    }
 
    private def sideEffect (str : String) = {
        logger.debug ("Computing argument for lazy log message: " + str)
        str
    }
}
