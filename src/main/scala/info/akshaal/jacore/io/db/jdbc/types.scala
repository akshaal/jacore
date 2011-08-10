/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc


/**
 * Case objects of this trait are used to define classes of values which JDBC can possibly handle.
 * It defines a way by which JDBC converts values from Scala type to database type.
 *
 * @tparam Value scala type that JDBC Type wraps
 */
sealed trait AbstractJdbcType [Value]


/**
 * Jdbc Array type.
 */
case object JdbcArray extends AbstractJdbcType [java.sql.Array]


/**
 * Jdbc ascii stream using InputStream.
 */
case object JdbcAsciiStream extends AbstractJdbcType [java.io.InputStream]


/**
 * Jdbc BigDecimal type.
 */
case object JdbcBigDecimal extends AbstractJdbcType [java.math.BigDecimal]


/**
 * Jdbc binary stream using InputStream.
 */
case object JdbcBinaryStream extends AbstractJdbcType [java.io.InputStream]


/**
 * Jdbc Blob type.
 */
case object JdbcBlob extends AbstractJdbcType [java.sql.Blob]


/**
 * Jdbc Blob type using InputStream.
 */
case object JdbcBlobStream extends AbstractJdbcType [java.io.InputStream]


/**
 * Jdbc Boolean type.
 */
case object JdbcBoolean extends AbstractJdbcType [Boolean]


/**
 * Jdbc Byte type.
 */
case object JdbcByte extends AbstractJdbcType [Byte]


/**
 * Jdbc Byte array.
 */
case object JdbcBytes extends AbstractJdbcType [Array[Byte]]


/**
 * Jdbc character stream using Reader.
 */
case object JdbcCharacterStream extends AbstractJdbcType [java.io.Reader]


/**
 * Jdbc Clob type.
 */
case object JdbcClob extends AbstractJdbcType [java.sql.Clob]


/**
 * Jdbc Clob type using Reader.
 */
case object JdbcClobStream extends AbstractJdbcType [java.io.Reader]


/**
 * Jdbc Sql Date.
 */
case object JdbcSqlDate extends AbstractJdbcType [java.sql.Date]


/**
 * Jdbc Date.
 */
case object JdbcDate extends AbstractJdbcType [java.util.Date]


/**
 * Jdbc Double type.
 */
case object JdbcDouble extends AbstractJdbcType [Double]


/**
 * Jdbc Float type.
 */
case object JdbcFloat extends AbstractJdbcType [Float]


/**
 * Jdbc Int type.
 */
case object JdbcInt extends AbstractJdbcType [Int]


/**
 * Jdbc Float type.
 */
case object JdbcLong extends AbstractJdbcType [Long]


/**
 * Jdbc NCharacter stream using Reader.
 */
case object JdbcNCharacterStream extends AbstractJdbcType [java.io.Reader]


/**
 * Jdbc NClob type.
 */
case object JdbcNClob extends AbstractJdbcType [java.sql.NClob]


/**
 * Jdbc NClob type using Reader.
 */
case object JdbcNClobStream extends AbstractJdbcType [java.io.Reader]


/**
 * Jdbc NString type.
 */
case object JdbcNString extends AbstractJdbcType [String]


/**
 * Jdbc Object type.
 */
case object JdbcObject extends AbstractJdbcType [Object]


/**
 * Jdbc Ref type.
 */
case object JdbcRef extends AbstractJdbcType [java.sql.Ref]


/**
 * Jdbc RowId type.
 */
case object JdbcRowId extends AbstractJdbcType [java.sql.RowId]


/**
 * Jdbc short type.
 */
case object JdbcShort extends AbstractJdbcType [Short]


/**
 * Jdbc SQLXML type.
 */
case object JdbcSqlXml extends AbstractJdbcType [java.sql.SQLXML]


/**
 * Jdbc String type.
 */
case object JdbcString extends AbstractJdbcType [String]


/**
 * Jdbc Time type.
 */
case object JdbcTime extends AbstractJdbcType [java.sql.Time]


/**
 * Jdbc Timestamp type.
 */
case object JdbcTimestamp extends AbstractJdbcType [java.sql.Timestamp]


/**
 * Jdbc Url type.
 */
case object JdbcUrl extends AbstractJdbcType [java.net.URL]

