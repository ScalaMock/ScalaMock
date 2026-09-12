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

import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalamock.scalatest.{MockFactory, AsyncMockFactory}

/**
 *  Tests for issue #371
 */
class AsyncSyncMixinTest extends AnyFlatSpec {

  "MockFactory" should "be mixed only with Any*Spec and not Async*Spec traits" in {
    assertCompiles("new AnyFlatSpec with MockFactory")

    //! FIXME //github.com/scalatest/scalatest/issues/2283
    //assertDoesNotCompile("new AsyncFlatSpec with MockFactory")
  }

  "AsyncMockFactory" should "be mixed only with Async*Spec and not Any*Spec traits" in {
    assertCompiles("new AsyncFlatSpec with AsyncMockFactory")
    //! FIXME //github.com/scalatest/scalatest/issues/2283
    //assertDoesNotCompile("new AnyFlatSpec with AsyncMockFactory")
  }

}
