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

package com.paulbutcher.test.proxy

import com.paulbutcher.test.ManyParamsTrait
import org.scalamock.scalatest.proxy.MockFactory
import org.scalatest.freespec.AnyFreeSpec

@deprecated(message = "proxy mocks are deprecated")
class ProxyMockManyParamsTest extends AnyFreeSpec with MockFactory {

  autoVerify = false

  "Mocking trait should" - {
    "allow expectations to be set on methods with many parameters" in withExpectations {
      val m = mock[ManyParamsTrait]

      m.expects(Symbol("methodWith1Ints"))(1).returning(99)
      assertResult(99) { m.methodWith1Ints(1) }

      m.expects(Symbol("methodWith2Ints"))(1,1).returning(98)
      assertResult(98) { m.methodWith2Ints(1,1) }

      m.expects(Symbol("methodWith3Ints"))(1,1,1).returning(97)
      assertResult(97) { m.methodWith3Ints(1,1,1) }

      m.expects(Symbol("methodWith4Ints"))(1,1,1,1).returning(96)
      assertResult(96) { m.methodWith4Ints(1,1,1,1) }

      m.expects(Symbol("methodWith5Ints"))(1,1,1,1,1).returning(95)
      assertResult(95) { m.methodWith5Ints(1,1,1,1,1) }

      m.expects(Symbol("methodWith6Ints"))(1,1,1,1,1,1).returning(94)
      assertResult(94) { m.methodWith6Ints(1,1,1,1,1,1) }

      m.expects(Symbol("methodWith7Ints"))(1,1,1,1,1,1,1).returning(93)
      assertResult(93) { m.methodWith7Ints(1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith8Ints"))(1,1,1,1,1,1,1,1).returning(92)
      assertResult(92) { m.methodWith8Ints(1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith9Ints"))(1,1,1,1,1,1,1,1,1).returning(91)
      assertResult(91) { m.methodWith9Ints(1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith10Ints"))(1,1,1,1,1,1,1,1,1,1).returning(90)
      assertResult(90) { m.methodWith10Ints(1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith11Ints"))(1,1,1,1,1,1,1,1,1,1,1).returning(89)
      assertResult(89) { m.methodWith11Ints(1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith12Ints"))(1,1,1,1,1,1,1,1,1,1,1,1).returning(88)
      assertResult(88) { m.methodWith12Ints(1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith13Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1).returning(87)
      assertResult(87) { m.methodWith13Ints(1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith14Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(86)
      assertResult(86) { m.methodWith14Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith15Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(85)
      assertResult(85) { m.methodWith15Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith16Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(84)
      assertResult(84) { m.methodWith16Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith17Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(83)
      assertResult(83) { m.methodWith17Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith18Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(82)
      assertResult(82) { m.methodWith18Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith19Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(81)
      assertResult(81) { m.methodWith19Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith20Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(80)
      assertResult(80) { m.methodWith20Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith21Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(79)
      assertResult(79) { m.methodWith21Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }

      m.expects(Symbol("methodWith22Ints"))(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1).returning(78)
      assertResult(78) { m.methodWith22Ints(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1) }
    }
  }
}
