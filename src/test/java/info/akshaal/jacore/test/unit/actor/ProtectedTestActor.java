/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.test.unit.actor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import info.akshaal.jacore.annotation.Act;
import info.akshaal.jacore.actor.Actor;
import info.akshaal.jacore.actor.HiPriorityActorEnv;

/**
 * Actor to test work with protected methods.
 * @author akshaal
 */
@Singleton
public class ProtectedTestActor extends Actor {
    public boolean intReceived = false;

    @Inject
    public ProtectedTestActor (final HiPriorityActorEnv actorEnv) {
        super (actorEnv);
    }

    @Act
    protected void test (int msg) {
        intReceived = true;
    }
}
