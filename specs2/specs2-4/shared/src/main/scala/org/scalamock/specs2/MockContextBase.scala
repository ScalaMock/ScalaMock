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

package org.scalamock.specs2

import org.scalamock.MockFactoryBase
import org.specs2.execute.{AsResult, Failure, FailureException}

/**
 * Base trait for MockContext and IsolatedMockFactory
 *
 * @define techniques
 * To use ScalaMock in Specs2 tests you can either:
 *  - mix `Specification` with [[org.scalamock.specs2.IsolatedMockFactory]] to
 *    get '''isolated test cases''' or
 *  - run each test case in separate '''fixture context''' - [[org.scalamock.specs2.MockContext]]
 */
trait MockContextBase extends MockFactoryBase {

  type ExpectationException = FailureException

  protected override def newExpectationException(message: String, methodName: Option[Symbol]) =
    new ExpectationException(Failure(message))

  protected def wrapAsResult[T: AsResult](body: => T) = {
    AsResult(withExpectations { body })
  }
}
