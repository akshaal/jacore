/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package actor

import logger.Logging

import org.objectweb.asm.{ClassVisitor, MethodVisitor, Opcodes, Type, Label, ClassWriter}
import net.sf.cglib.core.{DefaultNamingPolicy, DefaultGeneratorStrategy, DebuggingClassWriter}

/**
 * Generates instances of MessageMatcher classes for MessageMatcherDefinition.
 *
 * @author akshaal
 */
private[actor] class ActorMessageMatcherGenerator (matcherDefinition : MessageMatcherDefinition[_])
                  extends ActorGeneratorWorkaround
                  with Logging
{
    import ActorMessageMatcherGenerator._

    /**
     * Creates an instance of dispatcher.
     */
    def create () : Object = {
        val matcherDefinitionClassesUsed =
                matcherDefinition.messageExtractionDefinitions
                                 .map (_.acceptExtractionClass) +
                              matcherDefinition.acceptMessageClass
        val matcherDefinitionAsShortString =
                matcherDefinitionClassesUsed.map (clazz => clazz.getName.split('.').last)
                                            .mkString ("$")
        val namePrefix = classOf[MessageMatcher].getName + "$" + matcherDefinitionAsShortString

        setNamePrefix (namePrefix)
        setNamingPolicy (NamingPolicy)
        setStrategy (GeneratorStrategy)

        super.create (matcherDefinition)
    }

    /**
     * {InheritedDoc}
     */
    override def generateClass (cv : ClassVisitor) : Unit = {
        val messageMatcherInterfaceIN = Type.getInternalName (classOf[MessageMatcher])
        val objectClassIN = Type.getInternalName (classOf[Object])
        val implClassIN = getClassName.replace ('.', '/')

        debugLazy ("Generating " + getClassName)

        // Class header
        cv.visit (Opcodes.V1_5,
                  Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL,
                  implClassIN,
                  null, /* signature */
                  objectClassIN, /* super */
                  Array(messageMatcherInterfaceIN) /* interfaces */)

        // Constructor
        val iv =
            cv.visitMethod (Opcodes.ACC_PUBLIC,
                            "<init>" /* name */,
                            "()V" /* desc */,
                            null /* sign */,
                            null /* excs */)

        iv.visitCode ()
        iv.visitMaxs (1, 1)
        iv.visitVarInsn (Opcodes.ALOAD, 0);
        iv.visitMethodInsn (Opcodes.INVOKESPECIAL,
                            objectClassIN,
                            "<init>",
                            "()V")
        iv.visitInsn (Opcodes.RETURN)
        iv.visitEnd ()

        // Emit 'isAcceptable' method
        emitIsAcceptableMethod (cv, implClassIN)

        // -- end

        cv.visitEnd ()
    }

    /**
     * Emit 'isAcceptable' method.
     * @param cv class visitor
     * @param implClassIN class internal name of implementation class of matcher
     */
    private def emitIsAcceptableMethod (cv : ClassVisitor, implClassIN : String) : Unit =
    {
        // Emit
        val mv = cv.visitMethod (Opcodes.ACC_PUBLIC,
                                 "isAcceptable",
                                 "(Ljava/lang/Object;)Z",
                                 null /* sign */,
                                 null /* excs */)

        mv.visitCode ()

        // Maxs
        val extractionsCount = matcherDefinition.messageExtractionDefinitions.size
        val argsNum = 2 /* 2 means 'this, msg' */
        val stackSize = 2 /* 2 means 'this, msg' */
        val localsSize = argsNum
        mv.visitMaxs (stackSize, localsSize)

        // Emit method body
        val skipInvocation = new Label

        // Check message with instanceof
        emitLoadMsg (mv)
        emitInstanceOfCheck (mv, matcherDefinition.acceptMessageClass, skipInvocation)

        // Check extractions
        for (extractionDefinition <- matcherDefinition.messageExtractionDefinitions) {
            val extractor = extractionDefinition.messageExtractor
            val extractorIN = internalNameOf (extractor)

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

            // Check result
            emitInstanceOfCheck (mv, extractionDefinition.acceptExtractionClass, skipInvocation)
        }

        // return true
        mv.visitInsn (Opcodes.ICONST_1)
        mv.visitInsn (Opcodes.IRETURN)

        // skipped
        mv.visitLabel (skipInvocation)

        // Result false
        mv.visitInsn (Opcodes.ICONST_0)
        mv.visitInsn (Opcodes.IRETURN)

        // End
        mv.visitEnd ();
    }

    /**
     * Emit instruction to check value on stack with instanceof instruction. If check fails
     * instruction counter will be set to instruction pointed by label.
     */
    private def emitInstanceOfCheck (mv : MethodVisitor, clazz : Class[_], ifNot : Label) : Unit =
    {
        if (clazz == classOf[Object]) {
            mv.visitJumpInsn (Opcodes.IFNULL, ifNot)
        } else {
            mv.visitTypeInsn (Opcodes.INSTANCEOF, internalNameOf (clazz))
            mv.visitJumpInsn (Opcodes.IFEQ, ifNot)
        }
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
                matcherDefinition.getClass.getClassLoader

    /**
     * {InheritedDoc}
     */

    override protected def firstInstance (clazz : Class[_]) : Object =
                clazz.newInstance.asInstanceOf[Object]

    /**
     * {InheritedDoc}
     */
    override protected def nextInstance (instance : Object) : Nothing = {
                throw new UnrecoverableError ("Not implemented")
    }
}

/**
 * Companion object.
 */
private[actor] object ActorMessageMatcherGenerator {
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
}
