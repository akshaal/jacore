/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore.system.actor

trait Broadcaster {

}

/**
 * Describes a matcher for messages
 */
private[actor] sealed case class MessageMatcher (
                    acceptMessageClass : Class[_ <: Any],
                    messageExtractions : Set[MessageExtraction])

private[actor] sealed case class MessageExtraction (
                    acceptExtractionClass : Class[_ <: Any],
                    messageExtractor : Class[_ <: Any])