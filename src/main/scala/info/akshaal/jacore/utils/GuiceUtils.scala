/** Akshaal (C) 2009-2010. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package utils

import java.io.{PrintWriter, StringWriter, File}
import java.util.Collection

import scala.collection.JavaConversions._
import scala.collection.mutable.{HashSet, Set}

import com.google.inject.{Guice, Inject, Injector, Key, ConfigurationException}

import com.google.inject.grapher.{GrapherModule, Renderer}
import com.google.inject.grapher.graphviz.{GraphvizModule, GraphvizRenderer}
import com.google.inject.spi.BindingTargetVisitor

import logger.Logging

/**
 * Utils to help work with Guice.
 */
object GuiceUtils {
    private[GuiceUtils] val loggerKey = Key.get (classOf[java.util.logging.Logger])

    /**
     * Create graph definition (.dot file)
     * @param filename name of the file to create
     * @param injector injector to use
     * @classes additional root classes
     */
    def createModuleGraphAsString (injector : Injector, classes : Class[_]*) : String =
    {
        val stringWriter = new StringWriter
        val out = new PrintWriter (stringWriter)

        createModuleGraph (out, injector, classes : _*)

        stringWriter.toString
    }

    /**
     * Create graph definition (.dot file)
     * @param filename name of the file to create
     * @param injector injector to use
     * @classes additional root classes
     */
    def createModuleGraph (filename : String, injector : Injector, classes : Class[_]*) : Unit =
    {
        withCloseableIO (new PrintWriter (new File (filename), "UTF-8")) (out =>
            createModuleGraph (out, injector, classes : _*)
        )
    }

    /**
     * Create graph definition (.dot file)
     * @param out print writer
     * @param injector injector to use
     * @classes additional root classes
     */
    def createModuleGraph (out : PrintWriter, injector : Injector, classes : Class[_]*) : Unit =
    {
        // Create list of keys to draw
        val keys = new HashSet [Key [_]]
        def addKeysForInjector (currentInjector : Injector) {
            for (key <- currentInjector.getBindings.keySet) {
                if (key.getTypeLiteral.getRawType.getPackage != classOf[Guice].getPackage
                     && key != loggerKey)
                {
                    keys += key
                }
            }

            val parentInjector = currentInjector.getParent
            if (parentInjector != null) {
                addKeysForInjector (parentInjector)
            }
        }

        addKeysForInjector (injector)

        for (clazz <- classes) {
            keys += Key.get (clazz)
        }

        // Create graph
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
                                renderer : Renderer) extends Logging
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

                    try {
                        // Visit binding for key
                        val binding = injector.getBinding (key)
                        binding.acceptTargetVisitor (graphingVisitor)

                        // Check keys used for binding
                        val newKeys = binding.acceptTargetVisitor (keyVisitor)
                        if (newKeys != null) {
                            visitKeys ++= newKeys
                        }
                    } catch {
                        case exc : ConfigurationException =>
                            debugLazy ("Ignored" +:+ exc, exc)
                    }
                }
            }

            renderer.render ()
        }
    }
}
