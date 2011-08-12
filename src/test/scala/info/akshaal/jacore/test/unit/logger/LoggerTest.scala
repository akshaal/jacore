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

                logger.debug ("Debug level", group = true)
                logger.info ("Info level", group = true)
                logger.warn ("Warn level", group = true)
                logger.error ("Error level", group = true)

                logger.debugLazy (sideEffect ("debug lazy"), group = true)
                logger.infoLazy (sideEffect ("info lazy"), group = true)
                logger.warnLazy (sideEffect ("warn lazy"), group = true)
                logger.errorLazy (sideEffect ("error lazy"), group = true)
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

                logger.businessLogicInfo ("Business Logic Info level", group = true)
                logger.businessLogicWarning ("Business Logic Warning level", group = true)
                logger.businessLogicProblem ("Business Logic Problem level", group = true)

                businessLogicInfo ("2 Business Logic Info level", group = true)
                businessLogicWarning ("2 Business Logic Warning level", group = true)
                businessLogicProblem ("2 Business Logic Problem level", group = true)
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

                debug ("2 Debug level", group = true)
                info ("2 Info level", group = true)
                warn ("2 Warn level", group = true)
                error ("2 Error level", group = true)

                debugLazy (sideEffect ("2 debug lazy"), group = true)
                infoLazy (sideEffect ("2 info lazy"), group = true)
                warnLazy (sideEffect ("2 warn lazy"), group = true)
                errorLazy (sideEffect ("2 error lazy"), group = true)
            }

            T must not be null
        }
    }

    private def sideEffect (str : String) = {
        logger.debug ("Computing argument for lazy log message" +:+ str)
        str
    }
}
