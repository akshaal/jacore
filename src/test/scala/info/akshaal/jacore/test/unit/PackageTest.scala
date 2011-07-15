/** Akshaal (C) 2011. GNU GPL. http://akshaal.info */

package info.akshaal.jacore
package test
package unit

class PackageTest extends JacoreSpecWithJUnit ("Jacore package specification") {
    "Jacore package" should {
        "provide pipeline operator |>" in {
            def test (x : String) : String = "@" + x + "!"

            ("x" |> test)  must_==  "@x!"
            ("123" |> test)  must_==  "@123!"
            ("123" |> test |> test)  must_==  "@@123!!"

            (1 |> (_ + 2))  must_==  3
            (13 |> (_.toString) |> test)  must_==  "@13!"
        }
    }
}
