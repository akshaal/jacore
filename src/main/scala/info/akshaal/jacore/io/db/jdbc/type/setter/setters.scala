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
 * @tparam Param type of value that setter is able to set
 */
sealed trait JdbcSetter [Param] extends Function3 [PreparedStatement, Int, Param, Unit]

/**
 * Compainion object for {JdbcSetter} trait.
 */
object JdbcSetter {
    /**
     * Returns setter for the given jdbc type.
     *
     * @tparam Param type of Scala value that jdbc type represents
     * @param jdbcType object that represents jdbc type
     * @return instance of setter that is able to set value given type on JDBC Prepared Statement 
     */
    def getFor [Param] (jdbcType : JdbcType [Param]) : JdbcSetter [Param] =
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
object ArraySetter extends JdbcSetter [java.sql.Array] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Array) : Unit =
        ps.setArray (idx, arg)
}

/**
 * Function object to set InputStream parameter with ascii data on PreparedStatement.
 */
object AsciiStreamSetter extends JdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setAsciiStream (idx, arg)
}

/**
 * Function object to set BigDecimal parameter on PreparedStatement.
 */
object BigDecimalSetter extends JdbcSetter [java.math.BigDecimal] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.math.BigDecimal) : Unit =
        ps.setBigDecimal (idx, arg)
}

/**
 * Function object to set InputStream parameter with binary data on PreparedStatement.
 */
object BinaryStreamSetter extends JdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setBinaryStream (idx, arg)
}

/**
 * Function object to set Blob parameter on PreparedStatement.
 */
object BlobSetter extends JdbcSetter [java.sql.Blob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Blob) : Unit =
        ps.setBlob (idx, arg)
}

/**
 * Function object to set InputStream providing data for Blob parameter on PreparedStatement.
 */
object BlobStreamSetter extends JdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setBlob (idx, arg)
}

/**
 * Function object to set Boolean parameter on PreparedStatement.
 */
object BooleanSetter extends JdbcSetter [Boolean] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Boolean) : Unit =
        ps.setBoolean (idx, arg)
}

/**
 * Function object to set byte parameter on PreparedStatement.
 */
object ByteSetter extends JdbcSetter [Byte] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Byte) : Unit =
        ps.setByte (idx, arg)
}

/**
 * Function object to set byte array parameter on PreparedStatement.
 */
object BytesSetter extends JdbcSetter [Array[Byte]] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Array[Byte]) : Unit =
        ps.setBytes (idx, arg)
}

/**
 * Function object to set Reader parameter with character data on PreparedStatement.
 */
object CharacterStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setCharacterStream (idx, arg)
}

/**
 * Function object to set Clob parameter on PreparedStatement.
 */
object ClobSetter extends JdbcSetter [java.sql.Clob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Clob) : Unit =
        ps.setClob (idx, arg)
}

/**
 * Function object to set Reader providing data for Clob parameter on PreparedStatement.
 */
object ClobStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setClob (idx, arg)
}

/**
 * Function object to set Date parameter on PreparedStatement.
 */
object SqlDateSetter extends JdbcSetter [java.sql.Date] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Date) : Unit =
        ps.setDate (idx, arg)
}

/**
 * Function object to set Date parameter on PreparedStatement.
 */
object DateSetter extends JdbcSetter [java.util.Date] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.util.Date) : Unit =
        ps.setDate (idx, new java.sql.Date (arg.getTime))
}

/**
 * Function object to set Double parameter on PreparedStatement.
 */
object DoubleSetter extends JdbcSetter [Double] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Double) : Unit =
        ps.setDouble (idx, arg)
}

/**
 * Function object to set Float parameter on PreparedStatement.
 */
object FloatSetter extends JdbcSetter [Float] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Float) : Unit =
        ps.setFloat (idx, arg)
}

/**
 * Function object to set Int parameter on PreparedStatement.
 */
object IntSetter extends JdbcSetter [Int] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Int) : Unit =
        ps.setInt (idx, arg)
}

/**
 * Function object to set Long parameter on PreparedStatement.
 */
object LongSetter extends JdbcSetter [Long] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Long) : Unit =
        ps.setLong (idx, arg)
}

/**
 * Function object to set Reader parameter with ncharacter data on PreparedStatement.
 */
object NCharacterStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setNCharacterStream (idx, arg)
}

/**
 * Function object to set NClob parameter on PreparedStatement.
 */
object NClobSetter extends JdbcSetter [java.sql.NClob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.NClob) : Unit =
        ps.setNClob (idx, arg)
}

/**
 * Function object to set Reader providing data for NClob parameter on PreparedStatement.
 */
object NClobStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setNClob (idx, arg)
}

/**
 * Function object to set NString parameter on PreparedStatement.
 */
object NStringSetter extends JdbcSetter [String] {
    override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
        ps.setNString (idx, arg)
}

/**
 * Function object to set Object parameter on PreparedStatement.
 */
object ObjectSetter extends JdbcSetter [Object] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Object) : Unit =
        ps.setObject (idx, arg)
}

/**
 * Function object to set Ref parameter on PreparedStatement.
 */
object RefSetter extends JdbcSetter [java.sql.Ref] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Ref) : Unit =
        ps.setRef (idx, arg)
}

/**
 * Function object to set RowId parameter on PreparedStatement.
 */
object RowIdSetter extends JdbcSetter [java.sql.RowId] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.RowId) : Unit =
        ps.setRowId (idx, arg)
}

/**
 * Function object to set Short parameter on PreparedStatement.
 */
object ShortSetter extends JdbcSetter [Short] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Short) : Unit =
        ps.setShort (idx, arg)
}

/**
 * Function object to set SQLXML parameter on PreparedStatement.
 */
object SqlXmlSetter extends JdbcSetter [java.sql.SQLXML] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.SQLXML) : Unit =
        ps.setSQLXML (idx, arg)
}

/**
 * Function object to set String parameter on PreparedStatement.
 */
object StringSetter extends JdbcSetter [String] {
    override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
        ps.setString (idx, arg)
}

/**
 * Function object to set Time parameter on PreparedStatement.
 */
object TimeSetter extends JdbcSetter [java.sql.Time] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Time) : Unit =
        ps.setTime (idx, arg)
}

/**
 * Function object to set Timestamp parameter on PreparedStatement.
 */
object TimestampSetter extends JdbcSetter [java.sql.Timestamp] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Timestamp) : Unit =
        ps.setTimestamp (idx, arg)
}

/**
 * Function object to set Url parameter on PreparedStatement.
 */
object UrlSetter extends JdbcSetter [java.net.URL] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.net.URL) : Unit =
        ps.setURL (idx, arg)
}
