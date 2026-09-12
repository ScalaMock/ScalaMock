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
import org.scalamock.clazz.Mock
import org.scalatest.exceptions.{StackDepthException, TestFailedException}
import org.scalatest.{Suite, SuiteMixin}

/**
 * Trait that can be mixed into org.scalatest.path.* specs (e.g. path.FunSpec).
 *
 * MockFactory cannot be used because it overrides a final method. If you use path.FunSpec you need to verify
 * expectations yourself - just add verifyExpectations at the end of the root-level suite.
 *
 * See [[MockFactory]] for more information.
 */
trait PathMockFactory extends SuiteMixin with MockFactoryBase with Mock { this: Suite =>
  
  type ExpectationException = TestFailedException

  protected def newExpectationException(message: String, methodName: Option[Symbol]) =
    new TestFailedException((_: StackDepthException) => Some(message), None, failedCodeStackDepthFn(methodName))

  /**
   * Verify all expectations.
   */
  protected def verifyExpectations(): Unit = withExpectations(())
}
