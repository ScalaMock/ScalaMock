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

import org.scalamock.context.MockContext

/**
 * Can simplify migration from kyo-mock to scalamock.
 * Not needed when writing tests on scalamock from scratch.
 */
trait SimplerKyoMockMigration {

  // for access to MockContext, in the implementation for Scala 3 it's needed
  this: ScalamockKyoSpec =>
 
  /**
   * Creates a layer that creates a new mock.
   *
   * WARN: use with caution, provide only one Layer of each type!
   *
   * In kyo-mock there's an implicit conversion from Expectation[A] to ULayer[A].
   * In scalamock, instead of Expectation, URIO[A, Any] can be used.
   * To avoid rewriting the passing of expectations to layers,
   * an analogous conversion is defined.
   *
   * It's not declared as implicit to avoid unnecessary bugs, it has to be called manually.
   *
   * WARN: when passing effects to layers carelessly, unexpected behavior can occur:
   * creation of mocks that we didn't want to create.
   *
   * Example:
   * {{{
   * test("creation of two mocks") {
   *   val layer1 = createMockedLayer(
   *     Kyo.serviceWith[Service] { mock =>
   *       (mock.f _).expects(42).returnsKyo("-42")
   *     }
   *   )
   *   val layer2 = createMockedLayer(
   *     Kyo.serviceWith[Service] { mock =>
   *       (mock.f _).expects(43).returnsKyo("-43")
   *     }
   *   )
   *   val effect = for {
   *     _ <- Kyo.serviceWith[Service] { mock =>
   *       (mock.f _).expects(42).returnsKyo("-42")
   *     }
   *     value <- Kyo.serviceWithKyo[Service](_.f(42))
   *   } yield assertTrue(value == "-42")
   *   effect.provideLayer(layer1 ++ layer2)
   * }
   * }}}
   *
   * In this test, two mocks are created — one expects to be called with an argument 42,
   * and the second — with an argument 43 (and probably creating TWO mocks is not what the developer wanted).
   *
   * So when writing tests on scalamock from scratch, it's not recommended
   * to use this conversion (and the trait SimplerKyoMockMigration, in general).
   * Only for simplifying the migration from kyo-mock to scalamock.
   *
   * Moreover, in some cases, when there's no complex logic on layers
   * in the test, it's possible to migrate without this trait, if you get rid
   * of passing expectations to layers.
   */
  inline def createMockedLayer[A](expectations: URIO[A, Any])(using tag: Tag[A]): ULayer[A] = {
    ${ LayeredMockMacros.createMockedLayer[A]('expectations, 'tag, '{unsafeMockFactory.mockContext} ) }
  }
} 