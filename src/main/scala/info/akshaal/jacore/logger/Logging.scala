/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package logger


/**
 * Abstract logging trait.
 */
private[logger] trait AbstractLogging {
    /**
     * Implicit instance of logger.
     */
    protected[logger] implicit val logger : Logger

    // Business logic logging -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    def businessLogicInfo (str : String, group : Boolean = false) : Unit =
        logger.businessLogicInfo (str, group = group)

    @inline
    def businessLogicWarning (str : String, group : Boolean = false) : Unit =
        logger.businessLogicWarning (str, group = group)

    @inline
    def businessLogicProblem (str : String, group : Boolean = false) : Unit =
        logger.businessLogicProblem (str, group = group)

    // Log with exception - - - - - - - - - - -- - - - - - - - - - - - - - - - -  -

    @inline
    def debug (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.debug (str, exc, group = group)

    @inline
    def info (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.info (str, exc, group = group)

    @inline
    def warn (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.warn (str, exc, group = group)

    @inline
    def error (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.error (str, exc, group = group)

    // Lazy log - - - - - - - - - - -- - - - - - - - - - - - - - - - -  - - - - -  -

    @inline
    def debugLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.debugLazy (obj, exc, group = group)

    @inline
    def infoLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.infoLazy (obj, exc, group = group)

    @inline
    def warnLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.warnLazy (obj, exc, group = group)

    @inline
    def errorLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        logger.errorLazy (obj, exc, group = group)

    // Levels - - - - - - - - - - -- - - - - - - - - - - - - - - - -  - - - - - - - -

    @inline
    def isDebugEnabled : Boolean = logger.isDebugEnabled

    @inline
    def isInfoEnabled : Boolean = logger.isInfoEnabled

    @inline
    def isWarnEnabled : Boolean = logger.isWarnEnabled

    @inline
    def isErrorEnabled : Boolean = logger.isErrorEnabled
}


/**
 * Logging trait. Provides convenient logging methods with the default logger created
 * using the current class name.
 */
trait Logging extends AbstractLogging {
    protected[logger] override implicit val logger : Logger = Logger.get (this)
}


/**
 * Logger trait for dummy logging. Output of any logging request goes to the console only.
 * Debugging requests are suppressed.
 */
trait DummyLogging extends {
    protected[logger] override implicit val logger : Logger = DummyLogger
} with AbstractLogging


/**
 * Logger trait for quick debugging. Any output of the logger goes to console.
 */
trait QuickDebugLogging extends {
    protected[logger] override implicit val logger : Logger = QuickDebugLogger
} with AbstractLogging

