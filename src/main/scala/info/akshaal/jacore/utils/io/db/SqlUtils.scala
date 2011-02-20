/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils.io.db

/**
 * Convenient methods for working with SQL statements.
 */
object SqlUtils {
    /**
     * Count number of placeholders in the given SQL. Placeholder is represnted as ? symbol.
     * Any quoted ? symbol is ignored and not counted.
     *
     * @param statement statement to search for placeholders
     * @return number of placeholders
     */
    def countPlaceholders (statement : String) : Int = {
        var inQuote = false
        var quoteChar = '_'
        var placeholders = 0
        var escape = false

        for (c <- statement) {
            if (escape) {
                // Current char is escaped
                escape = false
            } else {
                // Current char is not escaped

                if (inQuote) {
                    // Inside quoted string
                    c match {
                        case '\'' | '\"' if quoteChar == c  =>  inQuote = false
                        case '\\'                           =>  escape = true
                        case _                              =>
                    }
                } else {
                    // Outside quoted string
                    c match {
                        case '\'' | '\"'  =>  quoteChar = c; inQuote = true
                        case '?'          =>  placeholders += 1
                        case _            =>
                    }
                }
            }
        }

        placeholders
    }
}
