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

package org.scalamock.kyotest.macros

import org.scalamock.kyotest.internal.ScalamockKyoSpecSetup
import org.scalamock.kyotest.ScalamockKyoSyntax

/**
 * Base trait for tests on kyo-test, using mocks.
 * 
 * Basic example:
 * 
 * {{{
 * object ExampleSpec extends ScalamockKyoSpec {
 *
 *   trait Service {
 *     def f(x: Int): UIO[String]
 *   }
 *
 *   override def spec: Spec[TestEnvironment, Any] =
 *     suite("ExampleSpec")(
 *       test("succeed if function is invoked as expected") {
 *         for {
 *           _ <- Kyo.serviceWith[Service] { mock =>
 *             (mock.f _).expects(42).returns(Kyo.succeed("-42"))
 *           }
 *           value <- Kyo.serviceWithKyo[Service](_.f(42))
 *         } yield assertTrue(value == "-42")
 *       },
 *     ).provide(mock[Service])
 * } 
 * }}}
 *
 * 
 */
trait ScalamockKyoSpec extends ScalamockKyoSpecSetup with ScalamockKyoSyntax {

  /**
   * Returns a layer for creating a mock, for testing in Expectations-First style.
   * https://scalamock.org/user-guide/mocking_style/
   *
   * Usually, it's needed to provide layers where the mock is needed.
   */
  inline def mock[A]: ULayer[A] = 
    LayeredMockMacros.mock[A](using unsafeMockFactory.mockContext)

  /**
   * Returns a layer for creating a mock with a name, for testing in Expectations-First style.
   * https://scalamock.org/user-guide/mocking_style/
   *
   * Usually, it's needed to provide layers where the mock is needed.
   * 
   * Can pass a name, if you want it to appear in error messages instead of mock-1.
   */
  inline def mock[A](name: String): ULayer[A] = 
    LayeredMockMacros.mockWithName[A](name)(using unsafeMockFactory.mockContext)

  /**
   * Returns a layer for creating a stub, for testing in Record-then-Verify style.
   * https://scalamock.org/user-guide/mocking_style/
   *
   * Usually, it's needed to provide layers where the stub is needed.
   */
  inline def stub[A]: ULayer[A] = 
    LayeredMockMacros.stub[A](using unsafeMockFactory.mockContext)

  /**
   * Returns a layer for creating a stub with a name, for testing in Record-then-Verify style.
   * https://scalamock.org/user-guide/mocking_style/
   *
   * Usually, it's needed to provide layers where the stub is needed.
   *
   * Can pass a name, if you want it to appear in error messages instead of mock-1.
   */
  inline def stub[A](name: String): ULayer[A] = 
    LayeredMockMacros.stubWithName[A](name)(using unsafeMockFactory.mockContext)

  /**
   * Documentation: https://scalamock.org/classic/features#ordering
   */
  override def inSequence[A](what: => A): A = {
    unsafeMockFactory.inSequence(what)
  }

  /**
   * Documentation: https://scalamock.org/classic/features#ordering
   */
  override def inAnyOrder[A](what: => A): A = {
    unsafeMockFactory.inAnyOrder(what)
  }
} 