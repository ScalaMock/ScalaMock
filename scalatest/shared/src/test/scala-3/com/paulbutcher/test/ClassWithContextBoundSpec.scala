package com.paulbutcher.test

import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec

import scala.reflect.ClassTag

class ClassWithContextBoundSpec extends AnyFunSpec with MockFactory {

  it("compile without args") {
    class ContextBounded[T: ClassTag] {
      def method(x: Int): Unit = ()
    }

    val m = mock[ContextBounded[String]]

  }

  it("compile with args") {
    class ContextBounded[T: ClassTag](x: Int) {
      def method(x: Int): Unit = ()
    }

    val m = mock[ContextBounded[String]]

  }

  it("compile with provided explicitly type class") {
    class ContextBounded[T](x: ClassTag[T]) {
      def method(x: Int): Unit = ()
    }

    val m = mock[ContextBounded[String]]

  }

}
