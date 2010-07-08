/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test

import org.specs.SpecificationWithJUnit

/**
 * Specification with additional features to be tested specs framework runned by junit.
 */
class JacoreSpecWithJUnit (name : String) extends SpecificationWithJUnit (name)
                                             with JacoreSpecAddons
