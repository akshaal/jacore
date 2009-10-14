/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface Act {
    boolean subscribe () default false;
}