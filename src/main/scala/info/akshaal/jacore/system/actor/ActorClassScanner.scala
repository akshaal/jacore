/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import Predefs._
import logger.Logging

import java.lang.annotation.Annotation
import java.lang.reflect.Modifier
import org.objectweb.asm.Type
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

        def badClass (str : String) : Nothing = {
            throw new UnrecoverableError ("Actor class " + actorClass.getName + ": " + str)
        }

        // Check methods and collect information
        var methods : List[ActMethodDesc] = Nil

        // For each annotated method (including inherited) of this class
        val allMethodNames = actorClass.getMethods.map (_.getName)

        for (method <- actorClass.getMethods if method.isAnnotationPresent (classOf[Act])) {
            val methodName = method.getName

            def badMethod (str : String) : Nothing = {
                badClass ("Action method " + methodName + " " + str)
            }

            // Checks modifiers: must not be static
            val modifiers = method.getModifiers

            if (Modifier.isStatic (modifiers)) {
                badMethod ("must not be static")
            }

            // Check return type: must be void
            val returnType = method.getReturnType
            if (returnType != Void.TYPE) {
                badMethod ("must return nothing, but returns " + returnType.getName)
            }

            // Check uniqueness of method name
            if (allMethodNames.count(_ == methodName) > 1) {
                badMethod ("must not be overloaded")
            }

            // Check params: must be at least one parameter
            val paramTypes = method.getParameterTypes
            if (paramTypes.length == 0) {
                badMethod ("must have at least one argument")
            }

            // Find extractor annotations for arguments
            def findArgExtractor (annotations : Array[Annotation]) : Option[Class[_]] =
            {
                def visitAnnotation (annotation : Annotation) : Option[Class[_]] = {
                    if (annotation.isInstanceOf[ExtractBy]) {
                        Some (annotation.asInstanceOf[ExtractBy].value)
                    } else {
                        annotation.annotationType.getAnnotation (classOf[ExtractBy]) match {
                            case null => None
                            case thisAnnotation => {
                                    visitAnnotation (thisAnnotation)
                            }
                        }
                    }
                }

                val argExtractors = annotations.map (visitAnnotation).filter (_.isDefined)

                argExtractors.size match {
                    case 0 => None
                    case 1 => Some (argExtractors(0).get)
                    case _ => badMethod ("has argument with more than one extractor annotation")
                }
            }

            val paramExtractors =
                        method.getParameterAnnotations.map (findArgExtractor)

            // Create parameter descriptions
            val paramDescs =
                for ((paramClazz, paramExtractor) <- paramTypes.zip (paramExtractors))
                    yield ActMethodParamDesc (clazz = paramClazz,
                                              extractor = paramExtractor)

            // First argument must not be extraction
            if (!paramDescs.head.extractor.isEmpty) {
                badMethod ("can't have extraction as a first argument."
                           + " First argument must be a message method receives.")
            }

            // Only first argument is message, others are values prduced by extractors
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

            // Check each extraction
            for (messageExtraction <- messageExtractions) {
                val extractor = messageExtraction.messageExtractor
                val acceptExtractionClass = messageExtraction.acceptExtractionClass

                // Extractor must implement MessageExtractor interface
                if (!classOf[MessageExtractor[Any, Any]].isAssignableFrom(extractor)) {
                    badMethod ("has an extractor not implementing MessageExtractor interface: "
                               + extractor)
                }

                // Extractor must have default constructor
                try {
                    extractor.getConstructor ()
                } catch {
                    case ex : NoSuchMethodException =>
                            badMethod ("uses an extractor without or inaccesible"
                                       + " default constructor: "
                                       + extractor)
                }

                // Extract must not have overloaded method extractFrom
                val extractingMethods =
                        extractor.getMethods
                                 .filter (m => m.getName == "extractFrom" && !m.isSynthetic)
                if (extractingMethods.length > 1) {
                    badMethod ("uses an extractor with overloaded extractFrom method: "
                               + extractor)
                }

                // Check that extractor can handle messages that this method receives
                val extractorMethod = extractingMethods.head
                val extractorMethodArg = ClassUtils.box (extractorMethod.getParameterTypes()(0))
                val extractorMethodReturn = ClassUtils.box (extractorMethod.getReturnType)

                if (!extractorMethodArg.isAssignableFrom(acceptMessageClass)) {
                    badMethod ("uses extractor " + extractor
                               + " which can't handle messages of class " + acceptMessageClass)
                }

                // Check that extractor produces values that are compatible with the method
                // argument
                if (!extractorMethodReturn.isAssignableFrom(acceptExtractionClass)
                    && !acceptExtractionClass.isAssignableFrom(extractorMethodReturn))
                {
                    badMethod ("uses extractor " + extractor
                               + " which produces values of class incompatible"
                               + " to the class of extraction (argument)")
                }
            }

            // Construct message matcher
            val messageMatcher =
                    MessageMatcher (acceptMessageClass = acceptMessageClass,
                                    messageExtractions = Set(messageExtractions : _*))

            if (messageMatcher.messageExtractions.map(_.messageExtractor).size
                                        != paramDescs.length - 1)
            {
                badMethod ("must not use same extractor multiple times")
            }

            // Gather information
            val actAnnotation = method.getAnnotation (classOf[Act])

            methods ::= ActMethodDesc (name = methodName,
                                       subscribe = actAnnotation.subscribe,
                                       suborder = actAnnotation.suborder,
                                       params = paramDescs,
                                       matcher = messageMatcher,
                                       typeDescriptor = Type.getMethodDescriptor (method))
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
                                                suborder : Int,
                                                params : Seq[ActMethodParamDesc],
                                                matcher : MessageMatcher,
                                                typeDescriptor : String)

/**
 * Describes an argument of method annotated with @Act annotation.
 */
private[actor] sealed case class ActMethodParamDesc (clazz : Class[_ <: Any],
                                                     extractor : Option[Class[_ <: Any]])
