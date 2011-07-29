/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit.io.db.jdbc

import java.util.Random
import java.sql.PreparedStatement
import org.specs.mock.Mockito

import io.db.jdbc.`type`._
import io.db.jdbc.`type`.setter._

class JdbcSettersTest extends JacoreSpecWithJUnit ("JdbcSetter specification")
                         with Mockito
{
    val rnd = new Random
    import rnd._

    def nextIdx = nextInt.abs % 10

    "AbstractJdbcSetter" should {
        "provide proper setter for JdbcArray type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Array]
            val setter = AbstractJdbcSetter.getFor (JdbcArray)

            setter  must_==  ArraySetter

            setter (ps, idx, value)

            there was one (ps).setArray (idx, value)
        }

        "provide proper setter for JdbcAsciiStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.InputStream]
            val setter = AbstractJdbcSetter.getFor (JdbcAsciiStream)

            setter  must_==  AsciiStreamSetter

            setter (ps, idx, value)

            there was one (ps).setAsciiStream (idx, value)
        }

        "provide proper setter for JdbcBigDecimal type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.math.BigDecimal]
            val setter = AbstractJdbcSetter.getFor (JdbcBigDecimal)

            setter  must_==  BigDecimalSetter

            setter (ps, idx, value)

            there was one (ps).setBigDecimal (idx, value)
        }

        "provide proper setter for JdbcBinaryStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.InputStream]
            val setter = AbstractJdbcSetter.getFor (JdbcBinaryStream)

            setter  must_==  BinaryStreamSetter

            setter (ps, idx, value)

            there was one (ps).setBinaryStream (idx, value)
        }

        "provide proper setter for JdbcBlob type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Blob]
            val setter = AbstractJdbcSetter.getFor (JdbcBlob)

            setter  must_==  BlobSetter

            setter (ps, idx, value)

            there was one (ps).setBlob (idx, value)
        }

        "provide proper setter for JdbcBlobStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.InputStream]
            val setter = AbstractJdbcSetter.getFor (JdbcBlobStream)

            setter  must_==  BlobStreamSetter

            setter (ps, idx, value)

            there was one (ps).setBlob (idx, value)
        }

        "provide proper setter for JdbcBoolean type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextBoolean
            val setter = AbstractJdbcSetter.getFor (JdbcBoolean)

            setter  must_==  BooleanSetter

            setter (ps, idx, value)

            there was one (ps).setBoolean (idx, value)
        }

        "provide proper setter for JdbcByte type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextInt.asInstanceOf [Byte]
            val setter = AbstractJdbcSetter.getFor (JdbcByte)

            setter  must_==  ByteSetter

            setter (ps, idx, value)

            there was one (ps).setByte (idx, value)
        }

        "provide proper setter for JdbcBytes type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = new Array [Byte] (10)
            nextBytes (value)
            
            val setter = AbstractJdbcSetter.getFor (JdbcBytes)

            setter  must_==  BytesSetter

            setter (ps, idx, value)

            there was one (ps).setBytes (idx, value)
        }

        "provide proper setter for JdbcCharacterStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.Reader]
            val setter = AbstractJdbcSetter.getFor (JdbcCharacterStream)

            setter  must_==  CharacterStreamSetter

            setter (ps, idx, value)

            there was one (ps).setCharacterStream (idx, value)
        }

        "provide proper setter for JdbcClob type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Clob]
            val setter = AbstractJdbcSetter.getFor (JdbcClob)

            setter  must_==  ClobSetter

            setter (ps, idx, value)

            there was one (ps).setClob (idx, value)
        }

        "provide proper setter for JdbcClobStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.Reader]
            val setter = AbstractJdbcSetter.getFor (JdbcClobStream)

            setter  must_==  ClobStreamSetter

            setter (ps, idx, value)

            there was one (ps).setClob (idx, value)
        }

        "provide proper setter for JdbcDate type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = new java.util.Date
            val setter = AbstractJdbcSetter.getFor (JdbcDate)

            setter  must_==  DateSetter

            setter (ps, idx, value)

            there was one (ps).setDate (idx, new java.sql.Date (value.getTime))
        }

        "provide proper setter for JdbcDouble type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextDouble
            val setter = AbstractJdbcSetter.getFor (JdbcDouble)

            setter  must_==  DoubleSetter

            setter (ps, idx, value)

            there was one (ps).setDouble (idx, value)
        }

        "provide proper setter for JdbcFloat type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextFloat
            val setter = AbstractJdbcSetter.getFor (JdbcFloat)

            setter  must_==  FloatSetter

            setter (ps, idx, value)

            there was one (ps).setFloat (idx, value)
        }

        "provide proper setter for JdbcInt type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextInt
            val setter = AbstractJdbcSetter.getFor (JdbcInt)

            setter  must_==  IntSetter

            setter (ps, idx, value)

            there was one (ps).setInt (idx, value)
        }

        "provide proper setter for JdbcLong type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextLong
            val setter = AbstractJdbcSetter.getFor (JdbcLong)

            setter  must_==  LongSetter

            setter (ps, idx, value)

            there was one (ps).setLong (idx, value)
        }

        "provide proper setter for JdbcNCharacterStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.Reader]
            val setter = AbstractJdbcSetter.getFor (JdbcNCharacterStream)

            setter  must_==  NCharacterStreamSetter

            setter (ps, idx, value)

            there was one (ps).setNCharacterStream (idx, value)
        }

        "provide proper setter for JdbcNClob type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.NClob]
            val setter = AbstractJdbcSetter.getFor (JdbcNClob)

            setter  must_==  NClobSetter

            setter (ps, idx, value)

            there was one (ps).setNClob (idx, value)
        }

        "provide proper setter for JdbcNClobStream type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.io.Reader]
            val setter = AbstractJdbcSetter.getFor (JdbcNClobStream)

            setter  must_==  NClobStreamSetter

            setter (ps, idx, value)

            there was one (ps).setNClob (idx, value)
        }

        "provide proper setter for JdbcNString type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextInt.toString
            val setter = AbstractJdbcSetter.getFor (JdbcNString)

            setter  must_==  NStringSetter

            setter (ps, idx, value)

            there was one (ps).setNString (idx, value)
        }

        "provide proper setter for JdbcObject type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [Object]
            val setter = AbstractJdbcSetter.getFor (JdbcObject)

            setter  must_==  ObjectSetter

            setter (ps, idx, value)

            there was one (ps).setObject (idx, value)
        }

        "provide proper setter for JdbcRef type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Ref]
            val setter = AbstractJdbcSetter.getFor (JdbcRef)

            setter  must_==  RefSetter

            setter (ps, idx, value)

            there was one (ps).setRef (idx, value)
        }

        "provide proper setter for JdbcRowId type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.RowId]
            val setter = AbstractJdbcSetter.getFor (JdbcRowId)

            setter  must_==  RowIdSetter

            setter (ps, idx, value)

            there was one (ps).setRowId (idx, value)
        }

        "provide proper setter for JdbcShort type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextInt.asInstanceOf [Short]
            val setter = AbstractJdbcSetter.getFor (JdbcShort)

            setter  must_==  ShortSetter

            setter (ps, idx, value)

            there was one (ps).setShort (idx, value)
        }

        "provide proper setter for JdbcSqlDate type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Date]
            val setter = AbstractJdbcSetter.getFor (JdbcSqlDate)

            setter  must_==  SqlDateSetter

            setter (ps, idx, value)

            there was one (ps).setDate (idx, value)
        }

        "provide proper setter for JdbcSqlXml type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.SQLXML]
            val setter = AbstractJdbcSetter.getFor (JdbcSqlXml)

            setter  must_==  SqlXmlSetter

            setter (ps, idx, value)

            there was one (ps).setSQLXML (idx, value)
        }

        "provide proper setter for JdbcString type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = nextLong.toString
            val setter = AbstractJdbcSetter.getFor (JdbcString)

            setter  must_==  StringSetter

            setter (ps, idx, value)

            there was one (ps).setString (idx, value)
        }

        "provide proper setter for JdbcTime type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Time]
            val setter = AbstractJdbcSetter.getFor (JdbcTime)

            setter  must_==  TimeSetter

            setter (ps, idx, value)

            there was one (ps).setTime (idx, value)
        }

        "provide proper setter for JdbcTimestamp type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = mock [java.sql.Timestamp]
            val setter = AbstractJdbcSetter.getFor (JdbcTimestamp)

            setter  must_==  TimestampSetter

            setter (ps, idx, value)

            there was one (ps).setTimestamp (idx, value)
        }

        "provide proper setter for JdbcUrl type" in {
            val idx = nextIdx
            val ps = mock [PreparedStatement]
            val value = new java.net.URL ("http://x" + nextInt.toString)
            val setter = AbstractJdbcSetter.getFor (JdbcUrl)

            setter  must_==  UrlSetter

            setter (ps, idx, value)

            there was one (ps).setURL (idx, value)
        }
    }
}
