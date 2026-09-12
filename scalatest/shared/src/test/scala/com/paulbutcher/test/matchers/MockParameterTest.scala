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

package com.paulbutcher.test.matchers

import org.scalamock.matchers.{MatchAny, MatchEpsilon, MockParameter}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MockParameterTest extends AnyFreeSpec with Matchers {
  
  "A mock parameter should" - {
    "be equal" - {
      "if its value is equal" in {
        new MockParameter(42) shouldEqual 42
      }
    
      "with a wildcard" in {
        new MockParameter[Int](new MatchAny) shouldEqual 123
      }
    
      "with an epsilon" in {
        new MockParameter[Double](new MatchEpsilon(1.0)) shouldEqual 1.0001
      }
    }
    
    "not be equal" - {
      "with different values" in {
        new MockParameter(42) should not be 43
      }
      
      "with different types" in {
       new MockParameter(42) should not be "forty two"
      }
    }
  }
  
  "A product of mock parameters should" - {
    "compare correctly to a product of non mock parameters" in {
      val p1 = (new MockParameter(42), new MockParameter[String](new MatchAny), new MockParameter[Double](new MatchEpsilon(1.0)))
      val p2 = (42, "foo", 1.0001)
      assert(p1 === p2)
    }
  }
}
