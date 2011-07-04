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
 * @tparam PAram type of value that setter is able to set
 */
private[jdbc] sealed trait JdbcSetter [Param] extends Function3 [PreparedStatement, Int, Param, Unit]

/**
 * Function object to set Array parameter on PreparedStatement.
 */
private[jdbc] object ArraySetter extends JdbcSetter [java.sql.Array] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Array) : Unit =
        ps.setArray (idx, arg)
}

/**
 * Function object to set InputStream parameter with ascii data on PreparedStatement.
 */
private[jdbc] object AsciiStreamSetter extends JdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setAsciiStream (idx, arg)
}

/**
 * Function object to set BigDecimal parameter on PreparedStatement.
 */
private[jdbc] object BigDecimalSetter extends JdbcSetter [java.math.BigDecimal] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.math.BigDecimal) : Unit =
        ps.setBigDecimal (idx, arg)
}

/**
 * Function object to set InputStream parameter with binary data on PreparedStatement.
 */
private[jdbc] object BinaryStreamSetter extends JdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setBinaryStream (idx, arg)
}

/**
 * Function object to set Blob parameter on PreparedStatement.
 */
private[jdbc] object BlobSetter extends JdbcSetter [java.sql.Blob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Blob) : Unit =
        ps.setBlob (idx, arg)
}

/**
 * Function object to set InputStream providing data for Blob parameter on PreparedStatement.
 */
private[jdbc] object BlobStreamSetter extends JdbcSetter [java.io.InputStream] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.InputStream) : Unit =
        ps.setBlob (idx, arg)
}

/**
 * Function object to set Boolean parameter on PreparedStatement.
 */
private[jdbc] object BooleanSetter extends JdbcSetter [Boolean] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Boolean) : Unit =
        ps.setBoolean (idx, arg)
}

/**
 * Function object to set byte parameter on PreparedStatement.
 */
private[jdbc] object ByteSetter extends JdbcSetter [Byte] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Byte) : Unit =
        ps.setByte (idx, arg)
}

/**
 * Function object to set byte array parameter on PreparedStatement.
 */
private[jdbc] object BytesSetter extends JdbcSetter [Array[Byte]] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Array[Byte]) : Unit =
        ps.setBytes (idx, arg)
}

/**
 * Function object to set Reader parameter with character data on PreparedStatement.
 */
private[jdbc] object CharacterStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setCharacterStream (idx, arg)
}

/**
 * Function object to set Clob parameter on PreparedStatement.
 */
private[jdbc] object ClobSetter extends JdbcSetter [java.sql.Clob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Clob) : Unit =
        ps.setClob (idx, arg)
}

/**
 * Function object to set Reader providing data for Clob parameter on PreparedStatement.
 */
private[jdbc] object ClobStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setClob (idx, arg)
}

/**
 * Function object to set Date parameter on PreparedStatement.
 */
private[jdbc] object SqlDateSetter extends JdbcSetter [java.sql.Date] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Date) : Unit =
        ps.setDate (idx, arg)
}

/**
 * Function object to set Date parameter on PreparedStatement.
 */
private[jdbc] object DateSetter extends JdbcSetter [java.util.Date] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.util.Date) : Unit =
        ps.setDate (idx, new java.sql.Date (arg.getTime))
}

/**
 * Function object to set Double parameter on PreparedStatement.
 */
private[jdbc] object DoubleSetter extends JdbcSetter [Double] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Double) : Unit =
        ps.setDouble (idx, arg)
}

/**
 * Function object to set Float parameter on PreparedStatement.
 */
private[jdbc] object FloatSetter extends JdbcSetter [Float] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Float) : Unit =
        ps.setFloat (idx, arg)
}

/**
 * Function object to set Int parameter on PreparedStatement.
 */
private[jdbc] object IntSetter extends JdbcSetter [Int] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Int) : Unit =
        ps.setInt (idx, arg)
}

/**
 * Function object to set Long parameter on PreparedStatement.
 */
private[jdbc] object LongSetter extends JdbcSetter [Long] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Long) : Unit =
        ps.setLong (idx, arg)
}

/**
 * Function object to set Reader parameter with ncharacter data on PreparedStatement.
 */
private[jdbc] object NCharacterStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setNCharacterStream (idx, arg)
}

/**
 * Function object to set NClob parameter on PreparedStatement.
 */
private[jdbc] object NClobSetter extends JdbcSetter [java.sql.NClob] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.NClob) : Unit =
        ps.setNClob (idx, arg)
}

/**
 * Function object to set Reader providing data for NClob parameter on PreparedStatement.
 */
private[jdbc] object NClobStreamSetter extends JdbcSetter [java.io.Reader] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.io.Reader) : Unit =
        ps.setNClob (idx, arg)
}

/**
 * Function object to set NString parameter on PreparedStatement.
 */
private[jdbc] object NStringSetter extends JdbcSetter [String] {
    override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
        ps.setNString (idx, arg)
}

/**
 * Function object to set Object parameter on PreparedStatement.
 */
private[jdbc] object ObjectSetter extends JdbcSetter [Object] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Object) : Unit =
        ps.setObject (idx, arg)
}

/**
 * Function object to set Ref parameter on PreparedStatement.
 */
private[jdbc] object RefSetter extends JdbcSetter [java.sql.Ref] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Ref) : Unit =
        ps.setRef (idx, arg)
}

/**
 * Function object to set RowId parameter on PreparedStatement.
 */
private[jdbc] object RowIdSetter extends JdbcSetter [java.sql.RowId] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.RowId) : Unit =
        ps.setRowId (idx, arg)
}

/**
 * Function object to set Short parameter on PreparedStatement.
 */
private[jdbc] object ShortSetter extends JdbcSetter [Short] {
    override def apply (ps : PreparedStatement, idx : Int, arg : Short) : Unit =
        ps.setShort (idx, arg)
}

/**
 * Function object to set SQLXML parameter on PreparedStatement.
 */
private[jdbc] object SqlXmlSetter extends JdbcSetter [java.sql.SQLXML] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.SQLXML) : Unit =
        ps.setSQLXML (idx, arg)
}

/**
 * Function object to set String parameter on PreparedStatement.
 */
private[jdbc] object StringSetter extends JdbcSetter [String] {
    override def apply (ps : PreparedStatement, idx : Int, arg : String) : Unit =
        ps.setString (idx, arg)
}

/**
 * Function object to set Time parameter on PreparedStatement.
 */
private[jdbc] object TimeSetter extends JdbcSetter [java.sql.Time] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Time) : Unit =
        ps.setTime (idx, arg)
}

/**
 * Function object to set Timestamp parameter on PreparedStatement.
 */
private[jdbc] object TimestampSetter extends JdbcSetter [java.sql.Timestamp] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.sql.Timestamp) : Unit =
        ps.setTimestamp (idx, arg)
}

/**
 * Function object to set Url parameter on PreparedStatement.
 */
private[jdbc] object UrlSetter extends JdbcSetter [java.net.URL] {
    override def apply (ps : PreparedStatement, idx : Int, arg : java.net.URL) : Unit =
        ps.setURL (idx, arg)
}
