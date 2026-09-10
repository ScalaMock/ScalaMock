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

import kyo.test.{assertTrue, Assertion, Spec, TestAspectAtLeastR, TestEnvironment}
import kyo._
import kyo.internal.stacktracer.SourceLocation
import kyo.test.Assertion._

object ScalamockKyoSpecSpec extends ScalamockKyoSpec {

  trait Service {
    def f(x: Int): UIO[String]
  }

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("ScalamockKyoSpec: mocks")(mockTests).provideShared(mock[Service]) +
      suite("ScalamockKyoSpec: stubs")(stubTests).provideShared(stub[Service]) +
      suite("ScalamockKyoSpec: named")(namedTests)

  private val mockTests = List(
    test("succeed if function is invoked as expected") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsKyo("-42")
        }
        value <- Kyo.serviceWithKyo[Service](_.f(42))
        _ <- unsafeMockFactory.verifyExpectations()
      } yield assertTrue(value == "-42")
    },
    test("when order of invocations is expected, should succeed if two functions are invoked sequentially") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          inSequence {
            (mock.f _).expects(42).returnsKyo("-42")
            (mock.f _).expects(43).returnsKyo("1")
          }
        }
        value1 <- Kyo.serviceWithKyo[Service](_.f(42))
        value2 <- Kyo.serviceWithKyo[Service](_.f(43))
        _ <- unsafeMockFactory.verifyExpectations()
      } yield assertTrue(value1 == "-42" && value2 == "1")
    },
    test("when order of invocations is expected, should fail if second function is invoked before first") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          inSequence {
            (mock.f _).expects(42).returnsKyo("-42")
            (mock.f _).expects(43).returnsKyo("1")
          }
        }
        result <- Kyo
          .serviceWithKyo[Service](_.f(43))
          .flip
          .catchSomeDefect { case e: ExpectationException => Kyo.succeed(e) }
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("Unexpected call: <mock-1> Service.f(43)"))
      }
    },
    test("when order of invocations is not defined, should succeed if two functions are invoked sequentially") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsKyo("-42")
          (mock.f _).expects(43).returnsKyo("1")
        }
        value1 <- Kyo.serviceWithKyo[Service](_.f(42))
        value2 <- Kyo.serviceWithKyo[Service](_.f(43))
        _ <- unsafeMockFactory.verifyExpectations()
      } yield assertTrue(value1 == "-42" && value2 == "1")
    },
    test("when order of invocations is not defined, should succeed if second function is invoked before first") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsKyo("-42")
          (mock.f _).expects(43).returnsKyo("1")
        }
        value1 <- Kyo.serviceWithKyo[Service](_.f(43))
        value2 <- Kyo.serviceWithKyo[Service](_.f(42))
        _ <- unsafeMockFactory.verifyExpectations()
      } yield assertTrue(value1 == "1" && value2 == "-42")
    },
    test("fail if function is invoked with wrong arguments") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsKyo("-42")
        }
        result <- Kyo
          .serviceWithKyo[Service](_.f(41))
          .flip
          .catchSomeDefect { case e: ExpectationException => Kyo.succeed(e) }
      } yield {
        val errorMessage = result.getMessage
        assertTrue(
          errorMessage.contains("<mock-1> Service.f(42) once (never called - UNSATISFIED)") &&
            !errorMessage.contains("Actual:\n      <mock-1> Service.f(41)")
        )
      }
    },
    test("fail if function is invoked, but effect is not invoked") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsKyo("-42")
        }
        service <- Kyo.service[Service]
        _ = service.f(42)
        result <- unsafeMockFactory.verifyExpectations().flip
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("<mock-1> Service.f(42) once (never called - UNSATISFIED)"))
      }
    },
    test("fail if function is invoked with wrong arguments when kyo assertions used") {
      // used when converting Assertion to MockParameter
      implicit val sourceLocation: SourceLocation = SourceLocation("testPath", line = 10)
      val inputAssertion: Assertion[Int] = Assertion.isWithin(42, 45)
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(inputAssertion).returnsKyo("-42")
        }
        result <- Kyo
          .serviceWithKyo[Service](_.f(41))
          .flip
          .catchSomeDefect { case e: ExpectationException => Kyo.succeed(e) }
      } yield {
        val errorMessage = result.getMessage
        val expected = renderKyoAssertion(inputAssertion.run(41).failures.get)

        assertTrue(errorMessage.contains(expected))
      }
    },
    test("fail if function is invoked with wrong arguments order kyo assertions used") {
      // used when converting Assertion to MockParameter
      implicit val sourceLocation: SourceLocation = SourceLocation("testPath", line = 10)
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(equalTo(42)).returnsKyo("-42")
          (mock.f _).expects(equalTo(41)).returnsKyo("-41")
        }
        result <- Kyo
          .serviceWithKyo[Service](s => s.f(41) *> s.f(42))
          .flip
          .catchSomeDefect { case e: ExpectationException => Kyo.succeed(e) }
      } yield {
        val errorMessage = result.getMessage
        val expected = renderKyoAssertion(equalTo(42).run(41).failures.get)

        assertTrue(errorMessage.contains(expected))
      }
    },
    test("fail if function is invoked with wrong arguments order kyo assertions used when inAnyOrder used") {
      // used when converting Assertion to MockParameter
      implicit val sourceLocation: SourceLocation = SourceLocation("testPath", line = 10)
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          inAnyOrder {
            (mock.f _).expects(equalTo(42)).returnsKyo("-42")
            (mock.f _).expects(equalTo(41)).returnsKyo("-41")
          }
        }
        result <- Kyo
          .serviceWithKyo[Service](s => s.f(41) *> s.f(42))
          .flip
          .catchSomeDefect { case e: ExpectationException => Kyo.succeed(e) }
      } yield {
        val errorMessage = result.getMessage
        val expected = renderKyoAssertion(equalTo(42).run(41).failures.get)

        assertTrue(errorMessage.contains(expected))
      }
    }
  )

  private val stubTests = List(
    test("fail on non-verified invocation") {
      for {
        _ <- Kyo.serviceWith[Service] { stub =>
          (stub.f _).when(42).returnsKyo("-42")
          (stub.f _).when(43).returnsKyo("-43")
        }
        _ <- Kyo.serviceWithKyo[Service](_.f(42))
        _ <- Kyo.serviceWith[Service] { stub =>
          (stub.f _).verify(43)
        }
        result <- unsafeMockFactory.verifyExpectations().flip
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("<stub-2> Service.f(43) once (never called - UNSATISFIED)"))
      }
    }
  )

  private val namedTests = List(
    test("include custom mock name in error") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsKyo("-42")
        }
        result <- unsafeMockFactory.verifyExpectations().flip
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("<my-service-mock> Service.f(42) once (never called - UNSATISFIED)"))
      }
    }.provide(mock[Service]("my-service-mock")),
    test("include custom stub name in error") {
      for {
        _ <- Kyo.serviceWith[Service] { mock =>
          (mock.f _).verify(42)
        }
        result <- unsafeMockFactory.verifyExpectations().flip
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("<my-service-stub> Service.f(42) once (never called - UNSATISFIED)"))
      }
    }.provide(stub[Service]("my-service-stub"))
  )

  // have to override aspects, here verifyExpectations is not called
  // otherwise it's not possible to recover from the failure in the aspect, and succeed the test
  override def aspects: Chunk[TestAspectAtLeastR[TestEnvironment]] =
    scalamockInternalSpecAspects
}
