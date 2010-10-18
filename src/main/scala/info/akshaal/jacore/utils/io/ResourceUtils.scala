/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils
package io

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import java.net.{URL, JarURLConnection}
import java.io.File

/**
 * Helper object to work with resources.
 */
object ResourceUtils {
    /**
     * Returns true if the given url points to jar file.
     *
     * @param url url
     */
    def isJarUrl (url : URL) : Boolean = {
        url.getProtocol match {
            case "jar" | "zip" | "wsjar" => true
            case _                       => false
        }
    }

    /**
     * Returns true if the given url is file.
     * 
     * @param url url
     */
    def isFileUrl (url : URL) : Boolean = {
        url.getProtocol == "file"
    }

    /**
     * Find resources starting from url and matching given predicate. Found resources
     * are added into buf.
     * @param buf add found resources here
     * @param url starts searching for resources from here
     * @param pred match this predicate
     */
    def findResources (buf : ListBuffer [URL],
                       url : URL,
                       pred : URL => Boolean) : Unit =
    {
        if (isFileUrl (url)) {
            findFileResources (buf, new File (url.getPath), pred)
        } else if (isJarUrl (url)) {
            findJarResources (buf, url, pred)
        } else {
            throw new IllegalArgumentException ("Unsupported URL" +:+ url)
        }
    }

    /**
     * Find file resources beginning from the given file matching the given predicate
     * and adding found files to the given buffer.
     * @param buf add results here
     * @param file starts from this file
     * @param pred predicate to match to
     */
    def findFileResources (buf : ListBuffer [URL],
                           file : File,
                           pred : URL => Boolean) : Unit =
    {
        val url = file.toURI.toURL

        if (pred (url)) {
            buf += url
        }

        if (file.isDirectory) {
            for (subFile <- file.listFiles) {
                findFileResources (buf, subFile, pred)
            }
        }
    }

    /**
     * Find jar file resources beginning from the given file matching the given predicate
     * and adding found files to the given buffer.
     * @param buf add results here
     * @param file starts from this url
     * @param pred predicate to match to
     */
    def findJarResources (buf : ListBuffer [URL],
                          url : URL,
                          pred : URL => Boolean) : Unit =
    {
        var dir = false

        url.openConnection match {
            case conn : JarURLConnection =>
                val jarRootEntry = conn.getJarEntry
                val jarFile = conn.getJarFile
                val jarRootPath = jarRootEntry.getName
                val jarRootDirPath =
                        if (jarRootPath.endsWith("/")) jarRootPath else jarRootPath + "/"

                val urlPath = url.toExternalForm
                val urlDirPath = if (urlPath.endsWith("/")) urlPath else urlPath + "/"

                for (jarEntry <- jarFile.entries) {
                    val jarEntryPath = jarEntry.getName

                    if (jarEntryPath.startsWith (jarRootDirPath)) {
                        val jarEntryRelativePath = jarEntryPath.substring (jarRootDirPath.length)
                        val entryUrl = new URL (urlDirPath + jarEntryRelativePath)

                        if (pred (entryUrl)) {
                            buf += entryUrl
                        }

                        dir = true
                    }
                }

            case conn =>
                throw new UnrecoverableError ("Unknown connection opened for url" +:+ url +:+ conn)
        }

        if (!dir && pred (url)) {
            buf += url
        }
    }
}
