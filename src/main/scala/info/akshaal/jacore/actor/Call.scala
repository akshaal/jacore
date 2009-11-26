/*
 * Call.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package info.akshaal.jacore
package actor

import org.aopalliance.intercept.MethodInvocation

private[actor] final case class Call (invocation : MethodInvocation) {
    override def toString = "Call(" + invocation.getMethod.getName + ")"
}
