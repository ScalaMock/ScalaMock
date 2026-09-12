package com.paulbutcher.test

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class Scala3Spec extends AnyFunSpec with MockFactory with Matchers {

  it("mock refinement types") {
    """
      |trait Effect:
      |  type Address[_ <: String]
      |  type NetworkError <: Throwable
      |  type Id
      |
      |  def getId[P <: String](address: Address[P]): Id
      |  def reachablePeersOf[P <: String](peerRepr: String): Set[Address[P]]
      |
      |  type IntNetwork = Effect {type Id = Int; type NetworkError = Throwable; type Address[P <: String] = String}
      |
      |  val netEffect = stub[IntNetwork]
      |""".stripMargin should compile

  }

  it("mock traits with parameters") {
    trait Test(val a: Int) {
      def method(x: Int): Int
    }

    val m = mock[Test]
  }

  it("mock parameters with & and | types") {
    trait A
    trait B
    trait Test {
      def method(x: Int | String, y: A & B): Int
    }

    val m = mock[Test]

    (m.method _).expects(*, *).returns(0)
    m.method(1, new A with B) shouldBe 0
  }

  it("mock intersection type with type parameter from trait") {

    trait B

    trait C

    trait TraitWithGenericIntersection[A] {
      def methodWithGenericIntersection(x: A & B): Unit
    }

    val m = mock[TraitWithGenericIntersection[C]]

    val obj = new B with C {}

    (m.methodWithGenericIntersection _).expects(obj).returns(())

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with left type parameter from method") {

    trait B

    trait C

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A](x: A & B): Unit

      def methodWithGenericUnion[A](x: A | B): Unit
    }

    val m = mock[TraitWithGenericIntersection]

    val obj = new C with B {}

    (m.methodWithGenericIntersection[C] _).expects(obj).returns(())

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with right type parameter from method") {

    trait B

    trait C

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A](x: B & A): Unit
    }

    val m = mock[TraitWithGenericIntersection]

    val obj = new B with C {}

    (m.methodWithGenericIntersection[C] _).expects(obj).returns(())

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with both type parameters from method") {

    trait B

    trait C

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A, B](x: A & B): Unit
    }

    val m = mock[TraitWithGenericIntersection]

    val obj = new B with C {}

    (m.methodWithGenericIntersection[B, C] _).expects(obj).returns(())

    m.methodWithGenericIntersection(obj)
  }


  it("mock intersection type with more then two types from method") {

    trait B

    trait C

    trait D

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A, B, C](x: A & B & C): Unit
    }

    val m = mock[TraitWithGenericIntersection]

    val obj = new B with C with D {}

    (m.methodWithGenericIntersection[B, C, D] _).expects(obj).returns(())

    m.methodWithGenericIntersection(obj)
  }

  it("mock intersection type with more then two types from method, one of witch is stable") {

    trait B

    trait C

    trait D

    trait TraitWithGenericIntersection {
      def methodWithGenericIntersection[A, B](x: A & D & B): Unit
    }

    val m = mock[TraitWithGenericIntersection]

    val obj = new B with C with D {}

    (m.methodWithGenericIntersection[B, C] _).expects(obj).returns(())

    m.methodWithGenericIntersection(obj)
  }

  it("mock union type with left type parameter from method") {

    trait B

    trait C

    trait TraitWithGenericUnion {

      def methodWithGenericUnion[A](x: A | B): Unit
    }

    val m = mock[TraitWithGenericUnion]

    val obj1 = new C {}
    val obj2 = new B {}

    (m.methodWithGenericUnion[C] _).expects(obj1).returns(())
    (m.methodWithGenericUnion[C] _).expects(obj2).returns(())

    m.methodWithGenericUnion(obj1)
    m.methodWithGenericUnion(obj2)
  }

  it("mock union type with right type parameter from method") {

    trait B

    trait C

    trait TraitWithGenericUnion {

      def methodWithGenericUnion[A](x: B | A): Unit
    }

    val m = mock[TraitWithGenericUnion]

    val obj1 = new C {}
    val obj2 = new B {}

    (m.methodWithGenericUnion[C] _).expects(obj1).returns(())
    (m.methodWithGenericUnion[C] _).expects(obj2).returns(())

    m.methodWithGenericUnion(obj1)
    m.methodWithGenericUnion(obj2)
  }

  it("mock union return type") {

    trait A

    trait B

    trait TraitWithUnionReturnType {

      def methodWithUnionReturnType[T](): T | A
    }

    val m = mock[TraitWithUnionReturnType]

    val obj = new B {}

    (() => m.methodWithUnionReturnType[B]()).expects().returns(obj)

    m.methodWithUnionReturnType[B]() shouldBe obj
  }

  it("mock intersection return type") {

    trait A

    trait B

    trait TraitWithIntersectionReturnType {

      def methodWithIntersectionReturnType[T](): A & T
    }

    val m = mock[TraitWithIntersectionReturnType]

    val obj = new A with B {}

    (() => m.methodWithIntersectionReturnType[B]()).expects().returns(obj)

    m.methodWithIntersectionReturnType[B]() shouldBe obj
  }

  it("mock intersection|union types with type constructors") {

    trait A[T]

    trait B

    trait C

    trait ComplexUnionIntersectionCases {

      def complexMethod1[T](x: A[T] & T): A[T] & T
      def complexMethod2[T](x: A[A[T]] | T):  A[T] | T
      def complexMethod3[F[_], T](x: F[A[T] & F[T]] | T & A[F[T]]):  F[T] & T
      def complexMethod4[T](x: A[B & C] ): A[B & C]
      def complexMethod5[T](x: A[B | A[C]]): A[B | C]
    }

    val m = mock[ComplexUnionIntersectionCases]

    val obj = new A[B] with B {}
    val obj2 = new A[A[B]] with B {}

    (m.complexMethod1[B] _).expects(obj).returns(obj)
    (m.complexMethod2[B] _).expects(obj2).returns(new A[B] {})

    m.complexMethod1[B](obj)
    m.complexMethod2[B](obj2)
  }

  it("mock methods returning function") {
    trait Test {
      def method(x: Int): Int => String
    }

    val m = mock[Test]

    (m.method _).expects(*).returns((_: Int) => "f")
    m.method(1)(0) shouldBe "f"

  }
}
