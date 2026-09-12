package org.scalamock.test.scalatest

import org.scalamock.scalatest.MixedMockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

@deprecated(message = "proxy mocks are deprecated")
class MixedMockFactoryTest extends AnyFlatSpec with MixedMockFactory with Matchers {
  "mixed mocks" should "work" in {
    trait Foo {
      def getI: Int
    }
    val m = mock[Foo]
    val p = Proxy.mock[Foo]

    p.expects(Symbol("getI"))().returning(5).once()
    (() => m.getI).expects().returns(42).anyNumberOfTimes()

    m.getI should be(42)
    p.getI should be(5)
  }
}
