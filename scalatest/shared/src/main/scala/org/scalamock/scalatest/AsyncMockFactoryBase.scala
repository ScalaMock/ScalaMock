package org.scalamock.scalatest

import org.scalamock.clazz.Mock
import org.scalamock.context.{CallLog, MockContext}
import org.scalamock.function.MockFunctions
import org.scalamock.handlers.UnorderedHandlers
import org.scalamock.matchers.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait AsyncMockFactoryBase extends MockContext with Mock with MockFunctions with Matchers {

  //! TODO - https://issues.scala-lang.org/browse/SI-5831
  implicit val _factory: AsyncMockFactoryBase = this

  implicit def executionContext: ExecutionContext

  initializeExpectations()

  private def initializeExpectations(): Unit = {
    val initialHandlers = new UnorderedHandlers
    callLog = new CallLog

    expectationContext = initialHandlers
    currentExpectationContext = initialHandlers
  }

  private def clearExpectations(): Unit = {
    // to forbid setting expectations after verification is done
    callLog = null
    expectationContext = null
    currentExpectationContext = null
  }


  def withExpectations[T](test: => Future[T]): Future[T] = {
    if (expectationContext == null) {
      // we don't reset expectations for the first test case to allow
      // defining expectations in Suite scope and writing tests in OneInstancePerTest/isolated style
      initializeExpectations()
    }

    val testResult = test map { result =>
      verifyExpectations()
      result
    }

    testResult onComplete {
      case Success(_) => ()
      case Failure(_) => clearExpectations()
    }

    testResult
  }

  private def verifyExpectations(): Unit = {
    callLog.foreach(expectationContext.verify(_))

    val oldCallLog = callLog
    val oldExpectationContext = expectationContext

    clearExpectations()

    if (!oldExpectationContext.isSatisfied)
      reportUnsatisfiedExpectation(oldCallLog, oldExpectationContext)
  }

}
