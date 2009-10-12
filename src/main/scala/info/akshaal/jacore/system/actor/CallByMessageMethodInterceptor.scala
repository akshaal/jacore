/*
 * CallByMessageMethodInterceptor.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore.system
package actor

import org.aopalliance.intercept.{MethodInterceptor, MethodInvocation}
import logger.Logging

/**
 * This class is supposed to handle invocations of methods annotated with
 * @CallByMessage annotation.
 */
class CallByMessageMethodInterceptor extends MethodInterceptor with Logging {
    /*
     * NOTE: Implementation details.
     *
     * As for guice 2.0, the method interception works as follows.
     * 1. An annotated method is about to be called.
     * 2. A proxy created by guice intercepts the call.
     * 3. The proxy sets invocation index to 0.
     * 4. The proxy invokes our method intercepter.
     * 5. If our method intercepters choose to proceed with execution
     *    it calls 'proceed' method of MethodInvocation object.
     * 6. The proceed method increments index again, and now index is 1.
     * 7. The proceed method compares index with number of method intercepters
     *    registered for the current method. If number of intercepters
     *    is equal to current index, it means that there are no more
     *    method intercepters registered for the current method, and thus
     *    proceed method calls original code of the method. Otherwise,
     *    proceed method takes intercepter by its index and calls 'invoke'
     *    method. That means that if there is only one interceptor (ours)
     *    registered for the current method and index is 1, then the original
     *    method is executed.
     * 8. When original method is executed and control returns back to the
     *    proceed method, index is decremented.
     *
     * That all works fine if proceed method is invoked from within
     * method intercepter. But if proceed method is invoked outside of the
     * method intercepter, then method intercepter is called,
     * not a super method as it might be expected. This behavior must be
     * taken into account in order to avoid having this method send message
     * over and over again instead of executing super method.
     */

    /**
     * This method is called when annotated method is about to be executed.
     */
    override def invoke (invocation : MethodInvocation) : Object = {
        if (CallByMessageMethodInterceptor.callNow.get) {
            CallByMessageMethodInterceptor.callNow.set (false)

            debugLazy ("Proceeding with invocation on object "
                       + invocation.getThis + " of method "
                       + invocation.getMethod.getName)

            invocation.proceed
        } else {
            debugLazy ("Wrapping invocation on object "
                       + invocation.getThis + " of method "
                       + invocation.getMethod.getName)

            invocation.getThis.asInstanceOf[Actor] ! (Call (invocation))
        }

        return null
    }
}

/**
 * Thread local state of the actor method intercepter.
 */
private[actor] object CallByMessageMethodInterceptor {
    /**
     * True signals that the next interception of the annotated method
     * must proceed, not wrap invocation into message.
     */
    val callNow = new ThreadLocal[Boolean] {
        override def initialValue : Boolean = false
    }

    /**
     * Call method.
     */
    def call (call : Call) = {
        try {
            callNow.set (true)
            call.invocation.proceed
        } finally {
            callNow.set (false)
        }
    }
}
