package org.scalamock.scalamockz
package macros

import org.scalamock.clazz.MockImpl.MockMaker
import org.scalamock.context.MockContext
import org.scalamock.util.MacroAdapter
import scala.language.existentials

/**
 * Creates mocks that validate whether an effect has actually been executed.
 *
 * In classic Scalamock, the following code will not cause the test to fail:
 *
 * {{{
 * // Test code
 * val m = mock[Service]
 * (m.f _).expects(42).returning(ZIO.succeed(42))
 *
 * // Production code
 * for {
 *   _ <- ZIO.unit
 *   _ = service.f(42) // The effect isn't actually executed!
 * } yield ()
 * }}}
 *
 * This macro ensures that the mocked effect is actually executed by wrapping the method in `suspendSucceed()`.
 *
 * How it works: A classic mock generates code similar to this (simplified for clarity):
 *
 * {{{
 * // Inside Scalamock
 * var callLog = new CallLog
 *
 * // Generated in the test, instead of mock[Service]
 * val m = new Mock[Service] {
 *   def f(x: Int): UIO[String] = {
 *     val call = new Call(this, arguments)
 *     callLog += call
 *   }
 * }
 * }}}
 *
 * When the method is called, its invocation is recorded in the callLog.
 * Scalamock considers the method as invoked.
 *
 * We wrap this mock like this:
 *
 * {{{
 * val m = new Mock[Service] {
 *   def f(x: Int): UIO[String] = ZIO.suspendSucceed {
 *     val call = new Call(this, arguments)
 *     callLog += call
 *   }
 * }
 * }}}
 *
 * Now, the method isn't immediately recorded in the callLog.
 * It is only added when the effect `f(x)` is actually executed.
 */
private[scalamockz] object CheckEffectInvocationMacros {

  import MacroAdapter.Context

  def mock[T: c.WeakTypeTag](c: Context)(mockContext: c.Expr[MockContext]): c.Expr[T] = {
    make[T](c)(mockContext)(stub = false, mockName = None)
  }

  def stub[T: c.WeakTypeTag](c: Context)(mockContext: c.Expr[MockContext]): c.Expr[T] = {
    make[T](c)(mockContext)(stub = true, mockName = None)
  }

  def mockWithName[T: c.WeakTypeTag](
      c: Context
    )(mockName: c.Expr[String]
    )(mockContext: c.Expr[MockContext]): c.Expr[T] = {
    make[T](c)(mockContext)(stub = false, mockName = Some(mockName))
  }

  def stubWithName[T: c.WeakTypeTag](
      c: Context
    )(mockName: c.Expr[String]
    )(mockContext: c.Expr[MockContext]): c.Expr[T] = {
    make[T](c)(mockContext)(stub = true, mockName = Some(mockName))
  }

  private def make[T: c.WeakTypeTag](
      c: Context
    )(mockContext: c.Expr[MockContext]
    )(stub: Boolean,
      mockName: Option[c.Expr[String]]): c.Expr[T] = {
    val maker = MockMaker[T](c)(mockContext, stub, mockName)
    val originalTree = maker.make
    val transformedTree = transformAst(c)(originalTree.tree)
    c.Expr[T](transformedTree)
  }

  /**
   * Scalamock generates code like this (you can inspect it using `println(tree)`):
   *
   * {{{
   * {
   *   final class $anon extends org.scalamock.scalamockz.ScalamockZSpecSpec.Service {
   *     def <init>() = {
   *       super.<init>();
   *       ()
   *     };
   *     val mock$special$mockName = factory$macro$1.get[org.scalamock.scalamockz.ScalamockZFactory]((zio.`package`.Tag.apply[org.scalamock.scalamockz.ScalamockZFactory]((izumi.reflect.Tag.apply[org.scalamock.scalamockz.ScalamockZFactory](classOf[org.scalamock.scalamockz.ScalamockZFactory], izumi.reflect.macrortti.LightTypeTag.parse[Nothing]((-1099772752: Int), ("\u0004\u0000\u0001*org.scalamock.scalamockz.ScalamockZFactory\u0001\u0001": String), ("\u0000\u0000\u0000": String), (30: Int))): izumi.reflect.Tag[org.scalamock.scalamockz.ScalamockZFactory])): zio.Tag[org.scalamock.scalamockz.ScalamockZFactory])).mockContext.generateMockDefaultName("mock").name;
   *     override def f(x: Int): zio.UIO[String] = $anon.this.mock$f$0.apply(x);
   *     val mock$f$0: MockFunction1[Int, zio.UIO[String]] = new MockFunction1[Int, zio.UIO[String]](factory$macro$1.get[org.scalamock.scalamockz.ScalamockZFactory]((zio.`package`.Tag.apply[org.scalamock.scalamockz.ScalamockZFactory]((izumi.reflect.Tag.apply[org.scalamock.scalamockz.ScalamockZFactory](classOf[org.scalamock.scalamockz.ScalamockZFactory], izumi.reflect.macrortti.LightTypeTag.parse[Nothing]((-1099772752: Int), ("\u0004\u0000\u0001*org.scalamock.scalamockz.ScalamockZFactory\u0001\u0001": String), ("\u0000\u0000\u0000": String), (30: Int))): izumi.reflect.Tag[org.scalamock.scalamockz.ScalamockZFactory])): zio.Tag[org.scalamock.scalamockz.ScalamockZFactory])).mockContext, scala.Symbol.apply(scala.Predef.augmentString("<%s> %s%s.%s%s").format($anon.this.mock$special$mockName, "Service", "", "f", "")))
   *   };
   *   new $anon()
   * }.asInstanceOf[org.scalamock.scalamockz.ScalamockZSpecSpec.Service]
   * }}}
   *
   * The macro traverses this tree, finds the body of mocked ZIO methods, and wraps them in suspendSucceed.
   */
  private def transformAst(c: Context)(tree: c.Tree): c.Tree = {
    import c.universe._

    object transformer extends Transformer {
      override def transform(tree: Tree): Tree = tree match {
        // Matching this part of the generated code:
        // override def f(x: Int): zio.UIO[String] = $anon.this.mock$f$0.apply(x);
        case DefDef(mods, name, tparams, vparamss, tpt, rhs) if isZIOType(c)(tpt.tpe) =>
          DefDef(mods, name, tparams, vparamss, tpt, q"_root_.zio.ZIO.suspendSucceed($rhs)")
        case _ => super.transform(tree)
      }
    }

    transformer.transform(tree)
  }

  private def isZIOType(c: Context)(tpe: c.Type): Boolean = {
    tpe != null && tpe.baseClasses.exists(_.fullName == "zio.ZIO")
  }

}
