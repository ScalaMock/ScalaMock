package org.scalamock.scalamockz.internal

import org.scalamock.context.MockContext

/**
 * A placeholder to prevent compiler errors in ScalamockZSpec.
 * It is not actually used.
 */
private[scalamockz] trait MockContextStub extends MockContext {
  override type ExpectationException = Throwable
  override protected def newExpectationException(message: String, methodName: Option[Symbol]) = ???
}
