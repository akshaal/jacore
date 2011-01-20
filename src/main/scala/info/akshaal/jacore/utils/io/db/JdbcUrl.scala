/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

/**
 * Abstract jdbc url. The main benefit from marking your class with this trait
 * is that jacore provides implict conversion from JdbcUrl to String. So it is possible
 * to use objects of this type where String url is required.
 */
trait JdbcUrl extends NotNull