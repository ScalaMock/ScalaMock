// Copyright (c) 2011-2015 ScalaMock Contributors (https://github.com/paulbutcher/ScalaMock/graphs/contributors)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package com.paulbutcher.test.mock

import org.scalamock.function.FunctionAdapter1
import org.scalamock.scalatest.MockFactory

import scala.reflect.ClassTag
import scala.util.{Failure, Try}

import com.paulbutcher.test._
import some.other.pkg._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MockTest extends AnyFreeSpec with MockFactory with Matchers {
  
  autoVerify = false
  
  "Mocks should" - {
    //! TODO - in scala 3 we can't distinguish abstract var setters with non abstract vars setters
    //  TODO   and override modifier is not allowed when overriding abstract vars
   /*
   "cope with a var" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.aVar_= _).expects("foo")
        (() => m.aVar).expects().returning("bar")
        m.aVar = "foo"
        assertResult(null) { m.aVar }
      }
    }*/

    "fail if an unexpected method call is made" in {
      withExpectations {
        val m = mock[TestTrait]
        intercept[ExpectationException] { m.oneParam(42) }
      }
    }

    "allow expectations to be set" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.twoParams _).expects(42, 1.23).returning("a return value")
        assertResult("a return value") { m.twoParams(42, 1.23) }
      }
    }

    "fail if a non-matching method call is made" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.twoParams _).expects(42, 1.23)
        intercept[ExpectationException] { m.twoParams(1, 1.0) }
        m.twoParams(42, 1.23)
      }
    }

    "cope with nullary methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (() => m.nullary).expects().returning("a return value")
        assertResult("a return value") { m.nullary }
      }
    }

    "cope with infix operators" in {
      withExpectations {
        val m1 = mock[TestTrait]
        val m2 = mock[TestTrait]
        val m3 = mock[TestTrait]
        (m1.+ _).expects(m2).returning(m3)
        assertResult(m3) { m1 + m2 }
      }
    }

    "cope with curried methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.curried(_: Int)(_: Double)).expects(10, 1.23).returning("curried method called")
        val partial = m.curried(10) _
        assertResult("curried method called") { partial(1.23) }
      }
    }

    "cope with polymorphic methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.polymorphic(_: List[Int])).expects(List(1, 2)).returning("called with integers")
        (m.polymorphic(_: List[String])).expects(List("foo", "bar")).returning("called with strings")
        assertResult("called with integers") { m.polymorphic(List(1, 2)) }
        assertResult("called with strings") { m.polymorphic(List("foo", "bar")) }
      }
    }

    "cope with curried polymorphic methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.polycurried(_: Int)(_: String)).expects(42, "foo").returning((123, "bar"))
        val partial = m.polycurried(42)(_: String)
        assertResult((123, "bar")) { partial("foo") }
      }
    }

    "cope with parameters of polymorphic type" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.polymorphicParam _).expects((42, 1.23)).returning("it works")
        assertResult("it works") { m.polymorphicParam((42, 1.23)) }
      }
    }

    "cope with methods with repeated parameters" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.repeatedParam _).expects(42, Seq("foo", "bar"))
        m.repeatedParam(42, "foo", "bar")
      }
    }

    "cope with methods where Seq[T] is the last parameter" in { // issue #54
      trait ClassWithSeqTParam {
         def run(ints: Seq[Int]) : Int
      }

      withExpectations {
        val m = mock[ClassWithSeqTParam]

        (m.run _).expects(Seq(1, 2, 3)).returning(100)
        assertResult(100) { m.run(Seq(1, 2, 3)) }

        (m.run _).expects(*).returning(200)
        assertResult(200) { m.run(Seq(5, 55)) }

        (m.run _).expects(Seq()).returning(300)
        assertResult(300) { m.run(Seq.empty) }
      }
    }

    "cope with methods with implicit parameters" in {
      withExpectations {
        implicit val y: Double = 1.23
        val m = mock[TestTrait]
        (m.implicitParam(_: Int)(_: Double)).expects(42, 1.23).returning("it works")
        assertResult("it works") { m.implicitParam(42) }
      }
    }

    "cope with references to another package" in {
      withExpectations {
        val m = mock[TestTrait]
        val x = new SomeOtherClass
        (m.referencesSomeOtherPackage _).expects(x).returning(x)
        assertResult(x) { m.referencesSomeOtherPackage(x) }
      }
    }

    "cope with upper bound in another package" in {
      withExpectations {
        val m = mock[TestTrait]
        val x = new SomeOtherClass
        (m.otherPackageUpperBound(_: SomeOtherClass)).expects(x).returning(x)
        assertResult(x) { m.otherPackageUpperBound(x) }
      }
    }

    "cope with explicit references to another package" in {
      withExpectations {
        val m = mock[TestTrait]
        val x = new yet.another.pkg.YetAnotherClass
        (m.explicitPackageReference _).expects(x).returning(x)
        assertResult(x) { m.explicitPackageReference(x) }
      }
    }

    "cope with upper bound in an explictly referenced package" in {
      withExpectations {
        val m = mock[TestTrait]
        val x = new yet.another.pkg.YetAnotherClass
        (m.explicitPackageUpperBound(_: yet.another.pkg.YetAnotherClass)).expects(x).returning(x)
        assertResult(x) { m.explicitPackageUpperBound(x) }
      }
    }

  /*  //! TODO - currently doesn't work because we can't override concrete vars
    "cope with a non-abstract var" ignore {
      withExpectations {
        val m = mock[TestTrait]
        (m.concreteVar_= _).expects("foo")
        (() => m.concreteVar).expects().returning("bar")
        m.concreteVar = "foo"
        assertResult("bar") { m.concreteVar }
      }
    }*/

    "cope with curried varargs" in {
      withExpectations {
        trait Foo {
          def bar(i: Int)(what: Any*): String
        }

        val m = mock[Foo]
        (m.bar(_: Int)(_: Seq[_])).expects(42, Seq("1", "2")).returning("baz").once()
        m.bar(42)("1", "2") === "baz"
      }
    }


    "cope with a val" in {
      withExpectations {
        val m = mock[TestTrait]
        assertResult(null) { m.aVal }
      }
    }

    "cope with a non-abstract val" in {
      withExpectations {
        val m = mock[TestTrait]
        assertResult("foo") { m.concreteVal }
      }
    }

    "cope with a function val" in {
      withExpectations {
        val m = mock[TestTrait]
        assertResult(null) { m.fnVal }
      }
    }

    "cope with non-abstract methods" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.withImplementation _).expects(42).returning(1234)
        assertResult(1234) { m.withImplementation(42) }
      }
    }


    "mock an embeddded trait" in {
      withExpectations {
        val m = mock[TestTrait]
        val e = mock[m.Embedded]
        (() => m.referencesEmbedded()).expects().returning(e)
        assertResult(e) { m.referencesEmbedded() }
      }
    }


    "handle projected types correctly" in {
      withExpectations {
        val m = mock[TestTrait]
        val e = mock[m.Embedded]
        val o = mock[m.ATrait]
        val i = mock[e.ATrait]
        (() => e.innerTraitProjected()).expects().returning(i)
        (() => e.outerTraitProjected()).expects().returning(o)
        assertResult(o) { e.outerTraitProjected() }
        assertResult(i) { e.innerTraitProjected() }
      }
    }

    "handle path-dependent types correctly" in {
      withExpectations {
        val m = mock[TestTrait]
        val e = mock[m.Embedded]
        val o = mock[m.ATrait]
        val i = mock[e.ATrait]
        (() => e.innerTrait()).expects().returning(i)
        (() => e.outerTrait()).expects().returning(o)
        assertResult(o) { e.outerTrait() }
        assertResult(i) { e.innerTrait() }
      }
    }

    "cope with upper bounds" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.upperBound _).expects((42, "foo")).returning(2)
        assertResult(2) { m.upperBound((42, "foo")) }
      }
    }

    "cope with lower bounds" in {
      withExpectations {
        val m = mock[TestTrait]
        (m.lowerBound _).expects((1, 2), List[Product]()).returning("it works")
        assertResult("it works") { m.lowerBound((1, 2), List[Product]()) }
      }
    }

    "mock a polymorphic trait" in {
      withExpectations {
        val m = mock[PolymorphicTrait[String]]
        (m.method[Double] _).expects(42, "foo", 1.23).returning("a return value")
        assertResult("a return value") { m.method(42, "foo", 1.23) }
      }
    }

    "handle path-dependent polymorphic types correctly" in {
      withExpectations {
        val m = mock[PolymorphicTrait[String]]
        val e = mock[m.Embedded[Double]]
        val o = mock[m.ATrait[String, Double]]
        val i = mock[e.ATrait[String, Double]]
        (e.innerTrait _).expects("foo", 1.23).returning(i)
        (e.outerTrait _).expects("bar", 4.56).returning(o)
        assertResult(o) { e.outerTrait("bar", 4.56) }
        assertResult(i) { e.innerTrait("foo", 1.23) }
      }
    }

    "mock a class" in {
      withExpectations {
        val m = mock[TestClass]
        (m.m _).expects(42, "foo").returning((123, "bar"))
        assertResult((123, "bar")) { m.m(42, "foo") }
      }
    }

    "mock a specialized class [Int]" in {
      withExpectations {
        val m1x1 = mock[SpecializedClass[Int]]
        (m1x1.identity _).expects(42).returning(43)
        assertResult(43) {
          m1x1.identity(42)
        }
      }
    }

    "mock a specialized class [Int,String]" in {
      withExpectations {
        val m1x2 = mock[SpecializedClass2[Int,String]]
        (m1x2.identity2 _).expects(42,"43").returning((44,"45"))
        assertResult((44,"45")) {
          m1x2.identity2(42,"43")
        }

        (m1x2.identity _).expects(42).returning(43)
        assertResult(43) {
          m1x2.identity(42)
        }
      }
    }

    "mock a specialized class [Int,Int]" in {
      withExpectations {
        val m1x3 = mock[SpecializedClass2[Int,Int]]
        (m1x3.identity2 _).expects(42,43).returning((44,45))
        assertResult((44,45)) {
          m1x3.identity2(42,43)
        }

        (m1x3.identity _).expects(42).returning(43)
        assertResult(43) {
          m1x3.identity(42)
        }
      }
    }

    "mock a specialized class [String]" in {
      withExpectations {
        val m1x5 = mock[SpecializedClass[String]]
        (m1x5.identity _).expects("one").returning("four")
        assertResult("four") { m1x5.identity("one") }
      }
    }

    "mock a specialized class [List[String]]" in {
      withExpectations {
        val m2 = mock[SpecializedClass[List[String]]]
        (m2.identity _).expects(List("one", "two", "three")).returning(List("four", "five", "six"))
        assertResult(List("four", "five", "six")) { m2.identity(List("one", "two", "three")) }
      }
    }

    "allow to be declared as var" in { // test for issue #62
      withExpectations {
        var m = mock[TestTrait]
        (m.oneParam _).expects(42).returning("foo")
        assertResult("foo") { m.oneParam(42) }
      }
    }

    "mock Function1[A, B] trait" in withExpectations { // test for issue #69
      val f = mock[Function1[Any, Boolean]]
      (f.apply _).expects(*).returning(true)
      f("this is something") shouldBe true
    }

    "mock methods that need a class tag" in withExpectations {
      case class User(first: String, last: String, enabled: Boolean)

      trait DataProviderComponent {
        def find[T: ClassTag](id: Int): Try[T]
      }

      val provider = mock[DataProviderComponent]

      (provider.find[User](_: Int)(using _: ClassTag[User])) expects (13, *) returning (Failure[User](new Exception()))
      provider.find[User](13) shouldBe a[Failure[_]]
    }


    "mock class with nonempty default constructor" in {
      class TestNonEmptyDefaultConstructor(a: Int, b: String, c: AnyRef, d: Any)(aa: String)
      val m = mock[TestNonEmptyDefaultConstructor]
    }



    // TODO: issue 150 - causes a compiler error
//    "cope with curried function returning methods" in {
//      withExpectations {
//        val m = mock[TestTrait]
//        (m.curriedFuncReturn(_: Int)(_: Double)).expects(10, 1.23).returning("curried func return method called")
//        val partial = m.curriedFuncReturn(10)
//        assertResult("curried func return method called") { partial(1.23) }
//      }
//    }



    // issue 132
    "mock a trait which has a final method" in withExpectations {
      trait FinalMethodTrait {
        def somePublicMethod(param: String): Unit
        final def someFinalMethod(param: Int) = "final method"
      }

      val m = mock[FinalMethodTrait]  //test will not compile if the test fails (cannot override final member)
      (m.somePublicMethod _).expects(*).anyNumberOfTimes()
      // next line will cause a runtime error and is not valid
      // m.someFinalMethod _ expects * anyNumberOfTimes()
    }


    "mock a trait which has a protected method" in withExpectations {
      trait FooTrait {
        def somePublicMethod(param: String): Unit
        protected[mock] def protectedMethod() = ()
        private[mock] def privateMethod() = ()
      }

      val m = mock[FooTrait]
      (m.somePublicMethod _).expects(*).anyNumberOfTimes()
      // next lines will cause a runtime error and are not valid
      // m.privateMethod _ expects() anyNumberOfTimes()
      // m.protectedMethod _ expects() anyNumberOfTimes()
    }

    "log all calls" in withExpectations {
      val m = mock[TestTrait]

      inAnyOrderWithLogging {
        (m.oneParam _).expects(*).twice()
      }
      inSequenceWithLogging {
        (m.twoParams _).expects(*, *).once()
        (m.overloaded(_: Int)).expects(*).once()
      }
      m.oneParam(5)
      m.oneParam(42)
      m.twoParams(1, 2)
      m.overloaded(99)
    }

    "be able to mock notify(Int)" in withExpectations {
      trait HasNotifyMethod {
        def notify(x: Int): Int
      }

      "stub[HasNotifyMethod]" should compile
    }

    "mock constructor arguments" in {
      class WithOption(opt: Option[String])
      class WithInt(i: Int)
      class WithString(s: String)
      "mock[WithOption]" should compile
      "mock[WithInt]" should compile
      "mock[WithString]" should compile
    }
  }
}
