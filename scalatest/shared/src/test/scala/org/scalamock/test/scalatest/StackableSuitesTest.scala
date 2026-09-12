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

import org.scalamock.scalatest.MockFactory
import org.scalamock.test.mockable.TestTrait
import org.scalatest._
import org.scalatest.events.TestSucceeded
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class StackableSuitesTest extends AnyFlatSpec with Matchers with TestSuiteRunner {

  object EventLogger {
    var events: List[String] = List.empty
    def logEvent(event: String) = { events = events :+ event }
  }

  trait SuiteWrapper extends SuiteMixin with TestSuite {
    abstract override def withFixture(test: NoArgTest): Outcome = {
      EventLogger.logEvent("SuiteWrapper setup")
      val outcome = super.withFixture(test)
      EventLogger.logEvent("SuiteWrapper cleanup")
      outcome
    }
  }

  class TestedSuite extends AnyFunSuite with SuiteWrapper with MockFactory with Matchers {
    test("execute block of code") {
      val mockedTrait = mock[TestTrait]
      (mockedTrait.oneParamMethod _).expects(1).onCall { (arg: Int) =>
        EventLogger.logEvent("mock method called")
        "one"
      }

      mockedTrait.oneParamMethod(1) shouldBe "one"
    }
  }

  "ScalaTest suite" can "be mixed together with other traits which override withFixture" in {
    val outcome = runTestCase[TestedSuite](new TestedSuite)
    outcome shouldBe a[TestSucceeded]
    EventLogger.events shouldBe List(
      "SuiteWrapper setup",
      "mock method called",
      "SuiteWrapper cleanup")
  }

}
