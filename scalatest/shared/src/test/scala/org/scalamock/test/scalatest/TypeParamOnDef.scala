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

package org.scalamock.test.scalatest

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

trait Bar
case class Baz(s: String) extends Bar

trait Foo {
  def p[T <: Bar](gen: Seq[T], t: Seq[T] => Seq[String]): Seq[String] = t(gen)
  def q[T <: Bar](gen: Seq[T]): Seq[String] = gen.map(_.toString)
}

class TypeParamOnFunctionArgToMethod extends AnyFlatSpec with Matchers with MockFactory {

  "TypeParamOnFunctionArgToMethod suite" should "permit mocking a method that takes a function w/ input parameterised by fn type param" in {
    val mockedTrait = mock[Foo]
    (mockedTrait.p[Baz](_:Seq[Baz], _: Seq[?] => Seq[String])).expects(Seq(Baz("one")), *).returning(Seq("one"))
    (mockedTrait.q[Baz](_:Seq[Baz])).expects(Seq(Baz("one"))).returning(Seq("one"))

    mockedTrait.p(Seq(Baz("one")), (_:Seq[Baz]).map(_.toString)) shouldBe Seq("one")
    mockedTrait.q(Seq(Baz("one"))) shouldBe Seq("one")
  }
}
