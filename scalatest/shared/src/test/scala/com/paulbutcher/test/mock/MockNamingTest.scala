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
import org.scalamock.function.MockFunction

class MockNamingTest extends IsolatedSpec {

  def getMockMethodName(method: MockFunction) = method.toString
  val m = mock[TestTrait]("mock")

  behavior of "Mock"

  it should "have a sensible method name when mocking a method without parameters" in {
    getMockMethodName(() => m.noParams()) shouldBe "<mock> TestTrait.noParams"
  }

  it should "have a sensible method name when mocking one parameter method" in {
    getMockMethodName(m.oneParam _) shouldBe "<mock> TestTrait.oneParam"
  }

  it should "have a sensible method name when mocking curried method" in {
    getMockMethodName(m.curried(_: Int)(_: Double)) shouldBe "<mock> TestTrait.curried"
  }

  it should "have a sensible method name when mocking an operator" in {
    getMockMethodName(m.+ _) shouldBe "<mock> TestTrait.+"
  }

  it should "have a sensible method name when mocking polymorphic method" in {
    getMockMethodName(m.polymorphic(_: List[_])) shouldBe "<mock> TestTrait.polymorphic[T]"
  }

  it should "have a sensible method name when mocking overloaded method" in {
    getMockMethodName(m.overloaded(_: Int)) shouldBe "<mock> TestTrait.overloaded"
  }

  it should "have a sensible method name when mocking a class" in {
    val myMock = mock[TestClass]
    getMockMethodName(myMock.m _) shouldBe "<mock-1> TestClass.m"
  }

  it should "have a sensible method name when mocking polymorphic trait" in {
    val myMock = mock[PolymorphicTrait[List[Int]]]
    getMockMethodName(myMock.method[Map[Int, String]] _) shouldBe "<mock-1> PolymorphicTrait[List[Int]].method[U]"
  }

  it can "be named using string literal" in {
    val myMock = mock[TestTrait]("mock name")
    getMockMethodName(myMock.oneParam _) shouldBe "<mock name> TestTrait.oneParam"
  }

  it should "should have its name evaluated during mock construction" in {
    var prefix = "mock"
    val mocks = for (idx <- 1 to 2) yield mock[TestTrait](prefix + idx)
    prefix = "changed"

    getMockMethodName(mocks(0).oneParam _) shouldBe "<mock1> TestTrait.oneParam"
    getMockMethodName(mocks(1).oneParam _) shouldBe "<mock2> TestTrait.oneParam"
  }

  it should "have sensible default name assigned" in {
    val myMock = mock[TestTrait]
    getMockMethodName(() => myMock.noParams()) shouldBe "<mock-1> TestTrait.noParams"
  }

  it should "have consistent names of mocked methods" in {
    val myMock = mock[TestTrait]
    getMockMethodName(() => myMock.noParams()) shouldBe "<mock-1> TestTrait.noParams"
    getMockMethodName(myMock.twoParams _) shouldBe "<mock-1> TestTrait.twoParams" // not <mock-2>
  }

  it should "should have differentiating default name assigned" in {
    val myMock1 = mock[TestTrait]
    val myMock2 = mock[TestTrait]
    getMockMethodName(myMock2.oneParam _) shouldBe "<mock-2> TestTrait.oneParam"
    getMockMethodName(myMock1.oneParam _) shouldBe "<mock-1> TestTrait.oneParam"
  }

  override def newInstance = new MockNamingTest
}
