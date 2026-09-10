// Copyright (c) 2011-2025 ScalaMock Contributors (https://github.com/ScalaMock/ScalaMock/graphs/contributors)
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

package org.scalamock.kyotest.internal

import org.scalamock.kyotest.ExpectationException
import org.scalamock.matchers.{Matchers, MockParameter}

import scala.reflect.ClassTag

private[kyotest] trait KyoAssertionSupport { self: Matchers =>

  def renderKyoAssertion(failure: TestTrace[Boolean]): String =
    ConsoleRenderer
      .renderToStringLines(ConsoleRenderer.renderAssertionResult(failure, 0))
      .mkString(System.lineSeparator())

  private[kyotest] def assertionToMockParameter[A: ClassTag](
      assertion: Assertion[A],
      allowAnyOrder: Boolean
    )(implicit sourceLocation: SourceLocation): MockParameter[A] =
    if (assertion == Assertion.anything) {
      // for better readability
      // in mock logs we'll see explicit `*` instead of assertArg lambda
      // since Assertion.anything is static, this check is safe
      *
    } else if (allowAnyOrder) {
      argThat[A](assertion.render) { arg =>
        assertion.run(arg).failures.isEmpty
      }
    } else {
      argAssert[A](assertion.render) { arg =>
        assertion.run(arg).failures.foreach { failure =>
          throw new ExpectationException(
            self.renderKyoAssertion(failure)
          )
        }
      }
    }

  final class AllowAnyOrderSyntax[A: ClassTag](val assertion: Assertion[A]) {

    def allowUnorderedCalls: MockParameter[A] =
      assertionToMockParameter(assertion, allowAnyOrder = true)
  }
}
