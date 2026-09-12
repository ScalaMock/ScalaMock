// Copyright (c) 2011-2025 ScalaMock Contributors (https://github.com/paulbutcher/ScalaMock/graphs/contributors)
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

package com.paulbutcher.test.features

import com.paulbutcher.test.AsyncIsolatedSpec
import org.scalatest.Assertion

import scala.concurrent.Future
import scala.language.implicitConversions

class AsyncOrderingTest extends AsyncIsolatedSpec {

  autoVerify = false

  val intFunMock = mockFunction[Int, Int]
  val stringFunMock = mockFunction[String, Int]

  behavior of "Mock function"

  it should "accept calls in any order by default" in withExpectations {
    intFunMock.expects(1).returning(1)
    intFunMock.expects(2).returning(2)

    Future {
      intFunMock(2) shouldBe 2
      intFunMock(1) shouldBe 1
    }
  }

  it should "accept calls in any order when inAnyOrder" in withExpectations {
    inAnyOrder {
      intFunMock.expects(1).returning(1)
      intFunMock.expects(2).returning(2)
    }

    Future {
      intFunMock(2) shouldBe 2
      intFunMock(1) shouldBe 1
    }
  }

  it should "handle a sequence of calls" in withExpectations {
    inSequence {
      intFunMock.expects(1).returning(1)
      intFunMock.expects(2).returning(2)
      intFunMock.expects(3).returning(3)
    }

    Future {
      intFunMock(1) shouldBe 1
      intFunMock(2) shouldBe 2
      intFunMock(3) shouldBe 3
    }
  }

  it should "handle a degenerate sequence" in withExpectations {
    inSequence {
      intFunMock.expects(42).returning(10)
    }

    Future {
      intFunMock(42) shouldBe 10
    }
  }

  trait Context {
    val result: Future[Assertion]
  }

  private implicit def contextResult(context: Context): Future[Assertion] = context.result

  abstract class InSequenceTest extends Context {
    def setupExpectations(): Unit = {
      inSequence {
        intFunMock.expects(1).returning(1).repeated(3 to 7)
        intFunMock.expects(2).returning(2).once()
        intFunMock.expects(3).returning(3).twice()
      }
    }
  }

  it should "handle a sequence of calls (call count)" in new InSequenceTest {
    val result = withExpectations {
      setupExpectations()

      Future {
        repeat(5) { intFunMock(1) shouldBe 1 }
        repeat(1) { intFunMock(2) shouldBe 2 }
        repeat(2) { intFunMock(3) shouldBe 3 }
        succeed
      }
    }
  }

  it should "fail if the entire sequence isn't called (none)" in new InSequenceTest {
    val result = demandExpectationException {
      setupExpectations()

      Future.successful(succeed)
    }
  }

  it should "fail if the entire sequence isn't called (1)" in new InSequenceTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        repeat(5) { intFunMock(1) shouldBe 1 }
        succeed
      }
    }
  }

  it should "fail if the entire sequence isn't called (1,2)" in new InSequenceTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        repeat(5) { assertResult(10) { intFunMock(1) } }
        repeat(1) { assertResult(11) { intFunMock(2) } }
        succeed
      }
    }
  }

  it should "fail if the entire sequence isn't called (2,3)" in new InSequenceTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        repeat(1) { intFunMock(2) shouldBe 2 }
        repeat(2) { intFunMock(3) shouldBe 3 }
        succeed
      }
    }
  }

  it should "fail if the entire sequence isn't called (1,3)" in new InSequenceTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        repeat(5) { intFunMock(1) shouldBe 1 }
        repeat(2) { intFunMock(3) shouldBe 3 }
        succeed
      }
    }
  }

  it should "fail if the sequence is called out of order (1,3,2)" in new InSequenceTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        repeat(5) { intFunMock(1) shouldBe 1 }
        repeat(2) { intFunMock(3) shouldBe 3 }
        repeat(1) { intFunMock(2) shouldBe 2 }
        succeed
      }
    }
  }

  it should "not match a previous item in the sequence" in withExpectations {
    inSequence {
      intFunMock.expects(1).returning(1).anyNumberOfTimes()
      intFunMock.expects(2).returning(2).once()
    }

    Future {
      intFunMock(1) shouldBe 1
      intFunMock(2) shouldBe 2
      intercept[ExpectationException] { intFunMock(1) }
      succeed
    }
  }

  it should "fail if unexpected call is made" in {
    demandExpectationException {
      inSequence {
        intFunMock.expects(1).returning(1).anyNumberOfTimes()
        intFunMock.expects(2).returning(2).once()
      }

      Future {
        intFunMock(1) shouldBe 1
        intFunMock(3) // TODO exception should be thrown here
        succeed
      }
    }
  }

  abstract class InSequenceMultipleMocksTest extends Context {
    val m1 = mockFunction[Int, Int]
    val m2 = mockFunction[Int, Int]
    val m3 = mockFunction[Int, Int]

    def setupExpectations(): Unit = {
      inSequence {
        m1.expects(1).returning(1)
        m2.expects(2).returning(2)
        m3.expects(3).returning(3)
      }
    }
  }

  it should "handle a sequence of calls (multiple mocks)" in new InSequenceMultipleMocksTest {
    val result = withExpectations {
      setupExpectations()

      Future {
        m1(1) shouldBe 1
        m2(2) shouldBe 2
        m3(3) shouldBe 3
        succeed
      }
    }
  }

  it should "fail if the sequence is called out of order (1,3,2) - multiple mocks" in new InSequenceMultipleMocksTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        m1(1) shouldBe 1
        m3(3) shouldBe 3
        m2(2) shouldBe 2
        succeed
      }
    }
  }

  it should "fail if the entire sequence isn't called (1,2) - multiple mocks" in new InSequenceMultipleMocksTest {
    val result = demandExpectationException {
      setupExpectations()

      Future {
        m1(1) shouldBe 1
        m2(2) shouldBe 2
        succeed
      }
    }
  }

  it should "handle a combination of ordered and unordered expectations" in withExpectations {
    val m = mockFunction[Int, Unit]

    m.expects(1)
    inSequence {
      m.expects(11)
      m.expects(12)
      m.expects(13)
    }
    m.expects(21)
    inSequence {
      m.expects(31)
      m.expects(32)
    }
    m.expects(41)

    Future {
      m(21)
      m(31)
      m(11)
      m(12)
      m(1)
      m(32)
      m(41)
      m(13)
      succeed
    }
  }

  it should "handle a sequence in which functions are called zero times" in withExpectations {
    val m = mockFunction[Int, Unit]
    inSequence {
      m.expects(1).once()
      m.expects(2).never()
      m.expects(3).anyNumberOfTimes()
      m.expects(4).once()
    }

    Future {
      m(1)
      m(4)
      succeed
    }
  }

  abstract class NestedExpectationsTest extends Context {
    val m = mockFunction[String, Unit]

    def setupExpectations(): Unit = {
      m.expects("1")
      inSequence {
        m.expects("2.1")
        inAnyOrder {
          m.expects("2.2.1")
          inSequence {
            m.expects("2.2.2.1")
            m.expects("2.2.2.2")
          }
          m.expects("2.2.3").anyNumberOfTimes()
        }
        m.expects("2.3")
      }
      m.expects("3")
    }
  }

  it should "handle valid deeply nested expectation contexts (1)" in new NestedExpectationsTest {
    val result = withExpectations {
      setupExpectations()
      Future {
        m("2.1")
        m("1")
        m("2.2.3")
        m("2.2.2.1")
        m("2.2.2.2")
        m("2.2.1")
        m("3")
        m("2.2.3")
        m("2.3")
        succeed
      }
    }
  }

  it should "handle valid deeply nested expectation contexts (2)" in new NestedExpectationsTest {
    val result = withExpectations {
      setupExpectations()
      Future {
        m("2.1")
        m("1")
        m("2.2.3")
        m("2.2.2.1")
        m("2.2.1")
        m("2.2.2.2")
        m("3")
        m("2.2.3")
        m("2.3")
        succeed
      }
    }
  }

  it should "handle valid deeply nested expectation contexts (3)" in new NestedExpectationsTest {
    val result = withExpectations {
      setupExpectations()
      Future {
        m("2.1")
        m("1")
        m("2.2.3")
        m("2.2.2.1")
        m("2.2.2.2")
        m("2.2.1")
        m("3")
        m("2.3")
        succeed
      }
    }
  }

  it should "handle invalid deeply nested expectation contexts" in {
    demandExpectationException {
      val m = mockFunction[String, Unit]

      m.expects("1")
      inSequence {
        m.expects("2.1")
        inAnyOrder {
          m.expects("2.2.1")
          inSequence {
            m.expects("2.2.2.1")
            m.expects("2.2.2.2")
          }
          m.expects("2.2.3")
        }
        m.expects("2.3")
      }
      m.expects("3")

      Future {
        m("2.1")
        m("1")
        m("2.2.1")
        m("2.2.2.1")
        m("2.2.2.2")
        m("2.3") // 2.3 before 2.2.3
        m("2.2.3")
        m("3")
        succeed
      }
    }
  }

  it should "handle invalid deeply nested expectation contexts (2)" in {
    demandExpectationException {
      inSequence {
        inAnyOrder {
          stringFunMock.expects("1.1")
          stringFunMock.expects("1.2")
        }
        stringFunMock.expects("2")
      }

      Future {
        stringFunMock("1.1")
        stringFunMock("2")
        stringFunMock("1.2")
        succeed
      }
    }
  }

  override def newInstance = new AsyncOrderingTest
}
