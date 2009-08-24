/*
 * SingleTypeActor.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.actor

/**
 * Simple actor that process only message of one type.
 */
abstract class SingleTypeActor[T <: AnyRef] (actorEnv : ActorEnv)
                    extends Actor (actorEnv : ActorEnv)                    
{
    final def act () = {
        case x : T if supported(x) => act (x.asInstanceOf[T])
    }

    final def supported (x : T) =
        supportedMessageClass.isAssignableFrom (x.getClass ())

    /**
     * Returns class of messages this actor supports.
     */
    def supportedMessageClass : Class[T]

    /**
     * Process message.
     */
    def act (message : T) : Unit
}
