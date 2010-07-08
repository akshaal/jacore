/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore.actor;

/**
 * Extract an object from message. Classes implementing this interfaces are used
 * to create a matcher of actor messages.
 */
public interface MessageExtractor<A, B> {
    /**
     * Actract an object of type B from a message.
     *
     * @param msg message to extract from
     * @returns an extracted object
     */
    B extractFrom (A msg);
}
