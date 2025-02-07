package org.scalamock.scalamockz

import org.scalatest.exceptions.TestFailedException
import zio.test.TestAspect.before
import zio.test.{assertTrue, TestAspect, TestAspectAtLeastR, TestEnvironment}
import zio._

object ScalamockZSpecSpec extends ScalamockZSpec {

  trait Service {
    def f(x: Int): UIO[String]
  }

  override def spec: MockedSpec =
    suite("ScalamockZSpec: mocks")(mockTests).provideSome[ScalamockZFactory](mock[Service]) +
      suite("ScalamockZSpec: stubs")(stubTests).provideSome[ScalamockZFactory](stub[Service])

  private val mockTests = List(
    test("succeed if function is invoked as expected") {
      for {
        _ <- ZIO.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsZIO("-42")
        }
        value <- ZIO.serviceWithZIO[Service](_.f(42))
        _ <- ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations())
      } yield assertTrue(value == "-42")
    },
    test("when order of invocations is expected, should succeed if two functions are invoked sequentially") {
      for {
        _ <- ZIO.serviceWithZIO[Service] { mock =>
          inSequence {
            (mock.f _).expects(42).returnsZIO("-42")
            (mock.f _).expects(43).returnsZIO("1")
          }
        }
        value1 <- ZIO.serviceWithZIO[Service](_.f(42))
        value2 <- ZIO.serviceWithZIO[Service](_.f(43))
        _ <- ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations())
      } yield assertTrue(value1 == "-42" && value2 == "1")
    },
    test("when order of invocations is expected, should fail if second function is invoked before first") {
      for {
        _ <- ZIO.serviceWithZIO[Service] { mock =>
          inSequence {
            (mock.f _).expects(42).returnsZIO("-42")
            (mock.f _).expects(43).returnsZIO("1")
          }
        }
        result <- ZIO
          .serviceWithZIO[Service](_.f(43))
          .flip
          .catchSomeDefect { case e: TestFailedException => ZIO.succeed(e) }
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("Unexpected call: <mock-1> Service.f(43)"))
      }
    },
    test("when order of invocations is not defined, should succeed if two functions are invoked sequentially") {
      for {
        _ <- ZIO.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsZIO("-42")
          (mock.f _).expects(43).returnsZIO("1")
        }
        value1 <- ZIO.serviceWithZIO[Service](_.f(42))
        value2 <- ZIO.serviceWithZIO[Service](_.f(43))
        _ <- ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations())
      } yield assertTrue(value1 == "-42" && value2 == "1")
    },
    test("when order of invocations is not defined, should succeed if second function is invoked before first") {
      for {
        _ <- ZIO.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsZIO("-42")
          (mock.f _).expects(43).returnsZIO("1")
        }
        value1 <- ZIO.serviceWithZIO[Service](_.f(43))
        value2 <- ZIO.serviceWithZIO[Service](_.f(42))
        _ <- ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations())
      } yield assertTrue(value1 == "1" && value2 == "-42")
    },
    test("fail if function is invoked with wrong arguments") {
      for {
        _ <- ZIO.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsZIO("-42")
        }
        result <- ZIO
          .serviceWithZIO[Service](_.f(41))
          .flip
          .catchSomeDefect { case e: TestFailedException => ZIO.succeed(e) }
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
        _ <- ZIO.serviceWith[Service] { mock =>
          (mock.f _).expects(42).returnsZIO("-42")
        }
        service <- ZIO.service[Service]
        _ = service.f(42)
        result <- ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations()).flip
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("<mock-1> Service.f(42) once (never called - UNSATISFIED)"))
      }
    }
  )

  private val stubTests = List(
    test("fail on non-verified invocation") {
      for {
        _ <- ZIO.serviceWith[Service] { stub =>
          (stub.f _).when(42).returnsZIO("-42")
          (stub.f _).when(43).returnsZIO("-43")
        }
        _ <- ZIO.serviceWithZIO[Service](_.f(42))
        _ <- ZIO.serviceWith[Service] { stub =>
          (stub.f _).verify(43)
        }
        result <- ZIO.serviceWithZIO[ScalamockZFactory](_.verifyExpectations()).flip
      } yield {
        val errorMessage = result.getMessage
        assertTrue(errorMessage.contains("<stub-1> Service.f(43) once (never called - UNSATISFIED)"))
      }
    }
  )

  // We have to copy-paste aspects from ZIOSpec and ScalamockZSpec
  // because otherwise, recovery from a verifyExpectations failure in the aspect would not be possible,
  // and the test couldn't be marked as successful.
  override def aspects: Chunk[TestAspectAtLeastR[TestEnvironment with ScalamockZFactory]] =
    Chunk(
      TestAspect.fibers,
      TestAspect.timeoutWarning(60.seconds),
      before(ZIO.serviceWithZIO[ScalamockZFactory](_.initializeExpectations())),
      TestAspect.fromLayer(ScalamockZFactory.live)
    )
}
