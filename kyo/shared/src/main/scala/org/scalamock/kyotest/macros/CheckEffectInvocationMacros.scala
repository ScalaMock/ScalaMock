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

// placed in org.scalamock.clazz package to access MockMaker
package org.scalamock.kyotest.macros

import org.scalamock.clazz.{MockMaker, MockType}
import org.scalamock.context.MockContext

import scala.annotation.experimental
import scala.quoted.*

import kyo.Kyo

/**
 * Creates mocks that check if an effect was called.
 *
 * Using classic scalamock such code wouldn't cause a test failure:
 *
 * {{{
 * // test
 * val m = mock[Service]
 * (m.f _).expects(42).returning(Kyo.succeed(42))
 *
 * // production
 * for {
 *   _ <- Kyo.unit
 *   _ = service.f(42) // effect is not called!
 * } yield ()
 * }}}
 *
 * This macro validates that the mocked effect is actually called,
 * by wrapping the method in suspendSucceed().
 *
 * How it works. A regular mock generates such code (for simplicity, it's more complex in reality):
 *
 * {{{
 * // inside scalamock
 * var callLog = new CallLog
 *
 * // generated in test, instead of mock[Service]
 * val m = new Mock[Service] {
 *   def f(x: Int): UIO[String] = {
 *     val call = new Call(this, arguments)
 *     callLog += call
 *   }
 * }
 * }}}
 *
 * So when the method is called, it's added to callLog, and scalamock thinks that the method was called.
 *
 * We wrap this mock like this:
 *
 * {{{
 * val m = new Mock[Service] {
 *   def f(x: Int): UIO[String] = Kyo.suspendSucceed {
 *     val call = new Call(this, arguments)
 *     callLog += call
 *   }
 * }
 * }}}
 *
 * So when the method is called, it's not added to callLog immediately.
 * It's added only when the effect f(x) is actually called.
 */
@experimental
private[scalamock] object CheckEffectInvocationMacros {

  def mock[T: Type](mockContext: Expr[MockContext])(using Quotes): Expr[T] = {
    transformAst(MockMaker.instance[T](MockType.Mock, mockContext, name = None))
  }

  def stub[T: Type](mockContext: Expr[MockContext])(using Quotes): Expr[T] = {
    transformAst(MockMaker.instance[T](MockType.Stub, mockContext, name = None))
  }

  def mockWithName[T: Type](mockName: Expr[String], mockContext: Expr[MockContext])(using Quotes): Expr[T] = {
    transformAst(MockMaker.instance[T](MockType.Mock, mockContext, name = Some(mockName)))
  }

  def stubWithName[T: Type](mockName: Expr[String], mockContext: Expr[MockContext])(using Quotes): Expr[T] = {
    transformAst(MockMaker.instance[T](MockType.Stub, mockContext, name = Some(mockName)))
  }

  /**
   * scalamock generates such code (can be seen with println(tree.show)):
   *
   * {{{
   * {
   *   class $anon extends java.lang.Object with org.scalamock.scalamockz.ScalamockZSpecSpec.Service with scala.reflect.Selectable {
   *     val mock$special$mockName: java.lang.String = given_MockContext.generateMockDefaultName("mock").name
   *     val mock$f$0: org.scalamock.function.MockFunction1[scala.Int, kyo.UIO[scala.Predef.String]] = new org.scalamock.function.MockFunction1[scala.Int, kyo.UIO[scala.Predef.String]](given_MockContext, scala.Symbol.apply(scala.Predef.augmentString("<%s> %s%s.%s%s").format($anon.this.mock$special$mockName, "Service", "", "f", "")))
   *     override def f(x: scala.Int): kyo.UIO[scala.Predef.String] = $anon.this.mock$f$0.apply(x).asInstanceOf[kyo.UIO[scala.Predef.String]]
   *   }
   *
   *   (new $anon(): org.scalamock.scalamockz.ScalamockZSpecSpec.Service & scala.reflect.Selectable)
   * }
   * }}}
   *
   * We traverse the tree, find the body of mocked Kyo methods, and then wrap them in suspendSucceed.
   */
  private def transformAst[T: Type](expr: Expr[T])(using quotes: Quotes): Expr[T] = {
    import quotes.reflect.*

    // documentation for TreeMap (it's not easy to find):
    // https://docs.scala-lang.org/scala3/reference/metaprogramming/reflection.html#tree-utilities-1
    val treeMap = new TreeMap {
      override def transformTerm(tree: Term)(owner: Symbol): Term = {
        if (tree.isExpr) {
          tree.asExpr match {
            // match this part of the generated code:
            // $anon.this.mock$f$0.apply(x).asInstanceOf[kyo.UIO[scala.Predef.String]]
            case '{ $treeExpr: Kyo[r, e, a] } =>
              val wrapped = '{ Kyo.suspendSucceed($treeExpr) }
              wrapped.asTerm
            // non-Kyo don't need to be wrapped
            case _ =>
              super.transformTerm(tree)(owner)
          }
        } else {
          super.transformTerm(tree)(owner)
        }
      }
    }

    treeMap.transformTree(expr.asTerm)(Symbol.spliceOwner).asExprOf[T]
  }
}
