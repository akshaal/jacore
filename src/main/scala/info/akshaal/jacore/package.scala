/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal

import java.io.{IOException, Closeable, File, BufferedReader, InputStreamReader, FileInputStream}
import java.lang.{Iterable => JavaIterable}
import java.text.DateFormat
import java.util.concurrent.{ExecutorService, Callable, Future}
import java.util.Date

import com.google.inject.{Injector, Inject => GuiceInject, Singleton => GuiceSingleton}
import com.google.inject.name.{Named => GuiceNamed}

import scala.collection.mutable.ListBuffer

import jacore.logger.Logger
import jacore.utils.TimeValueFromNumberCreator
import jacore.utils.io.db.JdbcUrl

/**
 * Package object that contains most useful and common functions.
 */
package object jacore {
    // Make timeunit visible
    type TimeValue = utils.TimeValue
    val TimeValue = utils.TimeValue

    // Make results visible
    type Result [A] = utils.Result [A]
    type Success [A] = utils.Success [A]
    type Failure [A] = utils.Failure [A]
    val Success = utils.Success
    val Failure = utils.Failure

    // Make jacore Enumeration visible
    type JacoreEnum = utils.JacoreEnum


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Collections

    /**
     * Create list repeating code n times.
     *
     * @param <T> type of elements in list
     * @param n number of times to repeat execution of code block
     * @param code code that is executed 'n' time in order to produce elements of the list
     * @return list with 'n' elements constructed using given 'code'
     */
    @inline
    def repeatToList[T] (n : Int) (code : => T) : List[T] = {
        var l : List[T] = Nil
        var i = n

        while (i > 0) {
            i -= 1
            l = code :: l
        }

        l
    }

    /**
     * Iterate over java iterable. It is compiled to very efficient code.
     *
     * @param <T> type of items in the iterable
     * @param c java iterable
     * @param f function to call for each item produced by 'c'
     */
    @inline
    def iterateOverJavaIterable[T] (c : JavaIterable[T]) (f : T => Unit) : Unit = {
        val it = c.iterator

        while (it.hasNext) {
            f (it.next)
        }
    }


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Nulls

    /**
     * Convert possible null value using code provided.
     *
     * @param <T> type of value to convert
     * @param ref possibly null value
     * @param code run this code if ref is null
     * @return 'ref' if it is not null, or value returned by 'code'
     */
    @inline
    def convertNull[T] (ref : T) (code : => T) : T = {
        if (ref == null) code else ref.asInstanceOf[T]
    }

    /**
     * Throws exception if value is null, otherwise returns value
     *
     * @param <T> type of value to check for null
     * @param ref possibly null value
     * @param thr exception to throw
     * @return 'ref'
     */
    @inline
    def throwIfNull[T] (ref : T) (thr : => Throwable) : T = {
        if (ref == null) {
            throw thr
        } else {
            ref
        }
    }


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Exceptions

    /**
     * Execute block of code and print a message if block of code throws
     * an exception.
     *
     * @param message message to be loggen upon exception
     * @param code code to try to run
     * @param logger logger to use for logging
     */
    @inline
    def logIgnoredException (message : => String) (code : => Unit) (implicit logger : Logger) : Unit =
    {
        try {
            code
        } catch {
            case ex: Exception =>
                logger.error (message, ex)
        }
    }


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // IO

    /**
     * Implicit conversion from string to File object.
     *
     * @param absolutePath absolute path to a file
     * @return file constructed for the given path
     */
    @inline
    implicit def string2file (absolutePath : String) : File = new File (absolutePath)

    /**
     * Execute code with closeable IO and close resource after 'code' is executed.
     *
     * @param <I> closeable resource type
     * @param createCode code that will be used to construct closeable resource
     * @param code code that is supposed to use closeable resource constructed by 'createCode'
     * @return value returned by 'code' function
     */
    @inline
    def withCloseableIO[I <: Closeable, T] (createCode : => I) (code : I => T) : T = {
        var inputStream : I = null.asInstanceOf[I]

        try {
            inputStream = createCode
            code (inputStream.asInstanceOf[I])
        } catch {
            case ex : IOException =>
                throw new IOException ("Error during access to input stream" +:+ ex, ex)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close ()
                } catch {
                    case ex : IOException =>
                        throw new IOException ("Error closing input stream" +:+ ex, ex)
                }
            }
        }
    }

    /**
     * Read content of the file. Lines feeds are not preserved and replaced with just \n.
     *
     * @param file file to read
     * @param encoding encoding to use
     * @return read file as a string
     */
    @inline
    def readFileLinesAsString (file : File, encoding : String) : String = {
        withCloseableIO (new BufferedReader (
                            new InputStreamReader (
                                new FileInputStream (file), encoding))) (
            reader => {
                val buf = new ListBuffer [String]
                var cont = true
                while (cont) {
                    val line = reader.readLine
                    if (line == null) {
                        cont = false
                    } else {
                        buf += line
                    }
                }

                buf.mkString ("\n")
            }
        )
    }

    /**
     * Implicitly converts JdbcUrl values to String. This makes it possible
     * to use instances of JdbcUrl in places where String is expected.
     * Conversion is done by toString method of 'jdbcUrl' object.
     *
     * @param jdbcUrl jdbc url to convert to string
     * @return converted jdbc url
     */
    @inline
    implicit def jdbcUrl2String (jdbcUrl : JdbcUrl) : String = jdbcUrl.toString


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Rich string

    /**
     * Additional methods for String class.
     *
     * @param str target string for additional operations
     */
    final class RichJacoreString (str : String) {
        /**
         * Append this string to the optional exception. If no exception
         * is defined then just return this string.
         *
         * @param optionThrowable optional throwable
         * @return this string or concatenation with ': ' and exception
         */
        @inline
        def +:+ (optionThrowable : Option[_ <: Throwable]) : String = {
            optionThrowable match {
                case None => str
                case Some (exc) => +:+ (exc)
            }
        }

        /**
         * Concatenate this string with ': ' and the message of the given throwable.
         *
         * @param throwable to concatenate with.
         * @return result of concatenation
         */
        @inline
        def +:+ (throwable : Throwable) : String = {
            str + ": " + throwable.getMessage
        }

        /**
         * Concatenate this string with ': ' and some other value
         *
         * @param other other value to concatenate with
         * @return result of concatenation
         */
        @inline
        def +:+ (other : Any) : String = {
            str + ": " + other
        }
    }

    /**
     * Concerts String to RichJacoreString to provide rich set of additional operations over string.
     *
     * @param str string to enrich
     * @return enriched string
     */
    @inline
    implicit def string2RichString (str : String) : RichJacoreString = new RichJacoreString (str)


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Concurrent

    /**
     * Wrapper class for ExecutorService. This class provides additional convenient methods.
     * 
     * @param executorService executor service instance to wrap
     */
    final class RichExecutorService (executorService : ExecutorService) {
        /**
         * Submit some code for execution.
         *
         * @param <A> type of the result produced by block of 'code'
         * @param code code to be executed by exection service
         * @return Future that holds result of execution
         */
        @inline
        def submit [A] (code : => A) : Future [A] = {
            executorService.submit (new Callable [A] {
                override def call () : A = code
            })
        }
    }

    /**
     * Implicit method to implicitly construct RichExecutorService out of ExecutorService.
     * 
     * @param executorService executor service to wrap
     * @return wrapped executor service
     */
    @inline
    implicit def executorService2RichExecutorService (executorService : ExecutorService) : RichExecutorService = {
        new RichExecutorService (executorService)
    }

    /**
     * Create object of interface Runnable which will execute the given block of code.
     *
     * @param code code to be executed by run method of returned Runnable object.
     * @return runnable object that will invoke the given code upon execution of run method
     */
    @inline
    def mkRunnable (code : => Unit) : Runnable = {
        new Runnable () {
            def run () {
                code
            }
        }
    }

    /**
     * Returns class loader, either thread's class loader or a classloader used to load
     * this class.
     *
     * @return class loader, never null
     */
    def defaultClassLoader : ClassLoader = {
        var classLoader : ClassLoader = null

        try {
            classLoader = currentThread.getContextClassLoader
        } catch {
            case _ => ()
        }

        if (classLoader == null) {
            this.getClass.getClassLoader
        } else {
            classLoader
        }
    }


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Rich stuff for Guice

    type Named = GuiceNamed @scala.annotation.target.param
    type Inject = GuiceInject
    type Singleton = GuiceSingleton

    /**
     * Richness for Guice Injector.
     *
     * @param injector guice injector to enrich
     */
    final class RichInjector (injector : Injector) {
        /**
         * Get instance of the given type.
         *
         * @param <T> type of instance to get from guice injector
         * @return instance of type 'T' create by guice
         */
        @inline
        def getInstanceOf[T](implicit clazz : ClassManifest[T]) : T = {
            injector.getInstance (clazz.erasure).asInstanceOf[T]
        }
    }

    /**
     * Implicit convertion to rich injector.
     *
     * @param injector injector to enrich
     * @return rich injector
     */
    @inline
    implicit def injector2richInjector (injector : Injector) : RichInjector = {
        new RichInjector (injector)
    }


    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////
    // Time

    /**
     * Convenient method to quickly format current date and time using FULL format.
     *
     * @return formatted current date and time
     */
    def formattedCurrentTime () : String = {
        DateFormat.getDateTimeInstance (DateFormat.FULL, DateFormat.FULL).format (new Date)
    }

    /**
     * Converts Long to TimeValueFromNumberCreator
     *
     * @param x long value to convert
     * @return TimeValue creator (DSL for constructing TimeValue)
     */
    @inline
    implicit def long2TimeValueFromNumberCreator (x : Long) : TimeValueFromNumberCreator =
        new TimeValueFromNumberCreator (x)

    /**
     * Converts Int to TimeValueFromLongCreator
     *
     * @param x int value to convert
     * @return TimeValue creator (DSL for constructing TimeValue)
     */
    @inline
    implicit def int2TimeValueFromNumberCreator (x : Int) : TimeValueFromNumberCreator =
        new TimeValueFromNumberCreator (x)

    /**
     * Parse string to construct time value.
     *
     * @param x string to parse
     * @return parsed time value
     */
    @inline
    implicit def string2TimeValue (x : String) : TimeValue = TimeValue.parse (x)
}
