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

import com.paulbutcher.test.IsolatedSpec

class MockFunctionNamingTest extends IsolatedSpec {

  behavior of "Mock function"

  it should "have a sensible default name" in {
    val m = mockFunction[String]
    m.toString shouldBe "MockFunction0-1"
  }

  it should "have the name we gave them when we use a symbol" in {
    val m = mockFunction[String](Symbol("a mock function"))
    m.toString shouldBe "a mock function"
  }

  it should "have the name we gave them when we use a string" in {
    val m = mockFunction[String]("another mock function")
    m.toString shouldBe "another mock function"
  }

  it should "resolve ambiguity when taking a symbol argument with no name specified" in {
    val m = mockFunction[Symbol, String]
    m.toString shouldBe "MockFunction1-1"
  }

  it should "resolve ambiguity when taking a symbol argument with a name specified" in {
    val m = mockFunction[Symbol, String](functionName("a named mock"))
    m.toString shouldBe "a named mock"
  }

  it should "have differentiating default name" in {
    val m1 = mockFunction[String]
    val m2 = mockFunction[String]
    val m3 = mockFunction[Int, String]

    m1.toString shouldBe "MockFunction0-1"
    m2.toString shouldBe "MockFunction0-2"
    m3.toString shouldBe "MockFunction1-3"
  }

  override def newInstance = new MockFunctionNamingTest
}
