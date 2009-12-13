/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.actor

/**
 * This trait provides a way for actor to delegate a code execution by passing
 * code in a message.
 */
trait ActorDelegation { this : Actor =>
    /**
     * Execute code later on (after all currently queued message are processed).
     *
     * @param reason describe why the code must be postponed
     * @param code code to be executed later
     */
    def postponed (reason : String) (code : => Unit) : Unit = {
        this ! (PostponedBlock (reason, () => code))
    }

    /**
     * Message with postponed code.
     */
    protected case class PostponedBlock (reason : String, code : () => Unit)
}
