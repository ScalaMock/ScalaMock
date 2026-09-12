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

class AbstractOverrideMethodTest extends AnyFlatSpec with Matchers with MockFactory {
  class A extends B with D
  trait B extends C {
    def foo(): Int = 1
    def bar[T](seq: Seq[T]): Seq[String] = seq.map(_.toString)
    override def baz(a: String, b: Int): Int = a.size + b
  }
  trait C {
    def foo(): Int
    def bar[T](seq: Seq[T]): Seq[String]
    def baz(a: String, b: Int): Int = (a.size * 2) + b
  }
  trait D extends C {
    abstract override def foo(): Int = super.foo() * 2
    abstract override def bar[T](seq: Seq[T]): Seq[String] = "first" +: super.bar(seq) :+ "last"
    abstract override def baz(a: String, b: Int): Int = super.baz(a, b) + 1
  }

  "ScalaTest suite" should "permit mocking classes build with stackable trait pattern" in {
    val mockedClass = mock[A]
    (mockedClass.foo _).expects().returning(42)
    (mockedClass.bar _).expects(*).returning(Seq("a", "b", "c"))
    (mockedClass.baz _).expects("A", 1).returning(2)
    (mockedClass.baz _).expects("B", 1).never()
    mockedClass.foo() shouldBe 42
    mockedClass.bar(Seq(1,2,3)) shouldBe Seq("a", "b", "c")
    mockedClass.baz("A", 1) shouldBe 2
  }
}
