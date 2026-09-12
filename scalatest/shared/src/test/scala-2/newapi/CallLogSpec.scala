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

    (first.foo _).returns(_ => 0)
    (first.foo2 _).returns(_ => 0)
    (second.bar _).returns(_ => "1")
    (() => second.bar0()).returnsWith("2")
    (() => second.bar00).returnsWith(3)

    first.foo(0, 0)
    second.bar("1")
    first.foo2(2)
    second.bar0()
    second.bar00
    first.foo(1, 1)

    (second.bar _).isBefore(first.foo2 _) shouldBe true
    (second.bar _).isAfter(first.foo2 _) shouldBe false

    (first.foo2 _).isBefore(second.bar _) shouldBe false
    (first.foo2 _).isAfter(second.bar _) shouldBe true

    (first.foo _).isBefore(second.bar _) shouldBe true
    (first.foo _).isAfter(second.bar _) shouldBe true

    (second.bar _).isAfter(first.foo _) shouldBe true
    (second.bar _).isBefore(first.foo _) shouldBe true

    (() => second.bar00).isAfter(() => second.bar0()) shouldBe true
    (() => second.bar0()).isBefore(() => second.bar00) shouldBe true

  }
}
