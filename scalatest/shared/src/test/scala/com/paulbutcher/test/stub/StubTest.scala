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

package com.paulbutcher.test.stub

import com.paulbutcher.test.{IsolatedSpec, TestTrait}

class StubTest extends IsolatedSpec {
  autoVerify = false
  val m = stub[TestTrait]

  behavior of "Stub"

  it can "have its methods called any number of times" in withExpectations {
    m.oneParam(42)
    m.oneParam(42)
    m.twoParams(1, 1.23)
  }

  it should "return null unless told otherwise" in withExpectations {
    m.oneParam(42) shouldBe null
  }

  it should "return what they're told to" in withExpectations {
    (m.twoParams _).when(42, 1.23).returns("a return value")
    m.twoParams(42, 1.23) shouldBe "a return value"
  }

  it should "handle chained expectations" in {
    (m.oneParam _).when(*).returns("1").twice()
    (m.oneParam _).when(*).returns("2").once()

    m.oneParam(1) shouldBe "1"
    m.oneParam(2) shouldBe "1"
    m.oneParam(2) shouldBe "2"
    m.oneParam(2) shouldBe null
  }

  it should "verify calls" in withExpectations {
    m.twoParams(42, 1.23)
    m.twoParams(42, 1.23)
    (m.twoParams _).verify(42, 1.23).twice()
  }

  it should "fail when verification fails because of parameter mismatch" in {
    demandExpectationException {
      m.twoParams(42, 1.00)
      (m.twoParams _).verify(42, 1.23).once()
    }
  }

  it should "fail when verification fails because of unexpected call count" in {
    demandExpectationException {
      m.twoParams(42, 1.23)
      m.twoParams(42, 1.23)
      (m.twoParams _).verify(42, 1.23).once()
    }
  }
  override def newInstance = new StubTest
}
