package info.akshaal.jacore.system.domain

import java.util.Date
import scala.reflect.BeanProperty

private[system] sealed case class LogRecord (
                             @BeanProperty val time       : Date,
                             @BeanProperty val nano       : Long,
                             @BeanProperty val levelId    : Int,
                             @BeanProperty val category   : String,
                             @BeanProperty val msg        : String,
                             @BeanProperty val thread     : String,
                             @BeanProperty val throwable  : String)
                    extends NotNull
    
private[system] object LogRecord {
    val debugId = 0
    val infoId  = 1
    val warnId  = 2
    val errorId = 3
}

