// Copyright (c) 2011-2025 ScalaMock Contributors (https://github.com/ScalaMock/ScalaMock/graphs/contributors)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package org.scalamock.kyotest.macros

import org.scalamock.context.MockContext
import org.scalamock.kyotest.internal.KyoMockFactory

import java.util.concurrent.locks.ReentrantLock
import scala.quoted.*

object LayeredMockMacros {

  inline def mock[A](using mockContext: MockContext): ULayer[A] = 
    ${ mockImpl[A]('mockContext) }

  inline def mockWithName[A](name: String)(using mockContext: MockContext): ULayer[A] = 
    ${ mockWithNameImpl[A]('name, 'mockContext) }

  inline def stub[A](using mockContext: MockContext): ULayer[A] = 
    ${ stubImpl[A]('mockContext) }

  inline def stubWithName[A](name: String)(using mockContext: MockContext): ULayer[A] = 
    ${ stubWithNameImpl[A]('name, 'mockContext) }

  def mockImpl[A: Type](mockContext: Expr[MockContext])(using Quotes): Expr[ULayer[A]] = {
    '{
      ZLayer.succeed {
        KyoMockFactory.mock[A](using $mockContext)
      }
    }
  }

  def mockWithNameImpl[A: Type](name: Expr[String], mockContext: Expr[MockContext])(using Quotes): Expr[ULayer[A]] = {
    '{
      ZLayer.succeed {
        KyoMockFactory.mock[A]($name)(using $mockContext)
      }
    }
  }

  def stubImpl[A: Type](mockContext: Expr[MockContext])(using Quotes): Expr[ULayer[A]] = {
    '{
      ZLayer.succeed {
        KyoMockFactory.stub[A](using $mockContext)
      }
    }
  }

  def stubWithNameImpl[A: Type](name: Expr[String], mockContext: Expr[MockContext])(using Quotes): Expr[ULayer[A]] = {
    '{
      ZLayer.succeed {
        KyoMockFactory.stub[A]($name)(using $mockContext)
      }
    }
  }

  // Lock is used to make expectations calls for different mocks sequential.
  // Inside scalamock scala.collection.mutable.ListBuffer is used to store expectations, it's not thread-safe.
  // Kyo can create layers in parallel, for example:
  // val mock1 = Kyo.serviceWith[Service1] { mock =>
  //   setExpectations1(mock)
  // }
  //
  // val mock2 = Kyo.serviceWith[Service2] { mock =>
  //   setExpectations2(mock)
  // }
  //
  // testCode.provide(createMockedLayer(mock1), createMockedLayer(mock2))
  //
  // In the example above setExpectations1 and setExpectations2 can be called in parallel.
  // To prevent this from losing expectations, we get a lock before each expectations call.
  private val lock = new ReentrantLock()

  private val scalamockKyotestInternalWithLock: URIO[Scope, Unit] =
    Kyo.acquireRelease(Kyo.succeed(lock.lock()))(_ => Kyo.succeed(lock.unlock()))

  def createMockedLayer[A: Type](expectations: Expr[URIO[A, Any]], tag: Expr[Tag[A]], mockContext: Expr[MockContext])(using Quotes): Expr[ULayer[A]] = {
    '{
      ZLayer.succeed {
        KyoMockFactory.mock[A](using $mockContext)
      } >+> ZLayer.fromKyo {
        Kyo.scoped {
          LayeredMockMacros.scalamockKyotestInternalWithLock *> $expectations
        }
      }
    }
  }
} 