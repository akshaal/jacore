/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package utils

import java.io.{PrintWriter, File}
import java.util.Collection

import scala.collection.JavaConversions._
import scala.collection.mutable.{HashSet, Set}

import com.google.inject.{Guice, Inject, Injector, Key}

import com.google.inject.grapher.{GrapherModule, Renderer}
import com.google.inject.grapher.graphviz.{GraphvizModule, GraphvizRenderer}
import com.google.inject.spi.BindingTargetVisitor

import Predefs._

/**
 * Utils to help work with Guice.
 */
object GuiceUtils {
    private[GuiceUtils] val loggerKey = Key.get (classOf[java.util.logging.Logger])

    /**
     * Create graph definition (.dot file)
     * @param filename name of the file to create
     */
    def createModuleGraph (filename : String, injector : Injector, classes : Class[_]*) : Unit =
    {
        // Create list of keys to draw
        val keys = new HashSet [Key [_]]
        for (key <- injector.getBindings.keySet) {
            if (key.getTypeLiteral.getRawType.getPackage != classOf[Guice].getPackage
                 && key != loggerKey)
            {
                keys += key
            }
        }

        for (clazz <- classes) {
            keys += Key.get (clazz)
        }

        // Create graph
        val out = new PrintWriter (new File (filename), "UTF-8")
        val graphInjector = Guice.createInjector (new GrapherModule, new GraphvizModule)
        val renderer = graphInjector.getInstanceOf [GraphvizRenderer]

        renderer.setOut (out).setRankdir ("TB");
        
        graphInjector.getInstanceOf [JacoreInjectorGrapher].graph (injector, keys)
    }

    /**
     * Guice's InjectorGrapher has some limitations - we can't use it. This is a copy
     * of Guice's InjectorGrapher with modication which allows to use binding from Injector
     * in addition to root classes.
     */
    private[GuiceUtils] class JacoreInjectorGrapher @Inject() (
                                keyVisitor : BindingTargetVisitor [Any, Collection[Key[_]]],
                                graphingVisitor : BindingTargetVisitor [Any, Void],
                                renderer : Renderer)
    {
        def graph (injector : Injector, keys : Set [Key[_]]) : Unit = {
            val visitKeys = new HashSet [Key[_]]
            val visitedKeys = new HashSet [Key[_]]

            visitKeys ++= keys

            // Visit every key that is supposed to be visited
            while (!visitKeys.isEmpty) {
                val key = visitKeys.head
                visitKeys.remove (key)

                // Visit only if we have not visited it yet
                if (!visitedKeys.contains (key)) {
                    visitedKeys.add (key)

                    // Visit binding for key
                    val binding = injector.getBinding (key)
                    binding.acceptTargetVisitor (graphingVisitor)

                    // Check keys used for binding
                    val newKeys = binding.acceptTargetVisitor (keyVisitor)
                    if (newKeys != null) {
                        visitKeys ++= newKeys
                    }
                }
            }

            renderer.render ()
        }
    }
}
