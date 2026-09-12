package com.paulbutcher.test

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec

class VarSpec extends AnyFunSpec with MockFactory {

  trait Vars {
    var aVar: Int = scala.compiletime.uninitialized
    var concreteVar = "foo"
  }

  it("mock traits with vars") {
    val m = mock[Vars]
    m.aVar = 6
    m.concreteVar = "bar"
  }

}
