package info.akshaal.jacore
package logger

import org.slf4j.LoggerFactory
import org.slf4j.{Logger => SlfLogger}

/**
 * Abstract logger
 */
sealed abstract class Logger extends NotNull {
    def debug (str : String) : Unit
    def info (str : String) : Unit
    def warn (str : String) : Unit
    def error (str : String) : Unit
    def businessLogicInfo (str : String) : Unit
    def businessLogicWarning (str : String) : Unit
    def businessLogicProblem (str : String) : Unit

    def debug (str : String, e : Throwable) : Unit
    def info (str : String, e : Throwable) : Unit
    def warn (str : String, e : Throwable) : Unit
    def error (str : String, e : Throwable) : Unit

    def debugLazy (obj : => AnyRef) : Unit
    def infoLazy (obj : => AnyRef) : Unit
    def warnLazy (obj : => AnyRef) : Unit
    def errorLazy (obj : => AnyRef) : Unit

    def debugLazy (obj : AnyRef, e : Throwable) : Unit
    def infoLazy (obj : AnyRef, e : Throwable) : Unit
    def warnLazy (obj : AnyRef, e : Throwable) : Unit
    def errorLazy (obj : AnyRef, e : Throwable) : Unit

    def isDebugEnabled : Boolean
    def isInfoEnabled : Boolean
    def isWarnEnabled : Boolean
    def isErrorEnabled : Boolean
}

/**
 * Logger instantiation helper.
 */
final object Logger {
    private[this] val BusinessLogicSuffix = ".BUSINESS_LOGIC"
    private[this] val EnhancedClass = """^(.*)\$\$EnhancerByGuice\$\$[0-9a-fA-F]+?$""".r

    def isBusinessLogicFQCN (fqcn : String) : Boolean = fqcn.endsWith (BusinessLogicSuffix)

    def get (name : String): Logger =
         new DefaultLogger (LoggerFactory.getLogger (name),
                            LoggerFactory.getLogger (name + BusinessLogicSuffix))

    def get (any : AnyRef): Logger = get (loggerNameForClass (any.getClass.getName))

    def get: Logger =
         get (loggerNameForClass(new Throwable().getStackTrace()(1).getClassName))

    private def loggerNameForClass (className : String) = {
        val strippedClassName =
                className match {
                    case EnhancedClass (clazz) => clazz
                    case _ => className
                }

        if (strippedClassName.endsWith("$"))
            strippedClassName.substring(0, strippedClassName.length - 1)
        else
            strippedClassName
    }
}

/**
 * Default logger.
 */
final class DefaultLogger (slfLogger : SlfLogger, businessSlfLogger : SlfLogger) extends Logger {
    @inline
    override def debug (str : String) = slfLogger.debug (str)

    @inline
    override def info (str : String)  = slfLogger.info (str)

    @inline
    override def warn (str : String)  = slfLogger.warn (str)

    @inline
    override def error (str : String) = slfLogger.error (str)

    @inline
    def businessLogicInfo (str : String) = businessSlfLogger.info (str)

    @inline
    def businessLogicWarning (str : String) = businessSlfLogger.warn (str)

    @inline
    def businessLogicProblem (str : String) = businessSlfLogger.error (str)

    // Log with exception
    
    @inline
    override def debug (str : String, e : Throwable) = slfLogger.debug (str, e)

    @inline
    override def info (str : String, e : Throwable)  = slfLogger.info (str, e)

    @inline
    override def warn (str : String, e : Throwable)  = slfLogger.warn (str, e)

    @inline
    override def error (str : String, e : Throwable) = slfLogger.error (str, e)
    
    // Lazy log
    
    @inline
    override def debugLazy (obj : => AnyRef) =
        if (slfLogger.isDebugEnabled) this.debug (obj.toString)
    
    @inline
    override def infoLazy (obj : => AnyRef) =
        if (slfLogger.isInfoEnabled) this.info (obj.toString)
    
    @inline
    override def warnLazy (obj : => AnyRef)  =
        if (slfLogger.isWarnEnabled) this.warn (obj.toString)

    @inline
    override def errorLazy (obj : => AnyRef) =
        if (slfLogger.isErrorEnabled) this.error (obj.toString)
    
    // Lazy log with exception
    
    @inline
    override def debugLazy (obj : AnyRef, e : Throwable) =
        if (slfLogger.isDebugEnabled) this.debug (obj.toString, e)
    
    @inline
    override def infoLazy (obj : AnyRef, e : Throwable)  =
        if (slfLogger.isInfoEnabled) this.info (obj.toString, e)
    
    @inline
    override def warnLazy (obj : AnyRef, e : Throwable)  =
        if (slfLogger.isWarnEnabled) this.warn (obj.toString, e)
    
    @inline
    override def errorLazy (obj : AnyRef, e : Throwable) =
        if (slfLogger.isErrorEnabled) this.error (obj.toString, e)

    // Logging levels
    @inline
    override def isDebugEnabled = slfLogger.isDebugEnabled

    @inline
    override def isInfoEnabled = slfLogger.isInfoEnabled

    @inline
    override def isWarnEnabled = slfLogger.isWarnEnabled

    @inline
    override def isErrorEnabled = slfLogger.isErrorEnabled
}

/**
 * Dummy logger. Shows message on console. Debug messages are suppressed.
 */
class DummyLogger extends Logger {
    @inline
    override def debug (str : String) = ()
    
    @inline
    override def info (str : String)  =
            println (formattedCurrentTime + " ::: INFO: " + str)

    @inline
    override def warn (str : String)  =
            System.err.println (formattedCurrentTime + " ::: WARN: " + str)

    @inline
    override def error (str : String) =
            System.err.println (formattedCurrentTime + " ::: ERROR: " + str)

    @inline
    def businessLogicInfo (str : String) =
        println (formattedCurrentTime + " ::: BUSINESS LOGIC INFO: " + str)

    @inline
    def businessLogicWarning (str : String) =
        println (formattedCurrentTime + " ::: BUSINESS LOGIC WARNING: " + str)

    @inline
    def businessLogicProblem (str : String) =
        println (formattedCurrentTime + " ::: BUSINESS LOGIC PROBLEM: " + str)

    // Log with exception

    @inline
    override def debug (str : String, e : Throwable) = ()
    
    @inline
    override def info (str : String, e : Throwable)  = {
        info (str)
        e.printStackTrace
    }

    @inline
    override def warn (str : String, e : Throwable)  = {
        warn (str)
        e.printStackTrace
    }

    @inline
    override def error (str : String, e : Throwable) = {
        error (str)
        e.printStackTrace
    }

    // Lazy log

    @inline
    override def debugLazy (obj : => AnyRef) = ()

    @inline
    override def infoLazy (obj : => AnyRef)  = info (obj.toString)

    @inline
    override def warnLazy (obj : => AnyRef)  = info (obj.toString)

    @inline
    override def errorLazy (obj : => AnyRef) = info (obj.toString)

    // Lazy log with exception

    @inline
    override def debugLazy (obj : AnyRef, e : Throwable) = ()

    @inline
    override def infoLazy (obj : AnyRef, e : Throwable)  = info (obj.toString, e)

    @inline
    override def warnLazy (obj : AnyRef, e : Throwable)  = warn (obj.toString, e)

    @inline
    override def errorLazy (obj : AnyRef, e : Throwable) = error (obj.toString, e)

    // Logging levels
    @inline
    def isDebugEnabled = false

    @inline
    def isInfoEnabled = true

    @inline
    def isWarnEnabled = true

    @inline
    def isErrorEnabled = true
}

/**
 * Instance of DummyLogger.
 */
object DummyLogger extends DummyLogger

/**
 * QuickDebugLogger. The same as DummyLogger, but debug message are not suppresesd.
 */
class QuickDebugLogger extends DummyLogger {
    override def debug (str : String) = println ("DEBUG: " + str)

    override def debug (str : String, e : Throwable) = {
        debug (str)
        e.printStackTrace
    }

    override def debugLazy (obj : => AnyRef) = debug (obj.toString)
    override def debugLazy (obj : AnyRef, e : Throwable) = debug (obj.toString, e)
    override def isDebugEnabled = true
}

/**
 * QuickDebugLogger instance.
 */
object QuickDebugLogger extends QuickDebugLogger
