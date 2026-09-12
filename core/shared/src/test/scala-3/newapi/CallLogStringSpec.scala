package newapi

import org.scalamock.stubs.*
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CallLogStringSpec extends AnyFunSpec, Matchers, Stubs:
  trait FirstTrait:
    def foo(x: Int, y: Int): Int

    def foo2(x: Int): Int

  trait SecondTrait:
    def bar(x: String): String

    def bar0(): String

    def bar00: Int

  it("log methods"):
    given log: CallLog = CallLog()
    val first = stub[FirstTrait]
    val second = stub[SecondTrait]

    first.foo.returns(_ => 0)
    first.foo2.returns(_ => 0)
    second.bar.returns(_ => "1")
    (() => second.bar0()).returnsWith("2")
    (() => second.bar00).returnsWith(3)

    first.foo(0, 0)
    second.bar("1")
    first.foo2(2)
    second.bar00
    second.bar0()

    log.toString shouldBe
      """<stub-1> FirstTrait.foo(x: Int, y: Int)Int
        |<stub-2> SecondTrait.bar(x: String)String
        |<stub-1> FirstTrait.foo2(x: Int)Int
        |<stub-2> SecondTrait.bar00 => Int
        |<stub-2> SecondTrait.bar0()String""".stripMargin

