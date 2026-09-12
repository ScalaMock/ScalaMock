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

import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec

class StubFunctionTest extends AnyFreeSpec with MockFactory {
  
  autoVerify = false
  
  def repeat(n: Int)(what: => Unit): Unit = {
    for (i <- 0 until n)
      what
  }
  
  case class TestException() extends RuntimeException

  "Stub functions should" - {

    "return null by default" in {
      withExpectations {
        val m = stubFunction[String]
        assertResult(null) { m() }
      }
    }
    
    "return a null-like default value for non reference types" in {
      withExpectations {
        val m = stubFunction[Int]
        assertResult(0) { m() }
      }
    }
    
    "return what they're told to" in {
      withExpectations {
        val m = stubFunction[String]
        m.when().returns("a return value")
        assertResult("a return value") { m() }
      }
    }
    
    "throw what they're told to" in {
      withExpectations {
        val m = stubFunction[String]
        m.when().throws(new TestException)
        intercept[TestException]{ m() }
      }
    }
    
    "default to anyNumberOfTimes" in {
      withExpectations {
        val m = stubFunction[String]
        m.when().returns("a return value")
        assertResult("a return value") { m() }
        assertResult("a return value") { m() }
        assertResult("a return value") { m() }
      }
    }
    
    "unless told otherwise" in {
      withExpectations {
        val m = stubFunction[String]
        m.when().returns("a return value").twice()
        assertResult("a return value") { m() }
        assertResult("a return value") { m() }
        assertResult(null) { m() }
      }
    }
    
    "match literal arguments" in {
      withExpectations {
        val m = stubFunction[String, Int, Int]
        m("foo", 42)
        m.verify("foo", 42)
      }
    }
    
    "match wildcard arguments" in {
      withExpectations {
        val m = stubFunction[String, Int, Int]
        m("foo", 42)
        m.verify(*, 42)
      }
    }
    
    "match epsilon arguments" in {
      withExpectations {
        val m = stubFunction[String, Double, Int]
        m("foo", 1.0001)
        m.verify("foo", ~1.0)
      }
    }

    "fail if an expectation is not met" in {
      intercept[ExpectationException](withExpectations {
        val m = stubFunction[String, Int, Int]
        m.verify("foo", 42)
      })
    }

    "fail if a method isn't called often enough" in {
      intercept[ExpectationException](withExpectations {
        val m = stubFunction[String, Int, Int]
        m("foo", 42)
        m.verify("foo", 42).twice()
      })
    }
      
    "fail if a method is called too often" in {
      intercept[ExpectationException](withExpectations {
        val m = stubFunction[String, Int, Int]
        m("foo", 42)
        m("foo", 42)
        m("foo", 42)
        m.verify("foo", 42).twice()
      })
    }
    
    "match arguments" - {
      "when stubbing" in {
        withExpectations {
          val m = stubFunction[Int, Int, String]
          m.when(where { _ < _ }).returns("lower")
          m.when(where { _ > _ }).returns("higher")
          assertResult("lower"){ m(1, 2) }
          assertResult("higher"){ m(2, 1) }
        }
      }
      
      "when verifying" in {
        withExpectations {
          val m = stubFunction[Int, Int, String]
          m(1, 2)
          m(2, 1)
          m(2, 1)
          m.verify(where { _ < _}).once()
          m.verify(where { _ > _}).twice()
        }
      }
    }
    
    "handle a degenerate sequence" in {
      withExpectations {
        val m = stubFunction[Int, Int]
        m(42)
        inSequence {
          m.verify(42)
        }
      }
    }
    
    "handle a sequence of calls" in {
      withExpectations {
        val m = stubFunction[Int, Int]
        repeat(5) { m(42) }
        repeat(1) { m(43) }
        repeat(2) { m(44) }
        inSequence {
          m.verify(42).repeated(3 to 7)
          m.verify(43).once()
          m.verify(44).twice()
        }
      }
    }
    
    "fail if functions are called out of sequence" in {
      intercept[ExpectationException](withExpectations {
        val m = stubFunction[Int, Int]
        repeat(5) { m(42) }
        m(44)
        inSequence {
          m.verify(42).repeated(3 to 7)
          m.verify(43).once()
          m.verify(44).twice()
        }
      })
    }
    
    "fail if the entire sequence isn't called" in {
      intercept[ExpectationException](withExpectations {
        val m = stubFunction[Int, Int]
        repeat(5) { m(42) }
        repeat(1) { m(43) }
        inSequence {
          m.verify(42).repeated(3 to 7)
          m.verify(43).once()
          m.verify(44).twice()
        }
      })
    }
    
    "not match a previous item in the sequence" in {
      withExpectations {
        val m = stubFunction[Int, Int]
        m(42)
        m(43)
        m(42)
        inSequence {
          m.verify(42).anyNumberOfTimes()
          m.verify(43)
          m.verify(42)
        }
      }
    }

    "handle a combination of ordered and unordered expectations" in {
      withExpectations {
        val m = stubFunction[Int, Unit]
        
        m(21)
        m(31)
        m(11)
        m(12)
        m(1)
        m(32)
        m(41)
        m(13)
  
        m.verify(1)
        inSequence {
          m.verify(11)
          m.verify(12)
          m.verify(13)
        }
        m.verify(21)
        inSequence {
          m.verify(31)
          m.verify(32)
        }
        m.verify(41)
      }      
    }

    "handle a sequence in which functions are called zero times" in {
      withExpectations {
        val m = stubFunction[Int, Unit]
        m(1)
        m(4)
        inSequence {
          m.verify(1).once()
          m.verify(2).never()
          m.verify(3).anyNumberOfTimes()
          m.verify(4).once()
        }
      }
    }

    "handle valid deeply nested expectation contexts" in {
      withExpectations {
        val m = stubFunction[String, Unit]
        
        m("2.1")
        m("1")
        m("2.2.3")
        m("2.2.2.1")
        m("2.2.2.2")
        m("2.2.1")
        m("3")
        m("2.2.3")
        m("2.3")
        
        m.verify("1")
        inSequence {
          m.verify("2.1")
          inAnyOrder {
            m.verify("2.2.1")
            inSequence {
              m.verify("2.2.2.1")
              m.verify("2.2.2.2")
            }
            m.verify("2.2.3").anyNumberOfTimes()
          }
          m.verify("2.3")
        }
        m.verify("3")
      }      
    }

    "handle invalid deeply nested expectation contexts" in {
      intercept[ExpectationException](withExpectations {
        val m = stubFunction[String, Unit]
        
        m("2.1")
        m("1")
        m("2.2.3")
        m("2.2.2.2")
  
        m.verify("1")
        inSequence {
          m.verify("2.1")
          inAnyOrder {
            m.verify("2.2.1")
            inSequence {
              m.verify("2.2.2.1")
              m.verify("2.2.2.2")
            }
            m.verify("2.2.3")
          }
          m.verify("2.3")
        }
        m.verify("3")
      })    
    }
    
    "cope with multiple stubs" in {
      withExpectations {
        val m1 = stubFunction[Int, String]
        val m2 = stubFunction[Int, String]
        
        m1.when(42).returns("m1")
        m2.when(42).returns("m2")
        
        assertResult("m1") { m1(42) }
        assertResult("m2") { m2(42) }
        
        m1.verify(42).once()
        m2.verify(42).once()
      }      
    }
  }
}
