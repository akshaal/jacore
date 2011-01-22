/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test

import java.util.concurrent.{CountDownLatch, TimeUnit => JavaTimeUnit}

import actor.Actor

/**
 * Makes it possible to wait a momment when messages are processed by actor.
 */
trait Waitable extends {
    // This is initialized before superclass is called

    private var messageLatch : CountDownLatch = null
} with Actor {
    /**
     * How much time we wait for a message to arrive to actor before timing out.
     */
    def timeout : TimeValue = 2 seconds

    /**
     * Execute the given code and wait for a message to be processed by actor. If
     * message is not received within some timeout interval, then MessageTimeout exception
     * will be thrown.
     * @param count number of batches to wait
     * @param f code to execute before waiting for a message on actor
     */
    def waitForMessageBatchesAfter (count : Int) (f : => Any) : Unit = {
        messageLatch = new CountDownLatch (count)

        debug ("Executing message trigger before waiting for message(s)")

        f

        debug ("Waiting for " + count + " message(s)")
        if (!messageLatch.await (timeout.inMilliseconds, JavaTimeUnit.MILLISECONDS)) {
            throw new MessageTimeoutException
        }
    }

    /**
     * Execute the given code and wait for a message to be processed by actor. If
     * message is not received within some timeout interval, then MessageTimeout
     * exception will be thrown.
     * @param f code to execute before waiting for a message on actor
     */
    def waitForMessageAfter (f : => Any) : Unit = {
        waitForMessageBatchesAfter (1) {f}
    }

    protected override def afterActs () : Unit = {
        try {
            super.afterActs
        } finally {
            if (messageLatch != null) {
                messageLatch.countDown ()
            }
        }
    }
}
