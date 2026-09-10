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

package org.scalamock.kyotest

import org.scalamock.handlers.CallHandler
import org.scalamock.kyotest.internal.{KyoAssertionSupport, MockContextStub}
import org.scalamock.matchers.{Matchers, MockParameter}
import kyo.internal.stacktracer.SourceLocation
import kyo.test.Assertion
import kyo.{IO, UIO, Kyo}

import scala.language.implicitConversions
import scala.reflect.ClassTag

/**
 * Base functionality of ScalamockKyoSyntax, not depending on macros.
 * Contains wrappers for Kyo and syntax for assertions.
 */
private[kyotest] trait ScalamockKyoSyntaxBase extends Matchers with MockContextStub with KyoAssertionSupport {

  // Wrappers for more convenient mocking of methods returning Kyo.
  // Naming is similar to methods from classic scalamock:
  // - for success return returnsKyo/returningKyo
  // - for error return failsKyo/failingKyo
  // - for defect return dieKyo/dyingKyo
  implicit class RichIOHandler[E, A](val handler: CallHandler[IO[E, A]]) {

    def returnsKyo(value: A): handler.Derived =
      handler.returns(Kyo.succeed(value))

    def returningKyo(value: A): handler.Derived =
      returnsKyo(value)

    def returnsKyoUnit(implicit ev: Unit <:< A): handler.Derived =
      handler.returns(Kyo.unit.map(ev))

    def returningKyoUnit(implicit ev: Unit <:< A): handler.Derived =
      returnsKyoUnit

    def failsKyo(error: E): handler.Derived =
      handler.returns(Kyo.fail(error))

    def failingKyo(error: E): handler.Derived =
      failsKyo(error)

    def diesKyo(error: Throwable): handler.Derived =
      handler.returns(Kyo.die(error))

    def dyingKyo(error: Throwable): handler.Derived =
      diesKyo(error)
  }

  // The compiler doesn't resolve RichIOHandler if the method returns UIO,
  // so we have to explicitly add extension methods for UIO
  implicit class RichUIOHandler[A](val handler: CallHandler[UIO[A]]) {

    def returnsKyo(value: A): handler.Derived =
      handler.returns(Kyo.succeed(value))

    def returningKyo(value: A): handler.Derived =
      returnsKyo(value)

    def diesKyo(error: Throwable): handler.Derived =
      handler.returns(Kyo.die(error))

    def dyingKyo(error: Throwable): handler.Derived =
      diesKyo(error)
  }

  /** Syntax for converting kyo.test.Assertion to MockParameter.
    * Internally uses argAssert, so it doesn't work with inAnyOrder.
    *
    * For using with inAnyOrder, use [[AllowAnyOrderSyntax]] with `myAssertion.allowUnorderedCalls`
    *
    * Example:
    *
    * {{{
    * object MyUtils extends ScalamockKyoSyntax {
    *   def mockGetName = Kyo.serviceWith[UserService] { mock =>
    *     (mock.getName _).expects(hasField("id", _.id, equalTo(4))).returnsKyo("Agent Smith")
    *   }
    * }
    * }}}
    *
    * Equivalent to:
    *
    * {{{
    * object MyUtils extends ScalamockKyoSyntax {
    *   def mockGetName = Kyo.serviceWith[UserService] { mock =>
    *     (mock.getName _).expects(argAssert("request")(_.id shouldBe 4)).returnsKyo("Agent Smith")
    *   }
    * }
    * }}}
    *
    * */
  implicit def kyoAssertionToMockParameter[A: ClassTag](
      assertion: Assertion[A]
    )(implicit sourceLocation: SourceLocation): MockParameter[A] =
    assertionToMockParameter(assertion, allowAnyOrder = false)

  /**
    * Similar to [[kyoAssertionToMockParameter]] with support for arbitrary mock calls. Reports less information on UNSATISFIED errors
    */
  implicit def allowAnyOrderSyntax[A: ClassTag](assertion: Assertion[A]): AllowAnyOrderSyntax[A] =
    new AllowAnyOrderSyntax(assertion)
} 