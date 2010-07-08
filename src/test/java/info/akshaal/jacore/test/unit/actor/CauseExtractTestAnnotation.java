/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.test.unit.actor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import info.akshaal.jacore.annotation.ExtractBy;

/**
 * Annotation to test cause extraction.
 * @author akshaal
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.PARAMETER)
@ExtractBy (CauseExtractorExample.class)
public @interface CauseExtractTestAnnotation {
}
