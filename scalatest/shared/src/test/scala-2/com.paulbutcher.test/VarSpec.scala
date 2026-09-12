package com.paulbutcher.test

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.reflect.ClassTag
import scala.language.higherKinds

class VarSpec extends AnyFunSpec with MockFactory with Matchers {

  autoVerify = false

  trait Vars {
    var aVar: String
    var concreteVar = "foo"
  }

  class WithParameters(foo: Int, bar: String)

  it("mock constructor arguments") {
    withExpectations {
      val m = mock[WithParameters]
      m.toString
    }
  }

  it("mock traits with vars") {
    withExpectations {
      val m = mock[Vars]
      (m.aVar_= _).expects("foo")
      (() => m.aVar).expects().returning("bar")
      m.aVar = "foo"
      assertResult("bar") {
        m.aVar
      }
    }
  }

  it("compile without args") {
    class ContextBounded[T: ClassTag] {
      def method(x: Int): Unit = ()
    }

    val m = stub[ContextBounded[String]]

  }

  it("compile with args") {
    class ContextBounded[T: ClassTag](x: Int) {
      def method(x: Int): Unit = ()
    }

    val m = stub[ContextBounded[String]]

  }

  it("compile with provided explicitly type class") {
    class ContextBounded[T](x: ClassTag[T]) {
      def method(x: Int): Unit = ()
    }

    val m = stub[ContextBounded[String]]

  }

  it("mock type constructor arguments") {
    class WithTC[TC[_]](tc: TC[Int])
    type ID[A] = A
    val foo = stub[WithTC[List]]
    val bar = stub[WithTC[ID]]
  }

  it("mock generic arguments") {
    class WithGeneric[T](t: T)

    val foo = stub[WithGeneric[String]]
    val bar = stub[WithGeneric[Int]]
  }

  it("mock type constructor context bounds") {
    trait Async[F[_]]
    class A[F[_] : Async](val b: B[F])
    class B[F[_] : Async](val c: C[F])
    trait C[F[_]]

    val foo = stub[A[List]]
    val bar = stub[B[List]]
    val baz = stub[C[List]]
  }

  it("mock iterables and builders") {
    withExpectations {
      "mock[scala.collection.mutable.Builder[String, Unit]]" should compile
      """
        |val m = mock[Iterable[Int]]
        |(m.filter _).expects(*).returns(Seq(1))
        |m.filter(_ => true) shouldBe Seq(1)
        |""".stripMargin should compile
    }
  }

}