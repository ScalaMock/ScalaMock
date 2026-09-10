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

import kyo.test.{Spec, TestAspectAtLeastR, TestEnvironment, assertTrue}
import kyo._

object SimplerKyoMockMigrationSpec extends ScalamockKyoSpec with SimplerKyoMockMigration {

  trait Service {
    def f(x: Int): UIO[String]
  }

  trait AnotherService {
    def g(x: String): UIO[Int]
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("SimplerKyoMockMigration")(
      test("valid expectations should succeed") {
        val layer = createMockedLayer(
          Kyo.serviceWith[Service] { mock =>
            (mock.f _).expects(42).returnsKyo("success")
          }
        )

        val effect = for {
          value <- Kyo.serviceWithKyo[Service](_.f(42))
        } yield assertTrue(value == "success")

        effect.provideLayer(layer)
      },
      test("invalid expectations should fail") {
        val layer = createMockedLayer(
          Kyo.serviceWith[Service] { mock =>
            (mock.f _).expects(42).returnsKyo("expected")
          }
        )

        Kyo
          .serviceWithKyo[Service](_.f(41))
          .provideLayer(layer)
          .flip
          .catchSomeDefect { case e: org.scalamock.kyotest.ExpectationException =>
            Kyo.succeed(e.getMessage)
          }
          .map { errorMessage =>
            assertTrue(errorMessage.contains("UNSATISFIED"))
          }
      },
      test("two mocks should work together") {
        val layer1 = createMockedLayer(
          Kyo.serviceWith[Service] { mock =>
            (mock.f _).expects(1).returnsKyo("one")
          }
        )

        val layer2 = createMockedLayer(
          Kyo.serviceWith[AnotherService] { mock =>
            (mock.g _).expects("test").returnsKyo(42)
          }
        )

        val effect = for {
          service1 <- Kyo.service[Service]
          service2 <- Kyo.service[AnotherService]
          result1 <- service1.f(1)
          result2 <- service2.g("test")
        } yield assertTrue(result1 == "one" && result2 == 42)

        effect.provideLayer(layer1 ++ layer2)
      }
    )

  // have to override aspects, here verifyExpectations is not called
  // otherwise it's not possible to recover from the failure in the aspect, and succeed the test
  override def aspects: Chunk[TestAspectAtLeastR[TestEnvironment]] =
    scalamockInternalSpecAspects
}
