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

package somepackage.org.scalamock.kyotest

import org.scalamock.kyotest._
import kyo.test._
import kyo._
import kyo.test.Assertion._

/**
 * Simple test to check that mocking works from another package.
 * We can't check cases when tests should fail here.
 * Such cases are checked in [[org.scalamock.test.kyotest.ScalamockKyoSpecSpec]].
 */
object ScalamockKyoSuccessSpec extends ScalamockKyoSpec {

  trait Service {
    def f(x: Int): UIO[String]
    def stringInput(x: String): UIO[String]
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ScalamockKyoSpec")(
      test("succeed if function is invoked as expected") {
        for {
          _ <- Kyo.serviceWith[Service] { mock =>
            (mock.f _).expects(42).returnsKyo("-42")
          }
          value <- Kyo.serviceWithKyo[Service](_.f(42))
        } yield assertTrue(value == "-42")
      }.provide(mock[Service]),
      test("correctly use kyo assertion api") {
        for {
          _ <- Kyo.serviceWith[Service] { mock =>
            (mock.stringInput _)
              .expects(startsWithString("give me") && endsWithString("value"))
              .returnsKyo("expected value")
          }
          actual <- Kyo.serviceWithKyo[Service](_.stringInput("give me expected value"))
        } yield assertTrue(actual == "expected value")
      }.provide(mock[Service]),
      test("correctly use kyo assertion api with allowUnorderedCalls") {
        for {
          _ <- Kyo.serviceWith[Service] { mock =>
            (mock.stringInput _)
              .expects(startsWithString("give me") && endsWithString("value"))
              .returnsKyo("expected value")
          }
          actual <- Kyo.serviceWithKyo[Service](_.stringInput("give me expected value"))
        } yield assertTrue(actual == "expected value")
      }.provide(mock[Service]),
      test("correctly use kyo assertion api with more complex checks") {
        def int(i: Int): Assertion[Int] = equalTo(i)

        for {
          _ <- Kyo.serviceWith[Service] { mock =>
            (mock.f _).expects(int(42).allowUnorderedCalls).returnsKyo("42")
            (mock.f _).expects(int(41).allowUnorderedCalls).returnsKyo("41")
          }
          actual <- Kyo.serviceWithKyo[Service](s => s.f(41).zip(s.f(42)))
        } yield assertTrue(actual == Tuple2("41", "42"))
      }.provide(mock[Service]),
      test("succeed if not all stub invocation were invoked") {
        for {
          _ <- Kyo.serviceWith[Service] { stub =>
            (stub.f _).when(42).returnsKyo("-42")
            (stub.f _).when(43).returnsKyo("-43")
          }
          value <- Kyo.serviceWithKyo[Service](_.f(42))
        } yield assertTrue(value == "-42")
      }.provide(stub[Service]),
      test("succeed if all verified stub invocations were invoked") {
        for {
          _ <- Kyo.serviceWith[Service] { stub =>
            (stub.f _).when(42).returnsKyo("-42")
          }
          value <- Kyo.serviceWithKyo[Service](_.f(42))
          _ <- Kyo.serviceWith[Service] { stub =>
            (stub.f _).verify(42)
          }
        } yield assertTrue(value == "-42")
      }.provide(stub[Service])
    )
}
