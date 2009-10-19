/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import Predefs._
import logger.Logging
import utils.ClassUtils

import org.objectweb.asm.{ClassVisitor, MethodVisitor, Opcodes, Type, Label, ClassWriter}
import net.sf.cglib.core.{ReflectUtils, DefaultNamingPolicy, DefaultGeneratorStrategy,
                          DebuggingClassWriter}
import java.util.{Arrays, Comparator}
import scala.collection.mutable.HashMap

/**
 * Generates instances of MethodDispatcher classes for Actors.
 *
 * @author akshaal
 */
private[actor] class ActorMethodDispatcherGenerator (actor : Actor,
                                                     methods : Seq[ActMethodDesc])
                  extends ActorMethodDispatcherGeneratorWorkaround
                  with Logging
{
    import ActorMethodDispatcherGenerator._

    private val actorClass = actor.getClass
    private val actorClassName = actor.getClass.getName

    /**
     * Creates an instance of dispatcher.
     */
    def create () : Object = {
        setNamePrefix (actorClassName)
        setNamingPolicy (NamingPolicy)
        setStrategy (GeneratorStrategy)
        super.create (actorClassName)
    }

    /**
     * {InheritedDoc}
     */
    override def generateClass (cv : ClassVisitor) : Unit = {
        val superClassIN = Type.getInternalName (classOf[Actor])
        val actorClassIN = Type.getInternalName (actorClass)
        val methodDispatcherClassIN = superClassIN + "$MethodDispatcher"
        val implClassIN = getClassName.replace ('.', '/')

        debugLazy ("Generating " + getClassName)

        // Class header
        cv.visit (Opcodes.V1_5,
                  Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL,
                  implClassIN,
                  null, /* signature */
                  methodDispatcherClassIN,
                  null /* interfaces */)

        // Member this$0 where enclosing class is stored
        cv.visitField (Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
                       "this$0"   /* name */,
                       "L" + actorClassIN + ";" /* descriptor */,
                       null /* signature */,
                       null).visitEnd ()

        // Constructor
        val iv =
            cv.visitMethod (Opcodes.ACC_PUBLIC,
                            "<init>" /* name */,
                            "(L" + actorClassIN + ";)V" /* desc */,
                            null /* sign */,
                            null /* excs */)

        iv.visitCode ()
        iv.visitMaxs (2, 2)
        iv.visitVarInsn (Opcodes.ALOAD, 0)
        iv.visitVarInsn (Opcodes.ALOAD, 1)
        iv.visitFieldInsn (Opcodes.PUTFIELD,
                           implClassIN,
                           "this$0",
                           "L" + actorClassIN + ";")
        iv.visitVarInsn (Opcodes.ALOAD, 0);
        iv.visitVarInsn (Opcodes.ALOAD, 1);
        iv.visitMethodInsn (Opcodes.INVOKESPECIAL,
                            methodDispatcherClassIN,
                            "<init>",
                            "(L" + superClassIN + ";)V")
        iv.visitInsn (Opcodes.RETURN)
        iv.visitEnd ()

        // Emit 'dispatch' method
        emitDispatch (cv = cv, actorClassIN = actorClassIN, implClassIN = implClassIN)

        // -- end

        cv.visitEnd ()
    }

    /**
     * Emit 'dispatch' method.
     * @param cv class visitor
     * @param actorClassIN class internal name of actor
     * @param implClassIN class internal name of implementation class of dispatcher
     */
    private def emitDispatch (cv : ClassVisitor,
                              actorClassIN : String,
                              implClassIN : String) : Unit =
    {
        // Sort methods in priority descending order
        val sortedMethods = methods.toArray
        Arrays.sort (sortedMethods, ActMethodDescComparator)

        debugLazy (actor.getClass.getName + ": act methods '"
                   + (methods.map (_.name).mkString (", "))
                   + "' sorted to '"
                   + (sortedMethods.map (_.name).mkString (", "))
                   + "'")

        // Emit
        val mv = cv.visitMethod (Opcodes.ACC_PUBLIC,
                                 "dispatch",
                                 "(Ljava/lang/Object;)Z",
                                 null /* sign */,
                                 null /* excs */)

        mv.visitCode ()

        // Maxs
        val maxExtractionsPerMethod =
                 methods.map (method => method.matcher.messageExtractions.size).max

        val stackSize = maxExtractionsPerMethod + 2 /* 2 means 'this, msg' */
        val argsNum = 2 /* 2 means 'this, msg' */
        val localsSize = maxExtractionsPerMethod + argsNum
        mv.visitMaxs (stackSize, localsSize)

        // Gen code
        for (method <- sortedMethods) {
            emitDispatchCodeForMethod (mv, actorClassIN, implClassIN, method)
        }

        mv.visitInsn (Opcodes.ICONST_0)
        mv.visitInsn (Opcodes.IRETURN)

        // End
        mv.visitEnd ();
    }

    /**
     * Emit 'dispatch' method.
     * @param cv class visitor
     * @param actorClassIN class internal name of actor
     * @param implClassIN class internal name of implementation class of dispatcher
     */
    private def emitDispatchCodeForMethod (mv : MethodVisitor,
                                           actorClassIN : String,
                                           implClassIN : String,
                                           method : ActMethodDesc) : Unit =
    {
        val matcher = method.matcher
        val skipInvocation = new Label

        // Check message with instanceof
        emitLoadMsg (mv)
        emitInstanceOfCheck (mv, matcher.acceptMessageClass, skipInvocation)

        // Check extractions
        var freeSlot = 2
        var usedSlots = new HashMap[Class[_], Int]
        for (extraction <- matcher.messageExtractions) {
            val extractorIN = internalNameOf (extraction.messageExtractor)

            // Construct extractor
            mv.visitTypeInsn (Opcodes.NEW, extractorIN)
            mv.visitInsn (Opcodes.DUP)
            mv.visitMethodInsn (Opcodes.INVOKESPECIAL,
                                extractorIN,
                                "<init>",
                                "()V")

            // Run extractFrom
            emitLoadMsg (mv)
            mv.visitMethodInsn (Opcodes.INVOKEVIRTUAL,
                                extractorIN,
                                "extractFrom",
                                "(Ljava/lang/Object;)Ljava/lang/Object;")
            mv.visitInsn (Opcodes.DUP)
            mv.visitVarInsn (Opcodes.ASTORE, freeSlot)

            // Check result
            emitInstanceOfCheck (mv, extraction.acceptExtractionClass, skipInvocation)

            // Remember
            usedSlots (extraction.messageExtractor) = freeSlot
            freeSlot += 1
        }

        // Invoke
        emitLoadThis (mv)
        mv.visitFieldInsn (Opcodes.GETFIELD,
                           implClassIN,
                           "this$0",
                           "L" + actorClassIN + ";")

        for (param <- method.params) {
            param.extractor match {
                case None => {
                    emitLoadMsg (mv)
                }

                case Some (extractorClass) => {
                    mv.visitVarInsn (Opcodes.ALOAD, usedSlots (extractorClass))
                }
            }

            if (param.clazz != classOf[Object]) {
                mv.visitTypeInsn(Opcodes.CHECKCAST,
                                 internalNameOf (ClassUtils.box (param.clazz)))
            }
            
            emitUnboxObjectTo (mv, param.clazz)
        }
        
        mv.visitMethodInsn (Opcodes.INVOKEVIRTUAL,
                            actorClassIN,
                            method.name,
                            method.typeDescriptor)

        // return true
        mv.visitInsn (Opcodes.ICONST_1)
        mv.visitInsn (Opcodes.IRETURN)

        // End
        mv.visitLabel (skipInvocation)
    }

    /* Unbox object value from stack if specified class is primitive.
     * @param mv method visitor
     * @param clazz class
     */
    private def emitUnboxObjectTo (mv : MethodVisitor, clazz : Class[_]) : Unit = {
        def emit (clazz : String, name : String, desc : String) : Unit =
                mv.visitMethodInsn (Opcodes.INVOKEVIRTUAL,
                                    clazz,
                                    name + "Value",
                                    "()" + desc)

        clazz match {
            case java.lang.Boolean.TYPE      => emit ("java/lang/Boolean",   "boolean", "Z")
            case java.lang.Byte.TYPE         => emit ("java/lang/Byte",      "byte", "B")
            case java.lang.Character.TYPE    => emit ("java/lang/Character", "char", "C")
            case java.lang.Double.TYPE       => emit ("java/lang/Double",    "double", "D")
            case java.lang.Float.TYPE        => emit ("java/lang/Float",     "float", "F")
            case java.lang.Integer.TYPE      => emit ("java/lang/Integer",   "int", "I")
            case java.lang.Long.TYPE         => emit ("java/lang/Long",      "long", "J")
            case java.lang.Short.TYPE        => emit ("java/lang/Short",     "short", "S")
            case _ => ()
        }
    }

    /**
     * Emit instruction to check value on stack with instanceof instruction. If check fails
     * instruction counter will be set to instruction pointed by label.
     */
    private def emitInstanceOfCheck (mv : MethodVisitor, clazz : Class[_], ifNot : Label) : Unit =
    {
        // NOTE: We check even for Object because it guards us against nulls

        mv.visitTypeInsn (Opcodes.INSTANCEOF, internalNameOf (clazz))
        mv.visitJumpInsn (Opcodes.IFEQ, ifNot)
    }

    /**
     * Put "this" object on stack.
     * @param mv method visitor
     */
    private def emitLoadThis (mv : MethodVisitor) : Unit = {
        mv.visitVarInsn (Opcodes.ALOAD, 0)
    }

    /**
     * Put "msg" object on stack.
     * @param mv method visitor
     */
    private def emitLoadMsg (mv : MethodVisitor) : Unit = {
        mv.visitVarInsn (Opcodes.ALOAD, 1)
    }

    /**
     * Get internal name of classes including objects.
     * @param clazz class
     * @return internal name
     */
    private def internalNameOf (clazz : Class[_]) : String = {
        val ty = Type.getType (clazz)

        if (clazz.isArray) {
            ty.getDescriptor
        } else {
            ty.getInternalName
        }
    }

    /**
     * {InheritedDoc}
     */
    override protected def getDefaultClassLoader () : ClassLoader =
                actor.getClass.getClassLoader

    /**
     * {InheritedDoc}
     */

    override protected def firstInstance (clazz : Class[_]) : Object =
                ReflectUtils.newInstance (clazz, Array(actorClass), Array(actor))

    /**
     * {InheritedDoc}
     */
    override protected def nextInstance (instance : Object) : Object =
                throw new UnrecoverableError ("Not implemented")
}

/**
 * Companion object.
 */
private[actor] object ActorMethodDispatcherGenerator {
    /**
     * Custom naming scheme.
     */
    object NamingPolicy extends DefaultNamingPolicy {
        override protected def getTag : String = "ByJacore"
    }

    /**
     * Custom generator strategy.
     */
    object GeneratorStrategy extends DefaultGeneratorStrategy {
        protected override def getClassWriter() : ClassWriter = {
            new DebuggingClassWriter (0)
        }
    }

    /**
     * Compares two method descriptions. Subclasses always win.
     */
    object ActMethodDescComparator extends Comparator[ActMethodDesc] with Logging {
        /**
         * {@Inherited}
         */
        override def compare (method1 : ActMethodDesc, method2 : ActMethodDesc) : Int = {
            val matcher1 = method1.matcher
            val matcher2 = method2.matcher

            // First, compare acceptMessage classes
            val acceptMessageClass1 = matcher1.acceptMessageClass
            val acceptMessageClass2 = matcher2.acceptMessageClass
            val acceptMessageClass1IsSuperOrSame =
                            acceptMessageClass1.isAssignableFrom (acceptMessageClass2)
            val acceptMessageClass2IsSuperOrSame =
                            acceptMessageClass2.isAssignableFrom (acceptMessageClass1)

            if (acceptMessageClass1IsSuperOrSame && acceptMessageClass2IsSuperOrSame) {
                // That means that message classes are the same
                compareExtractions (matcher1.messageExtractions, matcher2.messageExtractions)
            } else if (!acceptMessageClass1IsSuperOrSame && !acceptMessageClass2IsSuperOrSame) {
                // That means that message classes are incompatible at all.
                // In order to have constitant result we will return comparison
                // by classes hashcode.
                acceptMessageClass1.hashCode.compare (acceptMessageClass2.hashCode)
            } else {
                if (acceptMessageClass2IsSuperOrSame) -1 else 1 // Superclass comes AFTER subclass.
            }
        }

        /**
         * Compare two sets of extractions.
         * @param extractions1 set of extractions 1
         * @param extractions2 set of extractions 2
         * @return 1 if extraction1 is wider than extractions2
         */
        private def compareExtractions (extractions1 : Set[MessageExtraction],
                                        extractions2 : Set[MessageExtraction]) : Int =
        {
            // Map extractor to extraction class
            def mapFromExtraction (extractions : Set[MessageExtraction])
                                                            : Map[Class[_], Class[_]] =
            {
                Map (extractions.toSeq
                                .map (ex => (ex.messageExtractor, ex.acceptExtractionClass)) : _*)
            }

            val map1 = mapFromExtraction (extractions1)
            val map2 = mapFromExtraction (extractions2)
            val extractors1 = map1.keySet
            val extractors2 = map2.keySet
            
            // Compare by common extractors
            val commonExtractors = extractors1.intersect (extractors2)
            var commonSupers1 = 0
            var commonSupers2 = 0
            for (commonExtractor <- commonExtractors) {
                val extraction1 = map1 (commonExtractor)
                val extraction2 = map2 (commonExtractor)
                val extraction1IsSuper = extraction1.isAssignableFrom (extraction2)
                val extraction2IsSuper = extraction2.isAssignableFrom (extraction1)

                if (!extraction1IsSuper && extraction2IsSuper) {
                    commonSupers2 += 1
                } else if (extraction1IsSuper && !extraction2IsSuper) {
                    commonSupers1 += 1
                }
            }

            if (commonSupers2 > commonSupers1) {
                return -1
            } else if (commonSupers2 < commonSupers1) {
                return 1
            }

            // Compare by number of uniq extractors
            val uniq1 = extractors1.size - commonExtractors.size
            val uniq2 = extractors2.size - commonExtractors.size

            if (uniq2 > uniq1) {
                return 1
            } else if (uniq2 < uniq1) {
                return -1
            }

            // There is no other option, but to use hashcode...
            return extractions1.hashCode.compare (extractions2.hashCode)
        }
    }
}