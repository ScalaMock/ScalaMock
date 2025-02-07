package org.scalamock.scalamockz

import org.scalamock.clazz.Mock
import org.scalamock.context.{CallLog, MockContext}
import org.scalamock.handlers.{Handlers, OrderedHandlers, UnorderedHandlers}
import org.scalamock.scalamockz.ScalamockZFactory.ExpectationException
import org.scalamock.scalamockz.macros.CheckEffectInvocationMacros
import org.scalatest.exceptions.{StackDepthException, TestFailedException}
import zio.{IO, UIO, ULayer, ZIO, ZLayer}

/**
 * Provides a MockContext for creating mocks in zio-test.
 * Can initialize the MockContext for use in tests.
 * Also verifies mock invocations.
 */
sealed trait ScalamockZFactory {
  // It has to be made public so that the RunMockedMacros macro works from tests.
  val mockContext: MockContext
  private[scalamockz] def initializeExpectations(): UIO[Unit]
  private[scalamockz] def verifyExpectations(): IO[ExpectationException, Unit]
  private[scalamockz] def inSequence[T](what: => T): T
}

object ScalamockZFactory extends Mock {

  type ExpectationException = TestFailedException

  private[scalamockz] val live: ULayer[ScalamockZFactory] = ZLayer.succeed(new Live())
  private[scalamockz] val dummy: ULayer[ScalamockZFactory] = ZLayer.succeed(Dummy)

  import scala.language.experimental.macros
  import scala.language.implicitConversions

  override def mock[T](implicit mockContext: MockContext): T = macro CheckEffectInvocationMacros.mock[T]
  override def stub[T](implicit mockContext: MockContext): T = macro CheckEffectInvocationMacros.stub[T]

  override def mock[T](mockName: String)(implicit mockContext: MockContext): T =
    macro CheckEffectInvocationMacros.mockWithName[T]

  override def stub[T](mockName: String)(implicit mockContext: MockContext): T =
    macro CheckEffectInvocationMacros.stubWithName[T]

  private[scalamockz] class Live extends ScalamockZFactory with MockContext {

    val mockContext: MockContext = this

    // todo: copy-paste from org.scalamock.scalatest, it shouldn't be here
    override type ExpectationException = ScalamockZFactory.ExpectationException

    private def failedCodeStackDepthFn(methodName: Option[Symbol]): StackDepthException => Int = e => {
      e.getStackTrace.indexWhere { s =>
        !s.getClassName.startsWith("org.scalamock") && !s.getClassName.startsWith("org.scalatest") &&
        !(s.getMethodName == "newExpectationException") && !(s.getMethodName == "reportUnexpectedCall") &&
        methodName.forall(s.getMethodName != _.name)
      }
    }

    override protected def newExpectationException(message: String, methodName: Option[Symbol]) =
      new TestFailedException((_: StackDepthException) => Some(message), None, failedCodeStackDepthFn(methodName))

    // Copy-paste from org.scalamock.MockFactoryBase
    def initializeExpectations(): UIO[Unit] = ZIO.succeed {
      val initialHandlers = new UnorderedHandlers
      callLog = new CallLog

      expectationContext = initialHandlers
      currentExpectationContext = initialHandlers
    }

    private def clearExpectations(): UIO[Unit] = ZIO.succeed {
      // to forbid setting expectations after verification is done
      callLog = null
      expectationContext = null
      currentExpectationContext = null
    }

    def verifyExpectations(): IO[ExpectationException, Unit] = {
      for {
        _ <- ZIO.succeed(callLog.foreach { call =>
          val _ = expectationContext.verify(call)
        })
        oldCallLog = callLog
        oldExpectationContext = expectationContext
        _ <- clearExpectations()
        _ <- ZIO.when(!oldExpectationContext.isSatisfied) {
          ZIO.fail(
            newExpectationException(s"Unsatisfied expectation:\n\n${errorContext(oldCallLog, oldExpectationContext)}")
          )
        }
      } yield ()

    }

    override private[scalamockz] def inSequence[T](what: => T): T = {
      inContext(new OrderedHandlers)(what)
    }

    private def inContext[T](context: Handlers)(what: => T): T = {
      currentExpectationContext.add(context)
      val prevContext = currentExpectationContext
      currentExpectationContext = context
      val r = what
      currentExpectationContext = prevContext
      r
    }
  }

  /**
   * A placeholder to prevent compiler errors in ScalamockZSpec.
   * It is not actually used.
   */
  private[scalamockz] object Dummy extends ScalamockZFactory {
    override lazy val mockContext: MockContext = ???
    override private[scalamockz] def initializeExpectations(): UIO[Unit] = ???
    override private[scalamockz] def verifyExpectations(): UIO[Unit] = ???
    override private[scalamockz] def inSequence[T](what: => T): T = ???
  }
}
