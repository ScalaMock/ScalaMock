package newapi

import kyo.*
import kyo.test.*
import org.scalamock.stubs.KyoStubs
import kyo.test.Assertion.*

object StubbedKyoMethodSpec extends KyoSpecDefault, KyoStubs:
  // Define test traits
  trait TestTrait0:
    def noArgs: UIO[Int]
    def noArgsWithFailure: IO[String, Int]
    def noArgsWithDie: IO[Nothing, Nothing]

  trait TestTraitWithArgs:
    def oneArg(x: Int): UIO[String]
    def twoArgs(x: Int, y: String): IO[String, Int]
    def threeArgs(x: Int, y: String, z: Boolean): IO[Exception, Int]

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("StubbedKyoMethod tests")(
      suite("StubbedKyoMethod0 tests")(
        test("returnsKyo should set the return value for a method without arguments"):
          val testStub = stub[TestTrait0]
          for
            _ <- testStub.noArgs.returnsKyoWith(Kyo.succeed(42))
            result <- testStub.noArgs
          yield assertTrue(result == 42)
        ,
        test("succeedsWith should set a success value for a method without arguments"):
          val testStub = stub[TestTrait0]
          for
            _ <- testStub.noArgsWithFailure.succeedsWith(42)
            result <- testStub.noArgsWithFailure
          yield assertTrue(result == 42)
        ,
        test("failsWith should set a failure value for a method without arguments"):
          val testStub = stub[TestTrait0]
          for
            _ <- testStub.noArgsWithFailure.failsWith("error")
            exit <- testStub.noArgsWithFailure.exit
          yield exit match
            case Exit.Success(_) => assertTrue(false)
            case Exit.Failure(cause) => assertTrue(cause.failures.contains("error"))
        ,
        test("diesWith should set a die value for a method without arguments"):
          val testStub = stub[TestTrait0]
          val exception = new RuntimeException("test exception")
          for
            _ <- testStub.noArgsWithDie.diesWith(exception)
            exit <- testStub.noArgsWithDie.exit
          yield exit match
            case Exit.Success(_) => assertTrue(false)
            case Exit.Failure(cause) => assertTrue(cause.defects.contains(exception))
        ,
        test("timesKyo should return the number of times a method was called"):
          val testStub = stub[TestTrait0]
          for
            _ <- testStub.noArgs.returnsKyoWith(Kyo.succeed(42))
            _ <- testStub.noArgs
            _ <- testStub.noArgs
            _ <- testStub.noArgs
            times <- testStub.noArgs.timesKyo
          yield assertTrue(times == 3)
      ),
      suite("StubbedKyoMethod tests")(
        test("returnsKyo should set the return value for a method with arguments"):
          val testStub = stub[TestTraitWithArgs]
          for
            _ <- testStub.oneArg.returnsKyo(x => Kyo.succeed(s"Value: $x"))
            result <- testStub.oneArg(42)
          yield assertTrue(result == "Value: 42")
        ,
        test("succeedsWith should set a success value for a method with arguments"):
          val testStub = stub[TestTraitWithArgs]
          for
            _ <- testStub.twoArgs.succeedsWith(42)
            result <- testStub.twoArgs(10, "test")
          yield assertTrue(result == 42)
        ,

        test("failsWith should set a failure value for a method with arguments"):
          val testStub = stub[TestTraitWithArgs]
          for
            _ <- testStub.twoArgs.failsWith("error")
            exit <- testStub.twoArgs(10, "test").exit
          yield exit match
            case Exit.Success(_) => assertTrue(false)
            case Exit.Failure(cause) => assertTrue(cause.failures.contains("error"))
        ,

        test("diesWith should set a die value for a method with arguments"):
          val testStub = stub[TestTraitWithArgs]
          val exception = new RuntimeException("test exception")
          for
            _ <- testStub.threeArgs.diesWith(exception)
            exit <- testStub.threeArgs(10, "test", true).exit
          yield exit match
            case Exit.Success(_) => assertTrue(false)
            case Exit.Failure(cause) => assertTrue(cause.defects.contains(exception))
        ,
        test("timesKyo should return the number of times a method was called"):
          val testStub = stub[TestTraitWithArgs]
          for
            _ <- testStub.oneArg.returnsKyo(_ => Kyo.succeed("test"))
            _ <- testStub.oneArg(1)
            _ <- testStub.oneArg(2)
            _ <- testStub.oneArg(3)
            times <- testStub.oneArg.timesKyo
          yield assertTrue(times == 3)
        ,
        test("set result depending on number of call"):
          val testStub = stub[TestTraitWithArgs]
          for
            _ <- testStub.oneArg.returnsKyoOnCall:
              case 1 => Kyo.succeed("test")
              case _ => Kyo.succeed("test2")
            res1 <- testStub.oneArg(1)
            res2 <- testStub.oneArg(2)
            res3 <- testStub.oneArg(3)
          yield assertTrue(
            res1 == "test",
            res2 == "test2",
            res3 == "test2"
          )
      )
    ) @@ TestAspect.before(Kyo.succeed(resetStubs())) @@ TestAspect.sequential
