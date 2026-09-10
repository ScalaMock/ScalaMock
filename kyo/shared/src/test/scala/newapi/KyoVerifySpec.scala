package newapi
import org.scalamock.stubs.{CallLog, Stub, KyoStubs}
import kyo.{IO, Scope, UIO, Kyo}
import kyo.test.{Spec, TestEnvironment, KyoSpecDefault, assertTrue}

object KyoVerifySpec extends KyoSpecDefault, KyoStubs:
  trait FirstTrait:
    def foo(x: Int, y: Int): UIO[Int]

    def foo2(x: Int): UIO[Int]

  trait SecondTrait:
    def bar(x: String): IO[String, String]

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("kyo verify test-cases")(
      test("verify"):
        given log: CallLog = CallLog()
        val first = stub[FirstTrait]
        val second = stub[SecondTrait]
        for {
          _ <- first.foo.returnsKyo(_ => Kyo.succeed(0))
          _ <- first.foo2.returnsKyo(_ => Kyo.succeed(0))
          _<- second.bar.returnsKyo(_ => Kyo.succeed(""))
          _ <- second.bar("1")
          _ <- first.foo(1, 1)
          _ <- first.foo2(1)
        } yield assertTrue(
          second.bar.isBefore(first.foo),
          second.bar.calls == List("1")
        )
    )
