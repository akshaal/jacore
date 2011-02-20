/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbctype

/**
 * A type that JDBC can possibly handle.
 *
 * @param T actual type
 */
sealed trait JdbcType [T]

/**
 * Jdbc Array type.
 */
object JdbcArray extends JdbcType [java.sql.Array]

/**
 * Jdbc ascii stream using InputStream.
 */
object JdbcAsciiStream extends JdbcType [java.io.InputStream]

/**
 * Jdbc BigDecimal type.
 */
object JdbcBigDecimal extends JdbcType [java.math.BigDecimal]

/**
 * Jdbc binary stream using InputStream.
 */
object JdbcBinaryStream extends JdbcType [java.io.InputStream]

/**
 * Jdbc Blob type.
 */
object JdbcBlob extends JdbcType [java.sql.Blob]

/**
 * Jdbc Blob type using InputStream.
 */
object JdbcBlobStream extends JdbcType [java.io.InputStream]

/**
 * Jdbc Boolean type.
 */
object JdbcBoolean extends JdbcType [Boolean]

/**
 * Jdbc Byte type.
 */
object JdbcByte extends JdbcType [Byte]

/**
 * Jdbc Byte array.
 */
object JdbcBytes extends JdbcType [Array[Byte]]

/**
 * Jdbc character stream using Reader.
 */
object JdbcCharacterStream extends JdbcType [java.io.Reader]

/**
 * Jdbc Clob type.
 */
object JdbcClob extends JdbcType [java.sql.Clob]

/**
 * Jdbc Clob type using Reader.
 */
object JdbcClobStream extends JdbcType [java.io.Reader]

/**
 * Jdbc Sql Date.
 */
object JdbcSqlDate extends JdbcType [java.sql.Date]

/**
 * Jdbc Date.
 */
object JdbcDate extends JdbcType [java.util.Date]

/**
 * Jdbc Double type.
 */
object JdbcDouble extends JdbcType [Double]

/**
 * Jdbc Float type.
 */
object JdbcFloat extends JdbcType [Float]

/**
 * Jdbc Int type.
 */
object JdbcInt extends JdbcType [Int]

/**
 * Jdbc Float type.
 */
object JdbcLong extends JdbcType [Long]

/**
 * Jdbc NCharacter stream using Reader.
 */
object JdbcNCharacterStream extends JdbcType [java.io.Reader]

/**
 * Jdbc NClob type.
 */
object JdbcNClob extends JdbcType [java.sql.NClob]

/**
 * Jdbc NClob type using Reader.
 */
object JdbcNClobStream extends JdbcType [java.io.Reader]

/**
 * Jdbc NString type.
 */
object JdbcNString extends JdbcType [String]

/**
 * Jdbc Object type.
 */
object JdbcObject extends JdbcType [Object]

/**
 * Jdbc Ref type.
 */
object JdbcRef extends JdbcType [java.sql.Ref]

/**
 * Jdbc RowId type.
 */
object JdbcRowId extends JdbcType [java.sql.RowId]

/**
 * Jdbc short type.
 */
object JdbcShort extends JdbcType [Short]

/**
 * Jdbc SQLXML type.
 */
object JdbcSqlXml extends JdbcType [java.sql.SQLXML]

/**
 * Jdbc String type.
 */
object JdbcString extends JdbcType [String]

/**
 * Jdbc Time type.
 */
object JdbcTime extends JdbcType [java.sql.Time]

/**
 * Jdbc Timestamp type.
 */
object JdbcTimestamp extends JdbcType [java.sql.Timestamp]

/**
 * Jdbc Url type.
 */
object JdbcUrl extends JdbcType [java.net.URL]