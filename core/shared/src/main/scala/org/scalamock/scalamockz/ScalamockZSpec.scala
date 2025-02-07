package org.scalamock.scalamockz

import org.scalamock.handlers.CallHandler
import org.scalamock.matchers.Matchers
import org.scalamock.scalamockz.internal.{MockContextStub, ScalamockZSpecSetup}
import org.scalamock.scalamockz.macros.LayeredMockMacros
import org.scalamock.scalamockz.syntax.MockConvertions
import zio.{IO, Scope, UIO, URIO, URLayer, ZIO}
import zio.test.{Spec, TestEnvironment}

/**
 * Base trait for tests with mocks using zio-test.
 * 
 * Basic usage example:
 * 
 * {{{
 * object ExampleSpec extends ScalamockZSpec {
 *
 *   trait Service {
 *     def f(x: Int): UIO[String]
 *   }
 *
 *   override def spec: MockedSpec =
 *     suite("ExampleSpec")(
 *       test("succeed if function is invoked as expected") {
 *         for {
 *           _ <- ZIO.serviceWith[Service] { mock =>
 *             (mock.f _).expects(42).returns(ZIO.succeed("-42"))
 *           }
 *           value <- ZIO.serviceWithZIO[Service](_.f(42))
 *         } yield assertTrue(value == "-42")
 *       },
 *     ).provideSome[ScalamockZFactory](mock[Service])
 * } 
 * }}}
 */
trait ScalamockZSpec extends ScalamockZSpecSetup with Matchers with MockContextStub with MockConvertions {

  import scala.language.experimental.macros

  /**
   * Returns a recipe for creating a mock for testing in the Expectations-First style.
   * https://scalamock.org/user-guide/mocking_style/
   *
   * Typically, it should be provided to the layers where the mock is required.
   */
  def mock[A]: URLayer[ScalamockZFactory, A] =
    macro LayeredMockMacros.mockImpl[A]

  /**
   * Returns a recipe for creating a stub for testing in the Record-then-Verify style.
   * https://scalamock.org/user-guide/mocking_style/
   *
   * Typically, it should be provided to the layers where the stub is required.
   */
  def stub[A]: URLayer[ScalamockZFactory, A] =
    macro LayeredMockMacros.stubImpl[A]

  // Shortcuts for working with mocks in tests using ScalamockZSpec.
  type MockedSpec = Spec[TestEnvironment with Scope with ScalamockZFactory, Any]
  type MockedLayer[A] = URLayer[ScalamockZFactory, A]

  // Wrappers for more convenient mocking of methods that return ZIO.
  // Naming follows the pattern from Scalamock:
  // - for returning success: returnsZIO/returningZIO
  // - for returning error: failsZIO/failingZIO
  // - for returning a defect: dieZIO/dyingZIO
  implicit class RichIOHandler[E, A](val handler: CallHandler[IO[E, A]]) {

    def returnsZIO(value: A): handler.Derived =
      handler.returns(ZIO.succeed(value))

    def returningZIO(value: A): handler.Derived =
      returnsZIO(value)

    def failsZIO(error: E): handler.Derived =
      handler.returns(ZIO.fail(error))

    def failingZIO(error: E): handler.Derived =
      failsZIO(error)

    def diesZIO(error: Throwable): handler.Derived =
      handler.returns(ZIO.die(error))

    def dyingZIO(error: Throwable): handler.Derived =
      diesZIO(error)
  }

  // RichIOHandler does not resolve when the method returns UIO
  // so we need to explicitly add extension methods for UIO
  implicit class RichUIOHandler[A](val handler: CallHandler[UIO[A]]) {

    def returnsZIO(value: A): handler.Derived =
      handler.returns(ZIO.succeed(value))

    def returningZIO(value: A): handler.Derived =
      returnsZIO(value)

    def diesZIO(error: Throwable): handler.Derived =
      handler.returns(ZIO.die(error))

    def dyingZIO(error: Throwable): handler.Derived =
      diesZIO(error)
  }

  /**
   * Allows defining a sequence of mock method calls:
   *
   * {{{
   * ZIO.serviceWithZIO[Service] { mock =>
   *   inSequence {
   *     (mock.f _).expects(42).returnsZIO("-42")
   *     (mock.f _).expects(43).returnsZIO("1")
   *   }
   * }
   * }}}
   *
   * You can also define a sequence of calls across multiple mocks, for example:
   *
   * {{{
   * for {
   *   authService <- ZIO.service[AuthService]
   *   userService <- ZIO.service[UserService]
   *   _ <- inSequence {
   *     (authService.isAuthorized _).expects(4).returnsZIO(true)
   *     (userService.getUserName _).expects(4).returnsZIO("Agent Smith")
   *   }
   * } yield ()
   * }}}
   *
   * The test will fail if the expected sequence of calls is not followed.
   */
  def inSequence[A](what: => A): URIO[ScalamockZFactory, Unit] =
    ZIO.serviceWith[ScalamockZFactory] { factory =>
      val _ = factory.inSequence(what)
    }
}
