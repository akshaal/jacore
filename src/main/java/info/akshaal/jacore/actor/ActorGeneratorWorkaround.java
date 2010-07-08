/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.actor;

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
