package org.scalamock.scalatest

import org.scalamock.clazz.{Mock => MacroMock}
import org.scalamock.proxy.ProxyMockFactory
import org.scalatest.TestSuite

import scala.reflect.ClassTag

/**
  * allows combining of macro mocks wih proxy mocks in the same Suite
  * {{{
  * val macroMock = mock[Foo]
  * val proxyMock = Proxy.mock[Bar]
  * }}}
  */
trait MixedMockFactory extends AbstractMockFactory with MacroMock { this: TestSuite =>

  @deprecated(message = "proxy mocks are deprecated")
  object Proxy extends ProxyMockFactory {
    import org.scalamock.proxy._
    def mock[T: ClassTag]: T with Mock = super.mock[T]
    def stub[T: ClassTag]: T with Stub = super.stub[T]
  }
}
