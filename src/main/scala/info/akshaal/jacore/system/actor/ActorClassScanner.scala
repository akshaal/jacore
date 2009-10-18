/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import Predefs._
import logger.Logging

import java.lang.reflect.Modifier
import annotation.{Act, ExtractBy}
import utils.ClassUtils

/**
 * The sole role of this class is to scan Actor classes for methods annotated with @Act
 * annotation and do some sanity checks.
 */
private[actor] object ActorClassScanner extends Logging {
    /**
     * Scan actor for messages annotated with @Act annotation and validate these methods.
     * @param actor actor to scan
     * @return method descriptions for annotated methods
     */
    def scan (actor : Actor) : Seq[ActMethodDesc] = {
        val actorClass = actor.getClass

        def badClass (str : String) {
            throw new UnrecoverableError ("Actor class " + actorClass.getName + ": " + str)
        }

        // Check methods and collect information
        var methods : List[ActMethodDesc] = Nil

        // For each annotated method (including inherited) of this class
        val allMethodNames = actorClass.getMethods.map (_.getName)

        for (method <- actorClass.getMethods if method.isAnnotationPresent (classOf[Act])) {
            val methodName = method.getName

            def badMethod (str : String) {
                badClass ("Action method " + methodName + " " + str)
            }

            // Checks modifiers
            val modifiers = method.getModifiers

            if (Modifier.isStatic (modifiers)) {
                badMethod ("must not be static")
            }

            // Check return type
            val returnType = method.getReturnType
            if (returnType != Void.TYPE) {
                badMethod ("must return nothing, but returns " + returnType.getName)
            }

            // Check uniqueness of method name
            if (allMethodNames.count(_ == methodName) > 1) {
                badMethod ("must not be overloaded")
            }

            // Check params
            val paramTypes = method.getParameterTypes
            if (paramTypes.length == 0) {
                badMethod ("must have at least one argument")
            }

            val paramExtractors =
                method.getParameterAnnotations.map (
                        _.find (_.isInstanceOf [ExtractBy])
                         .map (_.asInstanceOf [ExtractBy].value())
                    )

            val paramDescs =
                for ((paramClazz, paramExtractor) <- paramTypes.zip (paramExtractors))
                    yield ActMethodParamDesc (clazz = paramClazz,
                                              extractor = paramExtractor)

            if (!paramDescs.head.extractor.isEmpty) {
                badMethod ("can't have extraction as a first argument."
                           + " First argument must be a message method receives.")
            }

            if (paramDescs.count (_.extractor.isEmpty) > 1) {
                badMethod ("must have no more than one argument without extractor")
            }

            // Create matcher
            val acceptMessageClass = ClassUtils.box (paramDescs.head.clazz)
            val messageExtractions =
                    for (paramDesc <- paramDescs.tail)
                            yield MessageExtraction (
                                        acceptExtractionClass = ClassUtils.box (paramDesc.clazz),
                                        messageExtractor = paramDesc.extractor.get)

            for (messageExtraction <- messageExtractions) {
                val extractor = messageExtraction.messageExtractor
                val acceptExtractionClass = messageExtraction.acceptExtractionClass

                if (!classOf[MessageExtractor[Any, Any]].isAssignableFrom(extractor)) {
                    badMethod ("has an extractor not implementing MessageExtractor interface: "
                               + extractor)
                }

                val extractingMethods =
                        extractor.getMethods
                                 .filter (m => m.getName == "extractFrom" && !m.isSynthetic)
                if (extractingMethods.length > 1) {
                    badMethod ("uses an extractor with overloaded extractFrom method: "
                               + extractor)
                }

                val extractorMethod = extractingMethods.head
                val extractorMethodArg = extractorMethod.getParameterTypes()(0)
                val extractorMethodReturn = extractorMethod.getReturnType

                if (!extractorMethodArg.isAssignableFrom(acceptMessageClass)) {
                    badMethod ("uses extractor " + extractor
                               + " which can't handle messages of class " + acceptMessageClass)
                }

                if (!extractorMethodReturn.isAssignableFrom(acceptExtractionClass)
                    && !acceptExtractionClass.isAssignableFrom(extractorMethodReturn))
                {
                    badMethod ("uses extractor " + extractor
                               + " which produces values of class incompatible"
                               + " to the class of extraction (argument)")
                }
            }

            val messageMatcher =
                    MessageMatcher (acceptMessageClass = acceptMessageClass,
                                    messageExtractions = Set(messageExtractions : _*))

            if (messageMatcher.messageExtractions.size != paramDescs.length - 1) {
                badMethod ("must not have duplicated arguments")
            }

            // Gather information
            val actAnnotation = method.getAnnotation (classOf[Act])

            methods ::= ActMethodDesc (name = methodName,
                                       subscribe = actAnnotation.subscribe,
                                       params = paramDescs,
                                       matcher = messageMatcher)

        }

        debugLazy ("Found action methods " + methods)

        // Sanity checks on class level
        for ((_, methodGroup) <- methods.groupBy (_.matcher)) {
            if (methodGroup.length > 1) {
                badClass ("More than one mathod match the same messages: "
                          + methodGroup.map(_.name).mkString(" "))
            }
        }

        methods;
    }
}

/**
 * Describes a method annotated with @Act annotation.
 */
private[actor] sealed case class ActMethodDesc (name : String,
                                                subscribe : Boolean,
                                                params : Seq[ActMethodParamDesc],
                                                matcher : MessageMatcher)

/**
 * Describes an argument of method annotated with @Act annotation.
 */
private[actor] sealed case class ActMethodParamDesc (clazz : Class[_ <: Any],
                                                     extractor : Option[Class[_ <: Any]])
