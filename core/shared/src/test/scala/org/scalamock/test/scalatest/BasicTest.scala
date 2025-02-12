// Copyright (c) 2011-2025 ScalaMock Contributors (https://github.com/ScalaMock/ScalaMock/graphs/contributors)
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
import org.scalamock.test.mockable.TestTrait
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/**
 *  Tests for mock defined in test case scope
 *
 *  Tests for issue #25
 */
class BasicTest extends AnyFlatSpec with Matchers with MockFactory {

  "ScalaTest suite" should "allow to use mock defined in test case scope" in {
    val mockedTrait = mock[TestTrait]
    (mockedTrait.oneParamMethod).expects(1).returning("one")
    (mockedTrait.oneParamMethod).expects(2).returning("two")
    (() => mockedTrait.noParamMethod()).expects().returning("yey")

    mockedTrait.oneParamMethod(1) shouldBe "one"
    mockedTrait.oneParamMethod(2) shouldBe "two"
    mockedTrait.noParamMethod() shouldBe "yey"
  }

  it should "use separate call logs for each test case" in {
    val mockedTrait = mock[TestTrait]
    (mockedTrait.oneParamMethod).expects(3).returning("three")

     mockedTrait.oneParamMethod(3) shouldBe "three"
  }
}
