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

import com.paulbutcher.test._
import org.scalamock.function.StubFunction0

class StubNamingTest extends IsolatedSpec {

  def getStubMethodName(method: StubFunction0[String]) = method.toString

  behavior of "Stub"

  it should "have a sensible method name when mocking a method without parameters" in {
    val myStub = stub[TestTrait]
    getStubMethodName(() => myStub.noParams()) shouldBe "<stub-1> TestTrait.noParams"
  }

  it can "be named using string literal" in {
    val myStub = stub[TestTrait]("stub name")
    getStubMethodName(() => myStub.noParams()) shouldBe "<stub name> TestTrait.noParams"
  }

  // NOTE: MockNamingTest contains more test cases related to mock naming

  override def newInstance = new StubNamingTest
}
