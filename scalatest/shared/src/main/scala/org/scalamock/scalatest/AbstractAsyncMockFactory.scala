package org.scalamock.scalatest

import org.scalatest._
import org.scalatest.exceptions.{StackDepthException, TestFailedException}

import scala.concurrent.Future
import scala.util.control.NonFatal

trait AbstractAsyncMockFactory extends AsyncTestSuiteMixin with AsyncMockFactoryBase { this: AsyncTestSuite =>

  type ExpectationException = TestFailedException

  abstract override def withFixture(test: NoArgAsyncTest): FutureOutcome = {
    if (autoVerify) {
      new FutureOutcome(withExpectations(super.withFixture(test).toFuture).recoverWith({
        case NonFatal(ex) => Future.successful(Exceptional(ex))
      }))
    } else {
      super.withFixture(test)
    }
  }

  protected def newExpectationException(message: String, methodName: Option[Symbol]) =
    new TestFailedException((_: StackDepthException) => Some(message), None, failedCodeStackDepthFn(methodName))

  protected var autoVerify = true

}
