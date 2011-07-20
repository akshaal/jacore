/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package `type`

/**
 * Objects of this trait are used to define classes of values which JDBC can possibly handle.
 *
 * @tparam Value scala type that JDBC Type wraps
 */
sealed trait JdbcType [Value]

/**
 * Jdbc Array type.
 */
case object JdbcArray extends JdbcType [java.sql.Array]

/**
 * Jdbc ascii stream using InputStream.
 */
case object JdbcAsciiStream extends JdbcType [java.io.InputStream]

/**
 * Jdbc BigDecimal type.
 */
case object JdbcBigDecimal extends JdbcType [java.math.BigDecimal]

/**
 * Jdbc binary stream using InputStream.
 */
case object JdbcBinaryStream extends JdbcType [java.io.InputStream]

/**
 * Jdbc Blob type.
 */
case object JdbcBlob extends JdbcType [java.sql.Blob]

/**
 * Jdbc Blob type using InputStream.
 */
case object JdbcBlobStream extends JdbcType [java.io.InputStream]

/**
 * Jdbc Boolean type.
 */
case object JdbcBoolean extends JdbcType [Boolean]

/**
 * Jdbc Byte type.
 */
case object JdbcByte extends JdbcType [Byte]

/**
 * Jdbc Byte array.
 */
case object JdbcBytes extends JdbcType [Array[Byte]]

/**
 * Jdbc character stream using Reader.
 */
case object JdbcCharacterStream extends JdbcType [java.io.Reader]

/**
 * Jdbc Clob type.
 */
case object JdbcClob extends JdbcType [java.sql.Clob]

/**
 * Jdbc Clob type using Reader.
 */
case object JdbcClobStream extends JdbcType [java.io.Reader]

/**
 * Jdbc Sql Date.
 */
case object JdbcSqlDate extends JdbcType [java.sql.Date]

/**
 * Jdbc Date.
 */
case object JdbcDate extends JdbcType [java.util.Date]

/**
 * Jdbc Double type.
 */
case object JdbcDouble extends JdbcType [Double]

/**
 * Jdbc Float type.
 */
case object JdbcFloat extends JdbcType [Float]

/**
 * Jdbc Int type.
 */
case object JdbcInt extends JdbcType [Int]

/**
 * Jdbc Float type.
 */
case object JdbcLong extends JdbcType [Long]

/**
 * Jdbc NCharacter stream using Reader.
 */
case object JdbcNCharacterStream extends JdbcType [java.io.Reader]

/**
 * Jdbc NClob type.
 */
case object JdbcNClob extends JdbcType [java.sql.NClob]

/**
 * Jdbc NClob type using Reader.
 */
case object JdbcNClobStream extends JdbcType [java.io.Reader]

/**
 * Jdbc NString type.
 */
case object JdbcNString extends JdbcType [String]

/**
 * Jdbc Object type.
 */
case object JdbcObject extends JdbcType [Object]

/**
 * Jdbc Ref type.
 */
case object JdbcRef extends JdbcType [java.sql.Ref]

/**
 * Jdbc RowId type.
 */
case object JdbcRowId extends JdbcType [java.sql.RowId]

/**
 * Jdbc short type.
 */
case object JdbcShort extends JdbcType [Short]

/**
 * Jdbc SQLXML type.
 */
case object JdbcSqlXml extends JdbcType [java.sql.SQLXML]

/**
 * Jdbc String type.
 */
case object JdbcString extends JdbcType [String]

/**
 * Jdbc Time type.
 */
case object JdbcTime extends JdbcType [java.sql.Time]

/**
 * Jdbc Timestamp type.
 */
case object JdbcTimestamp extends JdbcType [java.sql.Timestamp]

/**
 * Jdbc Url type.
 */
case object JdbcUrl extends JdbcType [java.net.URL]
