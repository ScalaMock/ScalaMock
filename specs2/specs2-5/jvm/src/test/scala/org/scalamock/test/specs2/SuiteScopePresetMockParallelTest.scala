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

package org.scalamock.test.specs2

import org.scalamock.specs2.MockContext
import org.scalamock.test.mockable.TestTrait
import org.specs2.mutable.Specification

/**
 *  Tests for mocks defined with predefined expectations, reused across several test cases.
 *
 *  specs2 5.x removed isolated specifications, so a mock defined once in ''suite'' scope
 *  (outside any example) can no longer have its predefined expectations survive from one
 *  example into the next - the first example to run would consume/verify them. Reapplying the
 *  predefined expectations per example via a ''fixture context'' (see [[org.scalamock.specs2.MockContext]])
 *  gives every example its own fresh mock while still reusing the same expectation setup, and
 *  keeps examples safe to run in parallel.
 *
 *  Tests for issue #25
 */
class SuiteScopePresetMockParallelTest extends Specification {

  trait TestSetupWithExpectationsPredefined extends MockContext {
    val mockWithExpectationsPredefined = mock[TestTrait]
    (mockWithExpectationsPredefined.oneParamMethod _).expects(0).returning("predefined")
  }

  "Specs2 suite" should {
    "allow to use mock defined with predefined expectations" in new TestSetupWithExpectationsPredefined {
      (mockWithExpectationsPredefined.oneParamMethod _).expects(1).returning("one")

      mockWithExpectationsPredefined.oneParamMethod(0) === "predefined"
      mockWithExpectationsPredefined.oneParamMethod(1) === "one"
    }

    "keep predefined mock expectations" in new TestSetupWithExpectationsPredefined {
      (mockWithExpectationsPredefined.oneParamMethod _).expects(2).returning("two")

      mockWithExpectationsPredefined.oneParamMethod(0) === "predefined"
      mockWithExpectationsPredefined.oneParamMethod(2) === "two"
    }
  }
}
