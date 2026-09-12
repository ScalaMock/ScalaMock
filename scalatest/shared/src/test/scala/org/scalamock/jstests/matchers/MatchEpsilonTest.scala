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

package org.scalamock.jstests.matchers

import org.scalamock.matchers.MatchEpsilon
import org.scalatest.freespec.AnyFreeSpec

class MatchEpsilonTest extends AnyFreeSpec {

  "MatchEpsilon should" - {
    "match anything that's close to the given value" in {
      assert(new MatchEpsilon(1.0).equals(1.0))
      assert(new MatchEpsilon(1.0).equals(1.0f))
      assert(new MatchEpsilon(1.0).equals(1.0001))
      assert(new MatchEpsilon(1.0).equals(1.0001f))
      assert(new MatchEpsilon(1.0).equals(1))
    }
    
    "not match anything that's not close enough" in {
      assert(!(new MatchEpsilon(1.0).equals(1.1)))
      assert(!(new MatchEpsilon(1.0).equals(0.9)))
    }
    
    "only match numbers" in {
      assert(!(new MatchEpsilon(1.0).equals("foo")))
    }
  }
}
