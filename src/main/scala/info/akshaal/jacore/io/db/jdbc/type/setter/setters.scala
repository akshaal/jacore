/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package `type`
package setter

import java.sql.PreparedStatement


/**
 * Setter trait for objects that are responisble for setting provided value on the given
 * PreparedStatement in the given parameter position.
 *
 * @tparam Value type of value that setter is able to set
 */
sealed trait AbstractJdbcSetter [Value] extends Function3 [PreparedStatement, Int, Value, Unit]

/**
 * Compainion object for {AbstractJdbcSetter} trait.
 */
object AbstractJdbcSetter {
    /**
     * Returns setter for the given JDBC type.
     *
     * @tparam Value type of Scala value that JDBC type represents
     * @param jdbcType object that represents JDBC type
     * @return instance of setter that is able to set value given type on JDBC Prepared Statement 
     */
    def getFor [Value] (jdbcType : AbstractJdbcType [Value]) : AbstractJdbcSetter [Value] =
        jdbcType match {
            case JdbcArray                  => ArraySetter
            case JdbcAsciiStream            => AsciiStreamSetter
            case JdbcBigDecimal             => BigDecimalSetter
            case JdbcBinaryStream           => BinaryStreamSetter
            case JdbcBlob                   => BlobSetter
            case JdbcBlobStream             => BlobStreamSetter
            case JdbcBoolean                => BooleanSetter
            case JdbcByte                   => ByteSetter
            case JdbcBytes                  => BytesSetter
            case JdbcCharacterStream        => CharacterStreamSetter
            case JdbcClob                   => ClobSetter
            case JdbcClobStream             => ClobStreamSetter
            case JdbcDate                   => DateSetter
            case JdbcDouble                 => DoubleSetter
            case JdbcFloat                  => FloatSetter
            case JdbcInt                    => IntSetter
            case JdbcLong                   => LongSetter
            case JdbcNCharacterStream       => NCharacterStreamSetter
            case JdbcNClob                  => NClobSetter
            case JdbcNClobStream            => NClobStreamSetter
            case JdbcNString                => NStringSetter
            case JdbcObject                 => ObjectSetter
            case JdbcRef                    => RefSetter
            case JdbcRowId                  => RowIdSetter
            case JdbcShort                  => ShortSetter
            case JdbcSqlDate                => SqlDateSetter
            case JdbcSqlXml                 => SqlXmlSetter
            case JdbcString                 => StringSetter
            case JdbcTime                   => TimeSetter
            case JdbcTimestamp              => TimestampSetter
            case JdbcUrl                    => UrlSetter
        }
}

/**
 * Function object to set Array parameter on PreparedStatement.
 */
case object ArraySetter extends AbstractJdbcSetter [java.sql.Array] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Array) : Unit =
        ps.setArray (idx, arg)
}

/**
 * Function object to set InputStream parameter with ascii data on PreparedStatement.
 */
case object AsciiStreamSetter extends AbstractJdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setAsciiStream (idx, arg)
}

/**
 * Function object to set BigDecimal parameter on PreparedStatement.
 */
case object BigDecimalSetter extends AbstractJdbcSetter [java.math.BigDecimal] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.math.BigDecimal) : Unit =
        ps.setBigDecimal (idx, arg)
}

/**
 * Function object to set InputStream parameter with binary data on PreparedStatement.
 */
case object BinaryStreamSetter extends AbstractJdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setBinaryStream (idx, arg)
}

/**
 * Function object to set Blob parameter on PreparedStatement.
 */
case object BlobSetter extends AbstractJdbcSetter [java.sql.Blob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Blob) : Unit =
        ps.setBlob (idx, arg)
}

/**
 * Function object to set InputStream providing data for Blob parameter on PreparedStatement.
 */
case object BlobStreamSetter extends AbstractJdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setBlob (idx, arg)
}

/**
 * Function object to set Boolean parameter on PreparedStatement.
 */
case object BooleanSetter extends AbstractJdbcSetter [Boolean] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Boolean) : Unit =
        ps.setBoolean (idx, arg)
}

/**
 * Function object to set byte parameter on PreparedStatement.
 */
case object ByteSetter extends AbstractJdbcSetter [Byte] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Byte) : Unit =
        ps.setByte (idx, arg)
}

/**
 * Function object to set byte array parameter on PreparedStatement.
 */
case object BytesSetter extends AbstractJdbcSetter [Array[Byte]] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Array[Byte]) : Unit =
        ps.setBytes (idx, arg)
}

/**
 * Function object to set Reader parameter with character data on PreparedStatement.
 */
case object CharacterStreamSetter extends AbstractJdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setCharacterStream (idx, arg)
}

/**
 * Function object to set Clob parameter on PreparedStatement.
 */
case object ClobSetter extends AbstractJdbcSetter [java.sql.Clob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Clob) : Unit =
        ps.setClob (idx, arg)
}

/**
 * Function object to set Reader providing data for Clob parameter on PreparedStatement.
 */
case object ClobStreamSetter extends AbstractJdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setClob (idx, arg)
}

/**
 * Function object to set Date parameter on PreparedStatement.
 */
case object SqlDateSetter extends AbstractJdbcSetter [java.sql.Date] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Date) : Unit =
        ps.setDate (idx, arg)
}

/**
 * Function object to set Date parameter on PreparedStatement.
 */
case object DateSetter extends AbstractJdbcSetter [java.util.Date] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.util.Date) : Unit =
        ps.setDate (idx, new java.sql.Date (arg.getTime))
}

/**
 * Function object to set Double parameter on PreparedStatement.
 */
case object DoubleSetter extends AbstractJdbcSetter [Double] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Double) : Unit =
        ps.setDouble (idx, arg)
}

/**
 * Function object to set Float parameter on PreparedStatement.
 */
case object FloatSetter extends AbstractJdbcSetter [Float] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Float) : Unit =
        ps.setFloat (idx, arg)
}

/**
 * Function object to set Int parameter on PreparedStatement.
 */
case object IntSetter extends AbstractJdbcSetter [Int] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Int) : Unit =
        ps.setInt (idx, arg)
}

/**
 * Function object to set Long parameter on PreparedStatement.
 */
case object LongSetter extends AbstractJdbcSetter [Long] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Long) : Unit =
        ps.setLong (idx, arg)
}

/**
 * Function object to set Reader parameter with ncharacter data on PreparedStatement.
 */
case object NCharacterStreamSetter extends AbstractJdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setNCharacterStream (idx, arg)
}

/**
 * Function object to set NClob parameter on PreparedStatement.
 */
case object NClobSetter extends AbstractJdbcSetter [java.sql.NClob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.NClob) : Unit =
        ps.setNClob (idx, arg)
}

/**
 * Function object to set Reader providing data for NClob parameter on PreparedStatement.
 */
case object NClobStreamSetter extends AbstractJdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setNClob (idx, arg)
}

/**
 * Function object to set NString parameter on PreparedStatement.
 */
case object NStringSetter extends AbstractJdbcSetter [String] {
    override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
        ps.setNString (idx, arg)
}

/**
 * Function object to set Object parameter on PreparedStatement.
 */
case object ObjectSetter extends AbstractJdbcSetter [Object] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Object) : Unit =
        ps.setObject (idx, arg)
}

/**
 * Function object to set Ref parameter on PreparedStatement.
 */
case object RefSetter extends AbstractJdbcSetter [java.sql.Ref] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Ref) : Unit =
        ps.setRef (idx, arg)
}

/**
 * Function object to set RowId parameter on PreparedStatement.
 */
case object RowIdSetter extends AbstractJdbcSetter [java.sql.RowId] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.RowId) : Unit =
        ps.setRowId (idx, arg)
}

/**
 * Function object to set Short parameter on PreparedStatement.
 */
case object ShortSetter extends AbstractJdbcSetter [Short] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Short) : Unit =
        ps.setShort (idx, arg)
}

/**
 * Function object to set SQLXML parameter on PreparedStatement.
 */
case object SqlXmlSetter extends AbstractJdbcSetter [java.sql.SQLXML] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.SQLXML) : Unit =
        ps.setSQLXML (idx, arg)
}

/**
 * Function object to set String parameter on PreparedStatement.
 */
case object StringSetter extends AbstractJdbcSetter [String] {
    override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
        ps.setString (idx, arg)
}

/**
 * Function object to set Time parameter on PreparedStatement.
 */
case object TimeSetter extends AbstractJdbcSetter [java.sql.Time] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Time) : Unit =
        ps.setTime (idx, arg)
}

/**
 * Function object to set Timestamp parameter on PreparedStatement.
 */
case object TimestampSetter extends AbstractJdbcSetter [java.sql.Timestamp] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Timestamp) : Unit =
        ps.setTimestamp (idx, arg)
}

/**
 * Function object to set Url parameter on PreparedStatement.
 */
case object UrlSetter extends AbstractJdbcSetter [java.net.URL] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.net.URL) : Unit =
        ps.setURL (idx, arg)
}
