/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.test.unit.actor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import info.akshaal.jacore.system.annotation.ExtractBy;

/**
 * Annotation to test cause extraction.
 * @author akshaal
 */
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.PARAMETER)
@ExtractBy (CauseExtractorExample.class)
public @interface CauseExtractTestAnnotation {
}
