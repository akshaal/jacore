/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

import java.lang.annotation.Annotation
import java.lang.reflect.Modifier
import org.objectweb.asm.Type

import logger.Logging
import annotation.{Act, ExtractBy}
import utils.ClassUtils

/**
 * The sole role of this class is to scan Actor classes for methods annotated with @Act
 * annotation and do some sanity checks.
 */
private[actor] object ActorClassScanner extends Logging {
    private type OptionalMessageExtractorClass = Option[Class[MessageExtractor[_, _]]]

    /**
     * Scan actor for messages annotated with @Act annotation and validate these methods.
     * @param actor actor to scan
     * @return method descriptions for annotated methods
     */
    def scan (actor : Actor) : Seq[ActMethodDesc] = {
        // TODO: Implement cache

        // TODO: It would be nice if we could detect CallByMessage annotation and
        // test that they (method) confirm to requriements (not final and so on).
        // Also, we must check if method was enchances or not!

        val actorClass = actor.getClass

        def badClass (str : String) : Nothing = {
            throw new UnrecoverableError ("Actor class " + actorClass.getName + ": " + str)
        }

        // Check methods and collect information
        var methods : List[ActMethodDesc] = Nil

        // Classes tree of this actor class
        val classesTree =
                Iterator.iterate[Class[_]] (actorClass) (_.getSuperclass)
                        .takeWhile (_ != classOf[Object])
                        .toList

        debugLazy ("Discovered the following classes tree " + classesTree + " for " + actor)

        // List of all declared methods (unique by name, parameter types and annotations)

        val allDeclaredMethodsGroupedBySignature =
                classesTree.map (_.getDeclaredMethods.toList)
                           .flatten
                           .groupBy (method => (method.getName,
                                                method.getParameterTypes.toList,
                                                method.getParameterAnnotations.map(_.toList)
                                                                              .toList))
                           .map (_._2)

        val allMethodNames = allDeclaredMethodsGroupedBySignature.map (_(0).getName)

        debugLazy ("There are (all) visible methods " + allMethodNames + " of " + actor)

        // For each found method signature
        for (methodsOfSignature <- allDeclaredMethodsGroupedBySignature;
             methodsActAnnotations = methodsOfSignature.map (_.getAnnotation(classOf[Act]))
                                                       .filter (_ != null);
             if !methodsActAnnotations.isEmpty)
        {
            val method = methodsOfSignature(0)
            val methodName = method.getName

            debugLazy ("Processing " + methodsOfSignature)

            def badMethod (str : String) : Nothing = {
                badClass ("Action method " + methodName + " " + str)
            }

            // Get and check subscribe option consistency
            val subscribe = methodsActAnnotations.head.subscribe
            val subscribeOverriden = methodsActAnnotations.tail.map (_.subscribe)
            if (!subscribeOverriden.isEmpty && subscribeOverriden.count (_ != subscribe) > 0) {
                badMethod ("is overriden with different subscribe flag values in Act annotation")
            }

            // Get and check subscribe option consistency
            val suborder = methodsActAnnotations.head.suborder
            val suborderOverriden = methodsActAnnotations.tail.map (_.suborder)
            if (!suborderOverriden.isEmpty && suborderOverriden.count (_ != suborder) > 0) {
                badMethod ("is overriden with different suborder flag values in Act annotation")
            }

            // Checks modifiers: must not be static
            val modifiers = method.getModifiers

            if (Modifier.isStatic (modifiers)) {
                badMethod ("must not be static")
            }

            if (Modifier.isPrivate (modifiers)) {
                badMethod ("must not be private")
            }

            // Check return type: must be void
            val returnType = method.getReturnType
            if (returnType != Void.TYPE) {
                badMethod ("must return nothing, but returns " + returnType.getName)
            }

            // Check uniqueness of method name
            if (allMethodNames.count(_ == methodName) > 1) {
                badMethod ("must not be overloaded or overriden with"
                           + " different set of annotations on paramteres")
            }

            // Check params: must be at least one parameter
            val paramTypes = method.getParameterTypes
            if (paramTypes.length == 0) {
                badMethod ("must have at least one argument")
            }

            // Find extractor annotations for arguments
            def findArgExtractor (annotations : Array[Annotation]) : OptionalMessageExtractorClass =
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

                    case 1 => {
                        val extractor = argExtractors(0).get
                        if (!classOf[MessageExtractor[_, _]].isAssignableFrom(extractor)) {
                            badMethod ("has an extractor not implementing MessageExtractor interface: "
                                       + extractor)
                        }

                        Some (extractor).asInstanceOf[OptionalMessageExtractorClass]
                    }

                    case _ => badMethod ("has argument with more than one extractor annotation")
                }
            }

            val paramExtractors = method.getParameterAnnotations.map (findArgExtractor)

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
            val messageExtractionDefinitions =
                    for (paramDesc <- paramDescs.tail)
                            yield MessageExtractionDefinition[Any] (
                                    acceptExtractionClass = ClassUtils.box (paramDesc.clazz),
                                    messageExtractor =
                                        paramDesc.extractor
                                                 .get.asInstanceOf[Class[MessageExtractor[Any,_]]])

            // Check each extraction
            for (messageExtractionDefinition <- messageExtractionDefinitions) {
                val extractor = messageExtractionDefinition.messageExtractor
                val acceptExtractionClass = messageExtractionDefinition.acceptExtractionClass

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
            val messageMatcherDefinition =
                    MessageMatcherDefinition (
                           acceptMessageClass = acceptMessageClass.asInstanceOf[Class[Any]],
                           messageExtractionDefinitions = Set(messageExtractionDefinitions : _*))

            if (messageMatcherDefinition.messageExtractionDefinitions
                                        .map(_.messageExtractor)
                                        .size
                                    != paramDescs.length - 1)
            {
                badMethod ("must not use same extractor multiple times")
            }

            // Gather information
            methods ::= ActMethodDesc (name = methodName,
                                       subscribe = subscribe,
                                       suborder = suborder,
                                       params = paramDescs,
                                       matcherDefinition = messageMatcherDefinition,
                                       typeDescriptor = Type.getMethodDescriptor (method))
        }

        debugLazy ("Found action methods " + methods + " for " + actor)

        // Sanity checks on class level
        for ((_, methodGroup) <- methods.groupBy (_.matcherDefinition)) {
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
private[actor] sealed case class ActMethodDesc (
                                name : String,
                                subscribe : Boolean,
                                suborder : Int,
                                params : Seq[ActMethodParamDesc],
                                matcherDefinition : MessageMatcherDefinition[_],
                                typeDescriptor : String)
/**
 * Describes an argument of method annotated with @Act annotation.
 */
private[actor] sealed case class ActMethodParamDesc (
                                clazz : Class[_],
                                extractor : Option[Class[MessageExtractor[_ ,_]]])
