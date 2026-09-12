package com.paulbutcher.test.mock

import com.paulbutcher.test.TestTrait
import org.scalamock.function.FunctionAdapter1
import org.scalatest.matchers.should.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.freespec.AnyFreeSpec

class ByNameParametersTest extends AnyFreeSpec with MockFactory with Matchers {

  autoVerify = false

  "cope with methods with by name parameters" in {
    withExpectations {
      val m = mock[TestTrait]
      (m.byNameParam _).expects(*).returning("it worked")
      assertResult("it worked") { m.byNameParam(42) }
    }
  }

  "match methods with by name parameters" in {
    withExpectations {
      trait ByNameParam {
        def byNameParam(x: => Int): String
      }

      val m = mock[ByNameParam]
      val f: (=> Int) => Boolean = { x => x == 1 && x == 2  }
      ((m.byNameParam _): (=> Int) => String).expects(new FunctionAdapter1(f)).returning("it works")
      var y = 0
      assertResult("it works") { m.byNameParam { y += 1; y } }
    }
  }
}
