package somepackage.org.scalamock.scalamockz

import org.scalamock.scalamockz._
import zio.test.assertTrue
import zio._

/**
 * A simple test to verify that mocking from another package (somepackage) works correctly.
 * We cannot check cases here where tests should fail.
 * Such cases are tested in [[org.scalamock.scalamockz.ScalamockZSpecSpec]].
 */
object ScalamockZSuccessSpec extends ScalamockZSpec {

  trait Service {
    def f(x: Int): UIO[String]
  }

  override def spec: MockedSpec =
    suite("ScalamockZSpec")(
      test("succeed if function is invoked as expected") {
        for {
          _ <- ZIO.serviceWith[Service] { mock =>
            (mock.f _).expects(42).returnsZIO("-42")
          }
          value <- ZIO.serviceWithZIO[Service](_.f(42))
        } yield assertTrue(value == "-42")
      }.provideSome[ScalamockZFactory](mock[Service]),
      test("succeed if not all stub invocation were invoked") {
        for {
          _ <- ZIO.serviceWith[Service] { stub =>
            (stub.f _).when(42).returnsZIO("-42")
            (stub.f _).when(43).returnsZIO("-43")
          }
          value <- ZIO.serviceWithZIO[Service](_.f(42))
        } yield assertTrue(value == "-42")
      }.provideSome[ScalamockZFactory](stub[Service]),
      test("succeed if all verified stub invocations were invoked") {
        for {
          _ <- ZIO.serviceWith[Service] { stub =>
            (stub.f _).when(42).returnsZIO("-42")
          }
          value <- ZIO.serviceWithZIO[Service](_.f(42))
          _ <- ZIO.serviceWith[Service] { stub =>
            (stub.f _).verify(42)
          }
        } yield assertTrue(value == "-42")
      }.provideSome[ScalamockZFactory](stub[Service])
    )
}
