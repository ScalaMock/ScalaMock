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

import org.scalamock.scalatest.proxy.MockFactory
import org.scalamock.test.mockable.TestTrait
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *  Tests for proxy mocks defined in suite scope (i.e. outside test case scope)
 *
 *  Test for issue #35
 */
@deprecated(message = "proxy mocks are deprecated")
class SuiteScopeProxyMockTest extends AnyFlatSpec with Matchers with OneInstancePerTest with MockFactory {
  // please note that this test suite mixes in OneInstancePerTest trait

  override def newInstance = new SuiteScopeProxyMockTest

  val mockWithoutExpectationsPredefined = mock[TestTrait]

  "ScalaTest suite" should "allow to use mock defined suite scope" in {
    mockWithoutExpectationsPredefined.expects(Symbol("oneParamMethod"))(1).returning("one")
    mockWithoutExpectationsPredefined.expects(Symbol("oneParamMethod"))(2).returning("two")

    mockWithoutExpectationsPredefined.oneParamMethod(1) shouldBe "one"
    mockWithoutExpectationsPredefined.oneParamMethod(2) shouldBe "two"
  }

  it should "allow to use mock defined suite scope in more than one test case" in {
    mockWithoutExpectationsPredefined.expects(Symbol("oneParamMethod"))(3).returning("three")

    mockWithoutExpectationsPredefined.oneParamMethod(3) shouldBe "three"
  }
}
