/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package actor

import Predefs._
import logger.Logging

import org.objectweb.asm.{ClassVisitor, Opcodes, Type}
import net.sf.cglib.core.{ReflectUtils, DefaultNamingPolicy}
import java.util.{Arrays, Comparator}

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
        cv.visit (Opcodes.V1_6,
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
        iv.visitMaxs (0, 0)
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
        /* doItVisitor.visitVarInsn (Opcodes.ALOAD, 0); // this
        doItVisitor.visitFieldInsn (Opcodes.GETFIELD, "Victim$DispatcherGeneratedImpl", "this$0", "LVictim;");
        doItVisitor.visitMethodInsn (Opcodes.INVOKEVIRTUAL, "Victim", "act", "()V");*/

        mv.visitInsn (Opcodes.ICONST_0)
        mv.visitInsn (Opcodes.IRETURN)
        mv.visitMaxs (0, 0)

        mv.visitEnd ();
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
         * @return TODO
         */
        private def compareExtractions (extractions1 : Set[MessageExtraction],
                                        extractions2 : Set[MessageExtraction]) : Int =
        {
            -1 // TODO
        }
    }
}