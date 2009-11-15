/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
package system
package dao

/**
 * Interface for data inserter.
 */
trait DataInserter[T] {
    /**
     * Insert data asynchronously.
     * @param data data to insert
     */
    def insert (data : T) : Unit
}