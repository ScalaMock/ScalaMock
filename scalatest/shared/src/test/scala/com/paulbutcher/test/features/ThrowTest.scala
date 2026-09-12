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

package com.paulbutcher.test.features

import com.paulbutcher.test.IsolatedSpec

class ThrowTest extends IsolatedSpec {

  case class TestException() extends RuntimeException
  case class AnotherTestException() extends RuntimeException

  val noArgFunMock = mockFunction[String]
  val intFunMock = mockFunction[Int, String]

  behavior of "Mock function"

  it should "throw what it is told to (throwing)" in {
    noArgFunMock.expects().throwing(new TestException)
    intercept[TestException] { noArgFunMock() }
  }

  it should "throw what it is told to (throws)" in {
    noArgFunMock.expects().throws(new TestException)
    intercept[TestException] { noArgFunMock() }
  }

  it should "throw computed exception" in {
    intFunMock.expects(*).repeat(3 to 3).onCall({ (arg: Int) =>
      if (arg == 1) throw new TestException()
      else if (arg == 2) throw new AnotherTestException()
      else "Foo"
    })

    intercept[TestException] { intFunMock(1) }
    intercept[AnotherTestException] { intFunMock(2) }
    intFunMock(3) shouldBe "Foo"
  }

  override def newInstance = new ThrowTest
}
