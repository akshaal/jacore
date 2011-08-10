/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc
package `type`

import java.sql.PreparedStatement


/**
 * Package with JDBC setters that are responsible for setting given parameter value of
 * corresponding type on the given prepared statement in the given position.
 */
package object setter {
    /**
     * Type for functions that are responisble for setting provided value on the given
     * PreparedStatement in the given parameter position.
     *
     * @tparam Value type of value that setter is able to set
     */
    type JdbcSetter [Value] = Function3 [PreparedStatement, Int, Value, Unit]

    /**
     * Returns setter for the given JDBC type.
     *
     * @tparam Value type of Scala value that JDBC type represents
     * @param jdbcType object that represents JDBC type
     * @return instance of setter that is able to set value given type on JDBC Prepared Statement 
     */
    def getSetterForJdbcType [Value] (jdbcType : AbstractJdbcType [Value]) : JdbcSetter [Value] =
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
