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

import com.paulbutcher.test._

class OverloadedMethodsTest extends IsolatedSpec {

  behavior of "Mocks"

  they should "mock traits with overloaded methods which have different number of type params" in { // test for issue #85
    trait Foo {
      def overloaded[T](x: T): String
      def overloaded(x: String): String
    }

    val fooMock = mock[Foo]
    (fooMock.overloaded[Double] _) expects (1.0) returning "one"
    fooMock.overloaded(1.0) shouldBe "one"

    (fooMock.overloaded(_: String)) expects ("2") returning "two"
    fooMock.overloaded("2") shouldBe "two"
  }

  they should "mock traits with overloaded methods which have different number of type params (2)" in {
    trait Foo {
      def overloaded[T](x: T): String
      def overloaded[T](x: T, y: String): String
    }

    val fooMock = mock[Foo]

    (fooMock.overloaded[Double]: Double => String) expects (1.0) returning "one"
    fooMock.overloaded(1.0) shouldBe "one"

    (fooMock.overloaded[Double]: (Double, String) => String).expects(2.0, "foo") returning "two"
    fooMock.overloaded(2.0, "foo") shouldBe "two"
  }

  they should "mock traits with overloaded methods which have different number of type params (3)" in {
    trait Foo {
      def overloaded[T](x: T): String
      def overloaded[T, U](x: T, y: U): String
    }

    val fooMock = mock[Foo]

    (fooMock.overloaded[Double]: Double => String) expects (1.0) returning "one"
    fooMock.overloaded(1.0) shouldBe "one"

    (fooMock.overloaded[Double, String]: (Double, String) => String).expects(2.0, "foo") returning "two"
    fooMock.overloaded(2.0, "foo") shouldBe "two"
  }

  they should "mock traits with overloaded methods which have different number of type params (4)" in {
    trait Foo {
      def overloaded[T](x: T, y: String): String
      def overloaded[T, U](x: T, y: U): String
    }

    val fooMock = mock[Foo]

    (fooMock.overloaded[Double]: (Double, String) => String) expects (1.0, "foo") returning "one"
    fooMock.overloaded(1.0, "foo") shouldBe "one"

    (fooMock.overloaded[String, Double]: (String, Double) => String).expects("foo", 2.0) returning "two"
    fooMock.overloaded("foo", 2.0) shouldBe "two"
  }

  they should "cope with overloaded methods" in {
    val m = mock[TestTrait]
    (m.overloaded(_: Int)).expects(10).returning("got an integer")
    (m.overloaded(_: Int, _: Double)).expects(10, 1.23).returning("got two parameters")
    assertResult("got an integer") { m.overloaded(10) }
    assertResult("got two parameters") { m.overloaded(10, 1.23) }
  }

  they should "cope with polymorphic overloaded methods" in {
    val m = mock[TestTrait]
    (m.overloaded[Double] _).expects(1.23).returning("polymorphic method called")
    assertResult("polymorphic method called") { m.overloaded(1.23) }
  }

  they should "choose between polymorphic and non-polymorphic overloaded methods correctly" in {
    val m = mock[TestTrait]
    (m.overloaded(_: Int)).expects(42).returning("non-polymorphic called")
    (m.overloaded[Int] _).expects(42).returning("polymorphic called")
    assertResult("non-polymorphic called") { m.overloaded(42) }
    assertResult("polymorphic called") { m.overloaded[Int](42) }
  }

  they should "mock PrintStream.print(String)" in { // test for issue #39
    import java.io.{OutputStream, PrintStream}
    class MockablePrintStream extends PrintStream(mock[OutputStream], false)

    val m = mock[MockablePrintStream]
    (m.print(_: String)) expects ("foo")
    m.print("foo")
  }


  they should "handle type aliases correctly" in {
    type X = Int
    type Y = X

    class GenericType[T]
    type ConcreteType = GenericType[X]

    class Foo {
      def foo()(y: GenericType[Y]) = 42
      def foo(a: Int)(y: GenericType[Y]) = 42
    }

    val m = mock[Foo]
    (m.foo()(_: ConcreteType)) expects (*)

    m.foo()(new ConcreteType())
  }


  override def newInstance = new OverloadedMethodsTest
}

