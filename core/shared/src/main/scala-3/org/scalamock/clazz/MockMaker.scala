// Copyright (c) ScalaMock Contributors (https://github.com/ScalaMock/ScalaMock/graphs/contributors)
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

package org.scalamock.clazz

import org.scalamock.context.MockContext

import scala.quoted.*
import scala.reflect.Selectable

private[clazz] object MockMaker:
  val MockDefaultNameValName = "mock$special$mockName"

  def instance[T: Type](mockType: MockType, ctx: Expr[MockContext], name: Option[Expr[String]])(using quotes: Quotes): Expr[T] =
    val utils = MakerUtils(using quotes)
    import utils.quotes.reflect.*
    val tpe = TypeRepr.of[T]

    def createDefaultMockNameSymbol(classSymbol: Symbol) =
      Symbol.newVal(classSymbol, MockDefaultNameValName, TypeRepr.of[String], Flags.EmptyFlags, Symbol.noSymbol)

    val instance = utils.buildInstance[T](
      anonName = "$anon",
      extraDecls = (classSymbol, _) => List(createDefaultMockNameSymbol(classSymbol)),
      extraBody = (classSymbol, _) =>
        val defaultMockNameSymbol = classSymbol.declaredField(MockDefaultNameValName)
        List(
          ValDef(
            defaultMockNameSymbol,
            Some(
              name
                .getOrElse('{ ${ctx}.generateMockDefaultName(${ Expr(mockType.toString.toLowerCase) }).name })
                .asTerm
            )
          )
        )
      ,
      methodDecls = (classSymbol, definition) =>
        val mockFunctionClassSym =
          Symbol.classSymbol(s"org.scalamock.function.${mockType}Function${definition.rawTypes.length}")

        val mockFunctionSym =
          Symbol.newVal(
            parent = classSymbol,
            name = definition.mockValName,
            tpe = TypeTree.ref(mockFunctionClassSym).tpe
              .appliedTo(List.fill(definition.rawTypes.length + 1)(TypeRepr.of[Any])),
            flags = Flags.EmptyFlags,
            privateWithin = Symbol.noSymbol
          )

        val overrideSym =
          if definition.symbol.isValDef then
            Symbol.newVal(
              parent = classSymbol,
              name = definition.symbol.name,
              tpe = This(classSymbol).tpe.memberType(definition.symbol),
              flags = Flags.Override,
              privateWithin = Symbol.noSymbol
            )
          else
            Symbol.newMethod(
              parent = classSymbol,
              name = definition.symbol.name,
              tpe = This(classSymbol).tpe.memberType(definition.symbol),
              flags = Flags.Override,
              privateWithin = Symbol.noSymbol
            )

        List(mockFunctionSym, overrideSym)
      ,
      methodBody = (classSymbol, definition) =>
        val defaultMockNameSymbol = classSymbol.declaredField(MockDefaultNameValName)

        val mockFunctionValDef: ValDef =
          val valSym = classSymbol.declaredField(definition.mockValName)
          val mockFunctionClassSymbol = valSym.typeRef.classSymbol.get
          val mockFunctionUniqueName = '{
            scala.Symbol(
              Predef.augmentString("<%s> %s%s.%s%s")
                .format(
                  ${ Ref(defaultMockNameSymbol).asExpr },
                  ${ Expr(tpe.typeSymbol.name) },
                  ${ Expr(if (tpe.typeArgs.isEmpty) "" else "[%s]".format(tpe.typeArgs.map(_.show(using Printer.TypeReprShortCode)).mkString(","))) },
                  ${ Expr(definition.symbol.name) },
                  ${ Expr {
                    definition.tpe match
                      case PolyType(params, _, _) => params.mkString("[", ",", "]")
                      case _ => ""
                  }
                  }
                )
            )
          }
          ValDef(
            symbol = valSym,
            rhs = Some(
              Apply(
                TypeApply(
                  Select(
                    New(TypeIdent(mockFunctionClassSymbol)),
                    mockFunctionClassSymbol.primaryConstructor
                  ),
                  List.fill(definition.rawTypes.length + 1)(TypeTree.of[Any])
                ),
                List(ctx.asTerm, mockFunctionUniqueName.asTerm)
              )
            )
          )

        val definitionOverride =
          if (definition.symbol.isValDef)
            ValDef(definition.symbol.overridingSymbol(classSymbol), Some('{ null }.asTerm))
          else
            DefDef(
              definition.symbol.overridingSymbol(classSymbol),
              { args =>
                Some(
                  TypeApply(
                    Select.unique(
                      Apply(
                        Select.unique(Ref(mockFunctionValDef.symbol), "apply"),
                        args.flatten.collect { case t: Term => t }
                      ),
                      "asInstanceOf"
                    ),
                    definition.prepareResType(classSymbol, args)
                      .asType match { case '[t] => List(TypeTree.of[t]) }
                  )
                )
              }
            )
        List(mockFunctionValDef, definitionOverride)
    )

    instance.asExprOf[T & Selectable]