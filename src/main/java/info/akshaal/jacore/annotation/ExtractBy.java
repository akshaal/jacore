/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.annotation;

import info.akshaal.jacore.system.actor.MessageExtractor;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;

/**
 * Annotation for parameters of methods annotated with @Act. This annotation specifies
 * a class to use to extract an object from message.
 *
 * @author akshaal
 */
@Retention (RetentionPolicy.RUNTIME)
@Target ({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface ExtractBy {
    Class<? extends MessageExtractor<?, ?>> value ();
}
