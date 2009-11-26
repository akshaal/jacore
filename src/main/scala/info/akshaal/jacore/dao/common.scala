/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package info.akshaal.jacore
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

    /**
     * Insert data asynchronously. When insertion is finished, a notification message
     * InsertFinished will be issued with the payload.
     * @param data data to insert
     * @param payload to send back in notification message
     */
    def insert (data : T, payload : Any) : Unit
}

/**
 * Issued to the requester when insert is finished.
 */
sealed case class InsertFinished (payload : Any)
