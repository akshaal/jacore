/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package io
package db
package jdbc

/**
 * Package object that contains implicit to be useful for Statement types.
 */
package object statement {
    import _root_.scala.annotation.implicitNotFound

    /**
     * Type indicating that Statement doesn't obtain values from a domain objects.
     * This type has no instances.
     */
    sealed trait Domainless

    /**
     * Implicit function to convert string into placeholderless statement.
     * 
     * @param sql string statement to be converted to Statement0 object
     * @return object created from the given string
     */
    @inline
    implicit def string2statement (sql : String) = Statement0 [Domainless] (sql = sql)


    // - - - - - - - -  --  - - - - - - - - - - - - - - - - - - - - - - --  - - - - - -
    // Verification of domain type changing process for statements

    /**
     * If instance of this class exists it means that it is allowed to change {TryingDomain} type
     * to {CurrentDomain} type in Statement class signature. It should only be allowed to change
     * from Domainless to some other type.
     */
    @implicitNotFound (
        msg = "Statement already works with domain type"
            + " ${CurrentDomain}, but this operator wants to change it to ${TryingDomain}")
    sealed abstract class NewDomainVerified [CurrentDomain, +TryingDomain]

    /**
     * This method makes sure that it is possible to use domain object of the same
     * type as one that was used before.
     */
    implicit def verifyForIdentityDomain [A] : NewDomainVerified [A, A] = null

    /**
     * This object says that if Domainless is currently used in Statement than it is allowed
     * to use any other domain type.
     */
    implicit object StatementDomainlessToAny extends NewDomainVerified [Domainless, Nothing]
}