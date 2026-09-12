package mock

import com.paulbutcher.test.TestTrait
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ByNameParametersTest extends AnyFunSpec with MockFactory with Matchers {

  autoVerify = false

  it("cope with methods with by name parameters") {
    withExpectations {
      val m = mock[TestTrait]
      (m.byNameParam(_: Int)).expects(*).returning("it worked")
      assertResult("it worked") {
        m.byNameParam(42)
      }
    }
  }

  it("match methods with by name parameters") {
    withExpectations {
      val m = mock[TestTrait]
      (m.byNameParam(_: Int)).expects(where[Int](Set(1, 2))).returning("it works")
      var y = 0
      assertResult("it works") {
        m.byNameParam {
          y += 1; y
        }
      }
    }
  }
}
