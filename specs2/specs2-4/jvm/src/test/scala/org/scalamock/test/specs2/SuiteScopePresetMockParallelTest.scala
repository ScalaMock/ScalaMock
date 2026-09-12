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

import org.scalamock.specs2.IsolatedMockFactory
import org.scalamock.test.mockable.TestTrait
import org.specs2.mutable.Specification

/**
 *  Tests for mocks defined in suite scope (i.e. outside test case scope) with predefined expectations
 *
 *  Tests for issue #25
 */
class SuiteScopePresetMockParallelTest extends Specification with IsolatedMockFactory {
  // please note that this test suite runs in isolated mode

  val mockWithExpectationsPredefined = mock[TestTrait]
  (mockWithExpectationsPredefined.oneParamMethod _).expects(0).returning("predefined")

  "Specs2 suite" should {
    "allow to use mock defined suite scope with predefined expectations" in {
      (mockWithExpectationsPredefined.oneParamMethod _).expects(1).returning("one")

      mockWithExpectationsPredefined.oneParamMethod(0) must_== "predefined"
      mockWithExpectationsPredefined.oneParamMethod(1) must_== "one"
    }

    "keep predefined mock expectations" in {
      (mockWithExpectationsPredefined.oneParamMethod _).expects(2).returning("two")

      mockWithExpectationsPredefined.oneParamMethod(0) must_== "predefined"
      mockWithExpectationsPredefined.oneParamMethod(2) must_== "two"
    }
  }
}
