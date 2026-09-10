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

import kyo.test.TestAspect.{after, before, checks, sequential}
import kyo.{Chunk, Trace, Kyo, KyoAspect}
import kyo.test.{TestAspectAtLeastR, TestEnvironment, KyoSpecDefault}

private[kyotest] trait ScalamockKyoSpecSetup extends KyoSpecDefault {

  // protected so that LayeredMockMacros, which is called from user code, can access it
  protected val unsafeMockFactory: KyoMockContext = new KyoMockContext.Live

  private val initAspect = before(unsafeMockFactory.initializeExpectations())

  // have to use orDie here
  // `def aspects` should return: TestAspect[..., LowerE = Nothing, UpperE = Any]
  // but without orDie it returns TestAspect[..., LowerE = ExpectationException, UpperE = Nothing]
  // and LowerE is covariant
  private val verifyAspect = after(unsafeMockFactory.verifyExpectations().orDie)

  // for property-based tests: verify mocks after each sample
  private val verifyAndCleanEverySample = checks {
    new KyoAspect[Nothing, Any, Nothing, Any, Nothing, Any] { kyo =>
      def apply[R, E, A](kyo: Kyo[R, E, A])(implicit trace: Trace): Kyo[R, E, A] =
        kyo.ensuring {
          unsafeMockFactory.verifyExpectations().orDie.ensuring {
            // prepare for the next sample
            unsafeMockFactory.initializeExpectations()
          }
        }
    }
  }

  // if you change the list of aspects here, make sure to update scalamockInternalSpecAspects accordingly!
  override def aspects: Chunk[TestAspectAtLeastR[TestEnvironment]] =
    super.aspects ++ Chunk(initAspect, verifyAspect, verifyAndCleanEverySample, sequential)

  // for scalamock's own tests
  protected[kyotest] def scalamockInternalSpecAspects: Chunk[TestAspectAtLeastR[TestEnvironment]] =
    super.aspects ++ Chunk(initAspect, sequential)
}
