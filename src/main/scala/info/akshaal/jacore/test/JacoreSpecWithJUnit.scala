/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package test

import org.specs.SpecificationWithJUnit

/**
 * Specification with additional features to be tested specs framework runned by junit.
 */
class JacoreSpecWithJUnit (name : String) extends SpecificationWithJUnit (name)
                                             with JacoreSpecAddons
