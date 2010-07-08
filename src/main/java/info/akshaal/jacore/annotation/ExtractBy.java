/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.annotation;

import info.akshaal.jacore.actor.MessageExtractor;
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
