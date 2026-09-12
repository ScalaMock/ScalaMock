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

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec

class MockFunctionTest extends AnyFreeSpec with MockFactory {
  
  autoVerify = false
  
  def repeat(n: Int)(what: => Unit): Unit = {
    for (i <- 0 until n)
      what
  }
  
  "Mock functions should" - {

    "match literal arguments" in {
      withExpectations {
        val m = mockFunction[String, Int, Int]
        m.expects("foo", 42)
        m("foo", 42)
      }
    }
    
    "match wildcard arguments" in {
      withExpectations {
        val m = mockFunction[String, Int, Int]
        m.expects(*, 42)
        m("foo", 42)
      }
    }
    
    "match epsilon arguments" in {
      withExpectations {
        val m = mockFunction[String, Double, Int]
        m.expects("foo", ~1.0)
        m("foo", 1.0001)
      }
    }
    
    "fail if an expectation is not met" in {
      intercept[ExpectationException](withExpectations {
        val m = mockFunction[String, Int, Int]
        m.expects("foo", 42)
      })
    }
    
    "match arguments" in {
      withExpectations {
        val m = mockFunction[Int, Int, String]
        m.expects(where { _ < _ }).returning("less")
        m.expects(where { _ > _ }).returning("more")
        assertResult("less"){ m(1, 2) }
        assertResult("more"){ m(2, 1) }
      }
    }
  }
}
