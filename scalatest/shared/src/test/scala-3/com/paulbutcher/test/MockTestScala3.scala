package com.paulbutcher.test

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

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MockTestScala3 extends AnyFreeSpec with MockFactory with Matchers {

  autoVerify = false

  "In Scala 3, Mocks should" - {

    "mock type constructor arguments" in {
      class WithTC[TC[_]](tc: TC[Int])
      type ID[A] = A
      "mock[WithTC[List]]" should compile
      "mock[WithTC[ID]]" should compile
    }


    "mock iterables and builders" in {
      "mock[scala.collection.mutable.Builder[String, Unit]]" should compile
      """
        |val m = mock[Iterable[Int]]
        |m.collect.expects(*).returns(Iterable.single(1))
        |m.collect(_ => 2) shouldBe Iterable.single(1)
        |""".stripMargin should compile
    }

    "mock generic arguments" in {
      class WithGeneric[T](t: T)
      "mock[WithGeneric[String]]" should compile
      "mock[WithGeneric[Int]]" should compile
    }

    "mock multiple generic arguments" in {
      class MyClass
      class MyTycon[T]
      class WithGeneric[T, B[_]](b: B[T], t: T)
      "mock[WithGeneric[String, List]]" should compile
      "mock[WithGeneric[Int, Option]]" should compile
      "mock[WithGeneric[MyClass, MyTycon]]" should compile
    }

    "mock type constructor context bounds" in {
      trait Async[F[_]]
      class A[F[_]: Async](val b: B[F])
      class B[F[_]: Async](val c: C[F])
      trait C[F[_]]
      "mock[A[List]]" should compile
      "mock[B[List]]" should compile
      "mock[C[List]]" should compile
    }

    "mock with opaque parameter types" in {
      type MyString = MyString.Type
      object MyString {
        opaque type Type <: String = String
      }

      class Bar(foo: MyString)

      val m = mock[Bar]
    }

    "mock with tagged parameter types" in {
      trait Tag[+U] extends Any {
        type Tag <: U
      }
      type @@[+T, +U] = T & Tag[U]

      extension [T](t: T)
        def taggedWith[U]: T @@ U = t.asInstanceOf[T @@ U]

      case class CacheApi()
      trait SettingsCache
      trait DataCache

      case class Controller(settingsCache: CacheApi @@ SettingsCache)

      object RealContext {
        val settingsCache: CacheApi @@ SettingsCache = CacheApi().taggedWith[SettingsCache]
        val dataCache: CacheApi @@ DataCache = CacheApi().taggedWith[DataCache]
        val contoller = Controller(settingsCache)
      }

      val controller = mock[Controller]
    }

    "mock method returning this.type" in {
      val m1 = mock[ThisTypeTrait]

      m1.thisTypeMethod.expects(*).returns(m1)

      m1.thisTypeMethod(1) shouldBe m1
    }

    "mock by-name arguments" in {
      class ByName(t: => String)
      "mock[ByName]" should compile
    }

    "mock identical methods where one return type is context function" in {
      trait Context

      type ContextExample[T] = Context ?=> T

      trait ExampleService {
        def exampleMethodWithContext(a: String): ContextExample[String]
      }

      val exampleMock = mock[ExampleService]

      (exampleMock.exampleMethodWithContext: String => Context ?=> String)
        .expects(*)
        .returns((_: Context) ?=> "foo")

      exampleMock.exampleMethodWithContext("123")(using new Context {}) shouldBe "foo"
    }
  }
}