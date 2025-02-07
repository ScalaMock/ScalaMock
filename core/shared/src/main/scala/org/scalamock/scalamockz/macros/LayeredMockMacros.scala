package org.scalamock.scalamockz
package macros

import scala.language.experimental.macros
import scala.reflect.macros.blackbox
import zio.{Tag, ULayer, URIO}

private[scalamockz] object LayeredMockMacros {

  def mockImpl[A: c.WeakTypeTag](
      c: blackbox.Context): c.Expr[ULayer[A]] = {
    import c.universe._

    val tpe = weakTypeOf[A]
    val factory = TermName(c.freshName("factory"))

    c.Expr[ULayer[A]](
      q"""
         _root_.zio.ZLayer.service[_root_.org.scalamock.scalamockz.ScalamockZFactory].flatMap {
           ($factory: _root_.zio.ZEnvironment[_root_.org.scalamock.scalamockz.ScalamockZFactory]) =>
             _root_.zio.ZLayer.succeed {
               _root_.org.scalamock.scalamockz.ScalamockZFactory.mock[$tpe]($factory.get.mockContext)
             }
         }
       """
    )
  }

  def stubImpl[A: c.WeakTypeTag](
      c: blackbox.Context): c.Expr[ULayer[A]] = {
    import c.universe._

    val tpe = weakTypeOf[A]
    val factory = TermName(c.freshName("factory"))

    c.Expr[ULayer[A]](
      q"""
         _root_.zio.ZLayer.service[_root_.org.scalamock.scalamockz.ScalamockZFactory].flatMap {
           ($factory: _root_.zio.ZEnvironment[_root_.org.scalamock.scalamockz.ScalamockZFactory]) =>
             _root_.zio.ZLayer.succeed {
               _root_.org.scalamock.scalamockz.ScalamockZFactory.stub[$tpe]($factory.get.mockContext)
             }
         }
       """
    )
  }

  def mockedImpl[A: c.WeakTypeTag](
      c: blackbox.Context
    )(expectations: c.Expr[URIO[A with ScalamockZFactory, Any]]
    )(tag: c.Expr[Tag[A]]): c.Expr[ULayer[A]] = {
    import c.universe._

    val tpe = weakTypeOf[A]
    val factory = TermName(c.freshName("factory"))

    c.Expr[ULayer[A]](
      q"""
         _root_.zio.ZLayer.service[_root_.org.scalamock.scalamockz.ScalamockZFactory].flatMap {
           ($factory: _root_.zio.ZEnvironment[_root_.org.scalamock.scalamockz.ScalamockZFactory]) =>
             _root_.zio.ZLayer.succeed {
               _root_.org.scalamock.scalamockz.ScalamockZFactory.mock[$tpe]($factory.get.mockContext)
             } >+> _root_.zio.ZLayer.fromZIO($expectations)
         }
       """
    )
  }
}
