/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package actor

import org.aopalliance.intercept.MethodInvocation

private[actor] final case class Call (invocation : MethodInvocation) {
    override def toString = "Call(" + invocation.getMethod.getName + ")"
}
