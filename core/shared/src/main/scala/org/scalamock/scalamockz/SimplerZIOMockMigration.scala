package org.scalamock.scalamockz

import org.scalamock.scalamockz.macros.LayeredMockMacros
import zio.{Tag, URIO, URLayer}

/**
 * Can simplify migration from zio-mock to scalamockz.
 * Not required when writing tests on scalamockz from scratch.
 */
trait SimplerZIOMockMigration {

  import scala.language.experimental.macros

  /**
   * Creates a Layer that generates a new mock.
   *
   * WARN: Use with caution, and pass only one Layer of each type into the test!
   *
   * In zio-mock, there is an implicit conversion from `Expectation[A]` to `ULayer[A]`.
   * In scalamockz, `URIO[A with ScalamockZFactory, Any]` is used as a replacement for `Expectation`.
   * To simplify the migration from zio-mock to scalamockz, a similar conversion is provided to avoid rewriting
   * how expectations are passed into layers.
   *
   * This conversion is not declared as `implicit` to avoid introducing unintended bugs, and it must be invoked manually.
   *
   * WARN: Careless passing of ZIO into layers may lead to unexpected behavior,
   * such as creating mocks that were not intended.
   *
   * Example:
   * {{{
   * test("creation of two mocks") {
   *   val layer1 = createMockedLayer(
   *     ZIO.serviceWith[Service] { mock =>
   *       (mock.f _).expects(42).returnsZIO("-42")
   *     }
   *   )
   *   val layer2 = createMockedLayer(
   *     ZIO.serviceWith[Service] { mock =>
   *       (mock.f _).expects(43).returnsZIO("-43")
   *     }
   *   )
   *   val effect = for {
   *     _ <- ZIO.serviceWith[Service] { mock =>
   *       (mock.f _).expects(42).returnsZIO("-42")
   *     }
   *     value <- ZIO.serviceWithZIO[Service](_.f(42))
   *   } yield assertTrue(value == "-42")
   *   effect.provideSomeLayer(layer1 ++ layer2)
   * }
   * }}}
   *
   * In this test, two mocks are created — one expects to be called with the argument `42`,
   * and the other with `43` (and likely creating TWO mocks is not what the developer intended).
   *
   * For tests written from scratch using scalamockz, it is generally
   * not recommended to use this conversion and the SimplerZIOMockMigration trait.
   * It is intended only to simplify migration from zio-mock to scalamockz.
   *
   * Furthermore, in some cases where the test does not involve complex logic with layers,
   * you can skip using this trait during migration by avoiding the need to pass expectations into layers.
   */
  def createMockedLayer[A](
      expectations: URIO[A with ScalamockZFactory, Any]
    )(implicit tag: Tag[A]
    ): URLayer[ScalamockZFactory, A] =
    macro LayeredMockMacros.mockedImpl[A]
}
