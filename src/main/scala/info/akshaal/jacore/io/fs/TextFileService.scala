/** Akshaal (C) 2009-2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package fs

import java.io.File

import actor.Operation

/**
 * Fast synchronous file reader/writer.
 */
trait TextFileService {
    /**
     * Write a given content into the file.
     *
     * @param file write content to this file
     * @param content string to write into the file
     */
    def writeFileOperation (file : File, content : String) : Operation.WithResult [Unit]

    /**
     * Open file and initiate reading from the file.
     *
     * @param file file to read from
     * @param size maximum number of bytes to read, if not given, then the whole file will be read
     *        and some time will be spent to obtain file size
     * @return string with data read from file
     */
    def readFileOperation (file : File, size : Option[Int] = None) : Operation.WithResult [String]
}
