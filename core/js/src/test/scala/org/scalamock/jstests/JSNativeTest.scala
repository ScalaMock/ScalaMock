package org.scalamock.jstests

import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@js.native
@JSGlobal
class FakeJSNativeClass extends js.Object {
  def fillText(text: String, x: Double, y: Double, maxWidth: Double = js.native): Unit = js.native
}

class JSNativeTest extends AnyFlatSpec with MockFactory with Matchers {
  it should "create a mock for method with 'js.native' default args" in {
    js.Dynamic.global.require("./core/js/src/test/js/fake-js-natives.js")
    val m = mock[FakeJSNativeClass]
    (m.fillText _).expects("hello", 1.0, 2.0, 3.0).returning(()).once()
    m.fillText("hello", 1.0, 2.0, 3.0)
  }
}
