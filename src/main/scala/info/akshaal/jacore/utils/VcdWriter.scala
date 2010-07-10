/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.utils

import java.io.Writer

import scala.collection.mutable.HashMap

/**
 * Writes verilog dump. Very simple implementation..
 * @param signals a list of tuples (name, signal_bits)
 */
class VcdWriter (writer : Writer, signals : List[(String, Int)]) {
    private var lastTime : Option[Long] = None

    // (alias, bits) by name
    protected val byName = new HashMap [String, (Char, Int)]

    require (signals.size > 0, "No signals given")
    require (signals.size < 33, "Can't handle more than 32 signals")

    writer.append ("$timescale 1us $end\n")
    writer.append ("$scope module logic $end\n")

    for (((name, bits), index) <- signals.zipWithIndex) {
        val alias = (' ' + index + 1).asInstanceOf[Char]
        byName (name) = (alias, bits)
        writer.append ("$var wire " + bits + " " + alias + " " + name + " $end\n")
    }

    writer.append ("$upscope $end\n")
    writer.append ("$enddefinitions $end\n")

    writer.append ("$dumpvars\n")
    for ((name, bits) <- signals) {
        writer.append (formatValueChange (name, getDefault(name)) + "\n")
    }
    writer.append ("$end\n")

    /**
     * Add a value.
     */
    def addValue (time : Long, name : String, value : Int) : Unit = {
        lastTime match {
            case Some (timeV) if timeV >= time =>
                ()
                
            case _ =>
                writer.append ("#" + time + "\n")
                lastTime = Some (time)
        }
        
        writer.append (formatValueChange (name, value) + "\n")
    }

    /**
     * Returns default for the given name. Default implementation always returns 0.
     */
    protected def getDefault (name : String) : Int = 0

    /**
     * Format value change description.
     */
    protected def formatValueChange (name : String, value : Int) : String = {
        val builder = new StringBuilder
        val (alias, size) = byName (name)

        if (size > 1) {
            builder.append ('b')
        }

        for (i <- size to (1, -1)) {
            builder.append (if ((value & (1 << (i-1))) != 0) '1' else '0')
        }

        if (size > 1) {
            builder.append (' ')
        }

        builder.append (alias)

        builder.toString
    }
}
