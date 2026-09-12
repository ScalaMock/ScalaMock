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

package org.scalamock.scalatest

import org.scalamock.MockFactoryBase
import org.scalatest.exceptions.{StackDepthException, TestFailedException}
import org.scalatest._

trait AbstractMockFactory extends TestSuiteMixin with MockFactoryBase { this: TestSuite =>

  type ExpectationException = TestFailedException

  abstract override def withFixture(test: NoArgTest): Outcome = {

    if (autoVerify) {
      withExpectations {
        val outcome = super.withFixture(test)
        outcome match {
          case Failed(throwable) =>
            // MockFactoryBase does not know how to handle ScalaTest Outcome.
            // Throw error that caused test failure to prevent hiding it by
            // "unsatisfied expectation" exception (see issue #72)
            throw throwable
          case _ => outcome
        }
      }
    } else {
      super.withFixture(test)
    }
  }

  protected def newExpectationException(message: String, methodName: Option[Symbol]) =
    new TestFailedException((_: StackDepthException) => Some(message), None, failedCodeStackDepthFn(methodName))

  protected var autoVerify = true
}
