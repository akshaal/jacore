/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package logger

import org.slf4j.{LoggerFactory, MarkerFactory}


/**
 * Abstract logger.
 */
sealed abstract class Logger extends NotNull {
    // Business logic logging -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    def businessLogicInfo (str : String, group : Boolean = false) : Unit
    def businessLogicWarning (str : String, group : Boolean = false) : Unit
    def businessLogicProblem (str : String, group : Boolean = false) : Unit

    // Normal logging -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    def debug (str : String, exc : Throwable = null, group : Boolean = false) : Unit
    def info (str : String, exc : Throwable = null, group : Boolean = false) : Unit
    def warn (str : String, exc : Throwable = null, group : Boolean = false) : Unit
    def error (str : String, exc : Throwable = null, group : Boolean = false) : Unit

    // Lazy log -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    def debugLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit
    def infoLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit
    def warnLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit
    def errorLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit

    // Logging levels -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    def isDebugEnabled : Boolean
    def isInfoEnabled : Boolean
    def isWarnEnabled : Boolean
    def isErrorEnabled : Boolean
}


/**
 * Logger instantiation helper.
 */
final object Logger {
    private[this] final val enhancedClass = """^(.*)\$\$EnhancerByGuice\$\$[0-9a-fA-F]+?$""".r

    /**
     * Get logger for the given name (usually it is FQCN of a class).
     * @param name name
     */
    def get (name : String) : Logger = new Slf4jLogger (name)

    /**
     * Get logger for the given object. FQCN of the class is used as the name of the logger.
     */
    def get (any : AnyRef) : Logger = get (loggerNameForClass (any.getClass.getName))

    /**
     * Get name of the caller class. FQCN of the caller class is used to get name.
     */
    def get : Logger = get (loggerNameForClass (new Throwable().getStackTrace () (1).getClassName))

    /**
     * Construct logger name from the given class name (FQCN). Garbage is removed from the name.
     */
    private def loggerNameForClass (className : String) : String = {
        val strippedClassName =
                className match {
                    case enhancedClass (clazz) => clazz
                    case _ => className
                }

        if (strippedClassName.endsWith ("$"))
            strippedClassName.substring (0, strippedClassName.length - 1)
        else
            strippedClassName
    }
}


/**
 * Information related to Slf4logger.
 */
object Slf4Logger {
    /**
     * Marker name for business messages.
     */
    val businessMarkerName = "BUSINESS"

    /**
     * Marker name for groupable messages.
     */
    val groupableMarkerName = "GROUPABLE"

    /**
     * Marker for business messages.
     */
    private[logger] val businessMarker = MarkerFactory.getMarker (businessMarkerName)

    /**
     * Marker for groupable messages.
     */
    private[logger] val groupableMarker = MarkerFactory.getMarker (groupableMarkerName)

    /**
     * Marker for groupable business messages.
     */
    val groupableBusinessMarker = MarkerFactory.getMarker (groupableMarkerName)

    // Setup markers
    groupableBusinessMarker.add (businessMarker)
}


/**
 * Default logger, based on slf4j.
 */
final class Slf4jLogger (name : String) extends Logger {
    import Slf4Logger._

    private[this] final val slfLogger = LoggerFactory.getLogger (name)

    // Business logic logging -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    def businessLogicInfo (str : String, group : Boolean = false) : Unit =
        slfLogger.info (if (group) groupableBusinessMarker else businessMarker, str)

    @inline
    def businessLogicWarning (str : String, group : Boolean = false) : Unit =
        slfLogger.warn (if (group) groupableBusinessMarker else businessMarker, str)

    @inline
    def businessLogicProblem (str : String, group : Boolean = false) : Unit =
        slfLogger.error (if (group) groupableBusinessMarker else businessMarker, str)

    // Log with exception -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    override def debug (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        if (group)
            slfLogger.debug (groupableMarker, str, exc)
        else
            slfLogger.debug (str, exc)

    @inline
    override def info (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        if (group)
            slfLogger.info (groupableMarker, str, exc)
        else
            slfLogger.info (str, exc)

    @inline
    override def warn (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        if (group)
            slfLogger.warn (groupableMarker, str, exc)
        else
            slfLogger.warn (str, exc)

    @inline
    override def error (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        if (group)
            slfLogger.error (groupableMarker, str, exc)
        else
            slfLogger.error (str, exc)

    // Lazy log -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    override def debugLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        if (slfLogger.isDebugEnabled) this.debug (obj.toString, exc = exc, group = group)

    @inline
    override def infoLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        if (slfLogger.isInfoEnabled) this.info (obj.toString, exc = exc, group = group)

    @inline
    override def warnLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        if (slfLogger.isWarnEnabled) this.warn (obj.toString, exc = exc, group = group)

    @inline
    override def errorLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        if (slfLogger.isErrorEnabled) this.error (obj.toString, exc = exc, group = group)

    // Logging levels -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    override def isDebugEnabled : Boolean = slfLogger.isDebugEnabled

    @inline
    override def isInfoEnabled : Boolean = slfLogger.isInfoEnabled

    @inline
    override def isWarnEnabled : Boolean = slfLogger.isWarnEnabled

    @inline
    override def isErrorEnabled : Boolean = slfLogger.isErrorEnabled
}


/**
 * Dummy logger. Shows message on console. Debug messages are suppressed.
 */
sealed class DummyLogger extends Logger {
    // Business logic logging -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    def businessLogicInfo (str : String, group : Boolean = false) : Unit =
        println (formattedCurrentTime + " ::: BUSINESS LOGIC INFO" +:+ str)

    @inline
    def businessLogicWarning (str : String, group : Boolean = false) : Unit =
        println (formattedCurrentTime + " ::: BUSINESS LOGIC WARNING" +:+ str)

    @inline
    def businessLogicProblem (str : String, group : Boolean = false) : Unit =
        println (formattedCurrentTime + " ::: BUSINESS LOGIC PROBLEM" +:+ str)

    // Log with exception -  - - -  -- - - - - - - - - - - -  - - - - - - - - - -

    @inline
    override def debug (str : String, exc : Throwable = null, group : Boolean = false) : Unit =
        ()

    @inline
    override def info (str : String, exc : Throwable = null, group : Boolean = false) : Unit = {
        println (formattedCurrentTime + " ::: INFO" +:+ str)
        if (exc != null) exc.printStackTrace
    }

    @inline
    override def warn (str : String, exc : Throwable = null, group : Boolean = false) : Unit = {
        System.err.println (formattedCurrentTime + " ::: WARN" +:+ str)
        if (exc != null) exc.printStackTrace
    }

    @inline
    override def error (str : String, exc : Throwable = null, group : Boolean = false) : Unit = {
        System.err.println (formattedCurrentTime + " ::: ERROR" +:+ str)
        if (exc != null) exc.printStackTrace
    }

    // Lazy log - -- - - - -- - -  - - - - - - - - - - - - - - - - - - - - -- - - -

    @inline
    override def debugLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        ()

    @inline
    override def infoLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        info (obj.toString, exc)

    @inline
    override def warnLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        warn (obj.toString, exc)

    @inline
    override def errorLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        error (obj.toString, exc)

    // Logging levels  - - - - - - -  - - - - - - - - -- - - - - - - - - - - - - - - - -

    @inline
    def isDebugEnabled : Boolean = false

    @inline
    def isInfoEnabled : Boolean = true

    @inline
    def isWarnEnabled : Boolean = true

    @inline
    def isErrorEnabled : Boolean = true
}


/**
 * Instance of DummyLogger.
 */
object DummyLogger extends DummyLogger


/**
 * QuickDebugLogger. The same as DummyLogger, but debug message are not suppresesd.
 */
sealed class QuickDebugLogger extends DummyLogger {
    @inline
    override def debug (str : String, exc : Throwable = null, group : Boolean = false) : Unit = {
        debug (str)
        if (exc != null) exc.printStackTrace
    }

    @inline
    override def debugLazy (obj : AnyRef, exc : Throwable = null, group : Boolean = false) : Unit =
        debug (obj.toString, exc = null, group = group)

    @inline
    override def isDebugEnabled : Boolean = true
}


/**
 * QuickDebugLogger instance.
 */
object QuickDebugLogger extends QuickDebugLogger
