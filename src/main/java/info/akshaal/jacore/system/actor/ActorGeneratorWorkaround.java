/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.actor;

import net.sf.cglib.core.AbstractClassGenerator;

/**
 * Workaround for scala to overcome limitation which makes it impossible to use static nested
 * classes of super class in scala.
 *
 * @author akshaal
 */
public abstract class ActorGeneratorWorkaround extends AbstractClassGenerator {
    private static final Source SOURCE = new Source ("ActorGeneratorImpl");

    protected ActorGeneratorWorkaround () {
        super (SOURCE);
    }
}
