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

class CallCountTest extends IsolatedSpec {

  val noArgFunMock = mockFunction[String]
  val intFunMock = mockFunction[Int, String]

  autoVerify = false

  behavior of "Mock function"

  it should "fail if an unexpected call is made" in withExpectations {
    intercept[ExpectationException] { intFunMock(42) }
  }

  it should "fail if a method isn't called often enough (once)" in withExpectations {
    intFunMock.expects(42).once()
    intFunMock(42)
  }

  it should "not fail if a method is called once (once)" in withExpectations {
    intFunMock.expects(42).once()
    intFunMock(42)
  }

  it should "fail if a method is called too often (once)" in withExpectations {
    intFunMock.expects(42).twice()

    intFunMock(42)
    intFunMock(42)
    intercept[ExpectationException] { intFunMock(42) }
  }

  it should "fail if a method isn't called often enough (twice)" in {
    intercept[ExpectationException] {
      withExpectations {
        intFunMock.expects(42).twice()
        intFunMock(42)
      }
    }
  }

  it should "fail if a method is called too often (twice)" in withExpectations {
    intFunMock.expects(42).twice()

    intFunMock(42)
    intFunMock(42)
    intercept[ExpectationException] { intFunMock(42) }
  }

  it should "handle noMoreThanTwice call count (zero)" in withExpectations {
    intFunMock.expects(2).noMoreThanTwice()
  }

  it should "handle noMoreThanTwice call count (one)" in withExpectations {
    intFunMock.expects(2).noMoreThanTwice()
    intFunMock(2)
  }

  it should "handle noMoreThanTwice call count (two)" in withExpectations {
    intFunMock.expects(2).noMoreThanTwice()
    intFunMock(2)
    intFunMock(2)
  }

  it should "handle noMoreThanTwice call count (three)" in withExpectations {
    intFunMock.expects(2).noMoreThanTwice()
    intFunMock(2)
    intFunMock(2)
    intercept[ExpectationException] { intFunMock(42) }
  }

  it should "treat stubs as syntactic sugar for anyNumberOfTimes" in withExpectations {
    intFunMock.stubs(*).returning("a return value")

    assertResult("a return value") { intFunMock(1) }
    assertResult("a return value") { intFunMock(2) }
    assertResult("a return value") { intFunMock(3) }
  }

  it should "handle never call count (zero)" in withExpectations {
    intFunMock.expects(2).never()
  }

  it should "handle never call count (one)" in withExpectations {
    intFunMock.expects(2).never()
    intercept[ExpectationException] { intFunMock(2) }
  }

  it should "handle repeated(3).times call count (3)" in withExpectations {
    intFunMock.expects(2).repeated(3).times()

    intFunMock(2)
    intFunMock(2)
    intFunMock(2)
  }

  it should "handle repeat(1 to 2) call count (0)" in {
    intercept[ExpectationException] {
      withExpectations {
        intFunMock.expects(2).repeat(1 to 2)
      }
    }
  }

  it should "handle repeat(1 to 2) call count (1)" in withExpectations {
    intFunMock.expects(2).repeat(1 to 2)
    intFunMock(2)
  }

  it should "handle repeat(1 to 2) call count (2)" in withExpectations {
    intFunMock.expects(2).repeat(1 to 2)
    intFunMock(2)
    intFunMock(2)
  }

  it should "handle repeat(1 to 2) call count (3)" in withExpectations {
    intFunMock.expects(2).repeat(1 to 2)
    intFunMock(2)
    intFunMock(2)
    intercept[ExpectationException] { intFunMock(2) }
  }

  it should "handle repeat(2) call count (2)" in withExpectations {
    intFunMock.expects(2).repeat(2)
    intFunMock(2)
    intFunMock(2)
  }

  override def newInstance = new CallCountTest
}


