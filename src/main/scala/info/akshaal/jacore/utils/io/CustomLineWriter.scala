/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io

import java.io.Writer

/**
 * Writer that prints lines using provided function.
 *
 * @param linesPrinter function that will be invoked when there is lines to print
 */
final class CustomLineWriter (linesPrinter : String => Unit) extends Writer {
    private[this] val buf : StringBuilder = new StringBuilder

    /**
     * {InheritedDoc}
     */
    override def flush () : Unit = {}

    /**
     * {InheritedDoc}
     */
    override def close () : Unit = {}

    /**
     * {InheritedDoc}
     */
    override def write (cbuf : Array[Char], off : Int, len : Int) : Unit = {
        buf.appendAll (cbuf, off, len)

        val idx = buf.lastIndexOf ("\n")
        if (idx != -1) {
            linesPrinter (buf.substring(0, idx + 1).stripLineEnd)
            buf.delete (0, idx + 1)
        }
    }
}
