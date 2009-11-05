/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.test.unit.actor;

import info.akshaal.jacore.system.actor.MessageExtractor;

/**
 * Cause extractor. Used in tests.
 * @author akshaal
 */
public class CauseExtractorExample implements MessageExtractor<Exception, Throwable> {
    @Override
    public Throwable extractFrom (final Exception msg) {
        return msg.getCause();
    }
}
