package newapi

import org.scalamock.stubs.{CallLog, Stubs, StubbedMethod}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StubbedMethodSpec extends AnyFlatSpec with Matchers with Stubs:
  // Define test traits
  trait TestTrait0:
    def noArgs: Int
    def noArgsString(): String

  trait TestTraitWithArgs:
    def oneArg(x: Int): String
    def twoArgs(x: Int, y: String): Int
    def threeArgs(x: Int, y: String, z: Boolean): Int

  "StubbedMethod0" should "set the return value for a method without arguments" in:
    val testStub = stub[TestTrait0]
    (() => testStub.noArgs).returnsWith(42)
    testStub.noArgs should be(42)

  it should "track the number of times a method was called" in:
    val testStub = stub[TestTrait0]
    (() => testStub.noArgs).returnsWith(42)
    testStub.noArgs
    testStub.noArgs
    testStub.noArgs
    (() => testStub.noArgs).times should be(3)

  it should "work with methods that have parentheses" in:
    val testStub = stub[TestTrait0]
    (() => testStub.noArgsString()).returnsWith("test")
    testStub.noArgsString() should be("test")

  "StubbedMethod" should "set the return value for a method with arguments" in:
    val testStub = stub[TestTraitWithArgs]
    testStub.oneArg.returns(x => s"Value: $x")
    testStub.oneArg(42) should be("Value: 42")

  it should "track the number of times a method was called" in:
    val testStub = stub[TestTraitWithArgs]
    testStub.oneArg.returns(_ => "test")
    testStub.oneArg(1)
    testStub.oneArg(2)
    testStub.oneArg(3)
    testStub.oneArg.times should be(3)

  it should "track the number of times a method was called with specific arguments" in:
    val testStub = stub[TestTraitWithArgs]
    testStub.oneArg.returns(_ => "test")
    testStub.oneArg(1)
    testStub.oneArg(2)
    testStub.oneArg(1)
    testStub.oneArg.times(1) should be(2)
    testStub.oneArg.times(2) should be(1)
    testStub.oneArg.times(3) should be(0)

  it should "track the arguments with which a method was called" in:
    val testStub = stub[TestTraitWithArgs]
    testStub.oneArg.returns(_ => "test")
    testStub.oneArg(1)
    testStub.oneArg(2)
    testStub.oneArg(3)
    testStub.oneArg.calls should contain theSameElementsInOrderAs List(1, 2, 3)

  it should "work with methods that have multiple arguments" in:
    val testStub = stub[TestTraitWithArgs]
    testStub.twoArgs.returns:
      case (1, "one") => 1
      case (2, "two") => 2
      case _ => 0
    testStub.twoArgs(1, "one") should be(1)
    testStub.twoArgs(2, "two") should be(2)
    testStub.twoArgs(3, "three") should be(0)

  it should "set a constant return value with returnsWith" in:
    val testStub = stub[TestTraitWithArgs]
    testStub.oneArg.returnsWith("constant")
    testStub.oneArg(1) should be("constant")
    testStub.oneArg(42) should be("constant")
    testStub.oneArg(100) should be("constant")

  it should "track call order with isBefore and isAfter" in:
    implicit val callLog: CallLog = new CallLog()
    val testStub = stub[TestTraitWithArgs]

    testStub.oneArg.returns(_ => "test")
    testStub.twoArgs.returns(_ => 42)

    testStub.oneArg(1)
    testStub.twoArgs(2, "two")

    testStub.oneArg.isBefore(testStub.twoArgs) should be(true)
    testStub.twoArgs.isAfter(testStub.oneArg) should be(true)
    testStub.twoArgs.isBefore(testStub.oneArg) should be(false)
    testStub.oneArg.isAfter(testStub.twoArgs) should be(false)

  it should "set result depending on number of call" in:
    val testStub = stub[TestTraitWithArgs]

    testStub.oneArg.returnsOnCall:
      case 1 => "1"
      case _ => "2"

    testStub.oneArg(100) shouldBe "1"
    testStub.oneArg(100) shouldBe "2"
    testStub.oneArg(100) shouldBe "2"
