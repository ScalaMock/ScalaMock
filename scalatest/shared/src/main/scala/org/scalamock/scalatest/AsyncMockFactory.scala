package org.scalamock.scalatest

import org.scalamock.clazz.Mock
import org.scalatest.AsyncTestSuite

trait AsyncMockFactory extends AbstractAsyncMockFactory with Mock { this: AsyncTestSuite =>

}
