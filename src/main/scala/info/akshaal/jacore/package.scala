/*
 * Implicits.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal

import java.io.{IOException, Closeable, File, BufferedReader, InputStreamReader, FileInputStream}
import java.lang.{Iterable => JavaIterable}

import com.google.inject.{Injector, Inject => GuiceInject, Singleton => GuiceSingleton}
import com.google.inject.name.{Named => GuiceNamed}

import scala.collection.mutable.ListBuffer
import java.util.concurrent.{ExecutorService, Callable, Future}

import jacore.logger.Logger

package object jacore {
    // Make timeunit visible
    type TimeUnit = utils.TimeUnit
    val TimeUnit = utils.TimeUnit

    // Make results visible
    type Result [A] = utils.Result [A]
    type Success [A] = utils.Success [A]
    type Failure [A] = utils.Failure [A]
    val Success = utils.Success
    val Failure = utils.Failure

    // File conversion
    @inline
    implicit def string2file (absolutePath : String) : File = new File (absolutePath)

    /**
     * Create list repeating code n times.
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
     * Create object of interface Runnable which will execute the given
     * block of code.
     */
    @inline
    def mkRunnable (code : => Unit) = {
        new Runnable () {
            def run () {
                code
            }
        }
    }

    /**
     * Convert possible null value using code provided.
     * @param ref possibly null value
     * @param code run this code if ref is null
     *
     * TODO: This method must return NotNull instance
     */
    @inline
    def convertNull[T] (ref : T) (code : => T) : T = {
        if (ref == null) code else ref.asInstanceOf[T]
    }

    /**
     * Throws exception if value is null, otherwise returns value
     * @param ref possibly null value
     * @param thr exception to throw
     */
    @inline
    def throwIfNull[T] (ref : T) (thr : => Throwable) : T = {
        if (ref == null) {
            throw thr
        } else {
            ref
        }
    }

    /**
     * Execute code with closeable IO.
     * TODO: Code must be executed with NotNull argument
     */
    @inline
    def withCloseableIO[I <: Closeable, T] (createCode : => I) (code : I => T) : T = {
        var inputStream : I = null.asInstanceOf[I]

        try {
            inputStream = createCode
            code (inputStream.asInstanceOf[I])
        } catch {
            case ex : IOException =>
                throw new IOException ("Error during access to input stream: "
                                       + ex.getMessage,
                                       ex)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close ()
                } catch {
                    case ex : IOException =>
                        throw new IOException ("Error closing input stream: "
                                               + ": " + ex.getMessage,
                                               ex)
                }
            }
        }
    }

    /**
     * Read content of the file. Lines feeds are not preserved and replaced with just \n.
     */
    @inline
    def readFileLinesAsString (path : String, encoding : String) : String = {
        withCloseableIO (new BufferedReader (
                            new InputStreamReader (
                                new FileInputStream (path), encoding))) (
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
     * Execute block of code and print a message if block of code throws
     * an exception.
     */
    @inline
    def logIgnoredException (message : => String) (code : => Unit) (implicit logger : Logger) =
    {
        try {
            code
        } catch {
            case ex: Exception =>
                logger.error (message, ex)
        }
    }

    /**
     * Iterate over java iterable. It is compiled to very efficient code.
     */
    @inline
    def iterateOverJavaIterable[T] (c : JavaIterable[T]) (f : T => Unit) {
        val it = c.iterator

        while (it.hasNext) {
            f (it.next)
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // Concurrent

    final class RichExecutorService (executorService : ExecutorService) {
        @inline
        def submit [A] (code : => A) : Future [A] = {
            executorService.submit (new Callable [A] {
                override def call () : A = code
            })
        }
    }

    @inline
    implicit def executorService2RichExecutorService (executorService : ExecutorService) : RichExecutorService = {
        new RichExecutorService (executorService)
    }

    // /////////////////////////////////////////////////////////////////////
    // Rich stuff for Guice

    type Named = GuiceNamed @scala.annotation.target.param
    type Inject = GuiceInject
    type Singleton = GuiceSingleton

    /**
     * Richness for Guice Injector.
     * @param injector guice injector to enrich
     */
    final class RichInjector (injector : Injector) {
        @inline
        def getInstanceOf[T](implicit clazz : ClassManifest[T]) : T = {
            injector.getInstance (clazz.erasure).asInstanceOf[T]
        }
    }

    /**
     * Implicit convertion to rich injector.
     */
    @inline
    implicit def injector2richInjector (injector : Injector) : RichInjector = {
        new RichInjector (injector)
    }

    // /////////////////////////////////////////////////////////////////////
    // Time

    /**
     * Converts Long to TimeUnitFromNumberCreator
     */
    @inline
    implicit def long2TimeUnitFromNumberCreator (x : Long) =
        new TimeUnitFromNumberCreator (x)

    /**
     * Converts Int to TimeUnitFromLongCreator
     */
    @inline
    implicit def int2TimeUnitFromNumberCreator (x : Int) =
        new TimeUnitFromNumberCreator (x)

    @inline
    implicit def string2TimeUnit (x : String) = TimeUnit.parse (x)

    /**
     * Wrapper for Long that makes it possible to convert
     * it to TimeUnit object.
     */
    final class TimeUnitFromNumberCreator (x : Long) extends NotNull {
        @inline
        def nanoseconds  = mk (x)

        @inline
        def microseconds = mk (x * 1000L)

        @inline
        def milliseconds = mk (x * 1000L * 1000L)

        @inline
        def seconds      = mk (x * 1000L * 1000L * 1000L)

        @inline
        def minutes      = mk (x * 1000L * 1000L * 1000L * 60L)

        @inline
        def hours        = mk (x * 1000L * 1000L * 1000L * 60L * 60L)

        @inline
        def mk (nano : Long) = new TimeUnit (nano)
    }
}