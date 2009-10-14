/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.actor;

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
