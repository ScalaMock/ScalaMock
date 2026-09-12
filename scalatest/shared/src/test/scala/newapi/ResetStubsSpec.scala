package newapi

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalamock.stubs.Stubs

class ResetStubsSpec extends AnyFunSpec with Matchers with Stubs {
  trait FirstTrait {
    def foo(x: Int, y: Int): Int

    def foo2(x: Int): Int
  }
  
  it("should reset stubs") {
    val first = stub[FirstTrait]

    (first.foo _).returns(_ => 3)
    (first.foo2 _).returns(_ => 2)
    
    first.foo(1, 2)
    first.foo2(1)

    (first.foo _).times shouldBe 1
    (first.foo2 _).times shouldBe 1
    
    resetStubs()

    (first.foo _).times shouldBe 0
    (first.foo2 _).times shouldBe 0
  }
}
