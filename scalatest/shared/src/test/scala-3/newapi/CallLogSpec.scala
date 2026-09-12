package newapi

import org.scalamock.stubs.{CallLog, Stubs}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CallLogSpec extends AnyFunSpec with Matchers with Stubs {
  trait FirstTrait {
    def foo(x: Int, y: Int): Int

    def foo2(x: Int): Int
  }

  trait SecondTrait {
    def bar(x: String): String

    def bar0(): String

    def bar00: Int
  }

  it("verify") {
    implicit val log: CallLog = CallLog()

    val first = stub[FirstTrait]
    val second = stub[SecondTrait]

    first.foo.returns(_ => 0)
    first.foo2.returns(_ => 0)
    second.bar.returns(_ => "1")

    val bar0Stubbed = stubbed(() => second.bar0())
    val bar00Stubbed = stubbed(() => second.bar00)

    bar0Stubbed.returnsWith("2")
    bar00Stubbed.returnsWith(3)

    first.foo(0, 0)
    second.bar("1")
    first.foo2(2)
    second.bar0()
    second.bar00
    first.foo(1, 1)

    second.bar.isBefore(first.foo2) shouldBe true
    second.bar.isAfter(first.foo2) shouldBe false

    first.foo2.isBefore(second.bar) shouldBe false
    first.foo2.isAfter(second.bar) shouldBe true

    first.foo.isBefore(second.bar) shouldBe true
    first.foo.isAfter(second.bar) shouldBe true

    second.bar.isAfter(first.foo) shouldBe true
    second.bar.isBefore(first.foo) shouldBe true

    (() => second.bar00).isAfter(() => second.bar0()) shouldBe true
    bar0Stubbed.isBefore(bar00Stubbed) shouldBe true

  }
}
