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

package org.scalamock.stubs.internal

import org.scalamock.clazz.{MakerUtils, Utils}
import org.scalamock.stubs.{CallLog, Stub, StubIO, StubbedMethod}

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.quoted.{Expr, Quotes, Type}

private[stubs] class StubMaker(
  using Quotes
) extends MakerUtils:
  import quotes.reflect.*

  override def newApi = true

  def newInstance[T: Type](
    collector: Expr[CreatedStubs],
    stubUniqueIndexGenerator: Expr[StubUniqueIndexGenerator]
  ): Expr[Stub[T]] =
    val instance = buildInstance[T](
      anonName = "anon",
      extraDecls = (classSymbol, _) => List(
        // Creates a method to clear this stub
        // def stub$macro$clear(): Unit
        Symbol.newMethod(
          parent = classSymbol,
          name = ClearStubsMethodName,
          tpe = MethodType(Nil)(_ => Nil, _ => TypeRepr.of[Unit]),
          flags = Flags.EmptyFlags,
          privateWithin = Symbol.noSymbol
        ),
        // Creates a field to store unique index for this stub
        // val stub$macro$idx: Int
        Symbol.newVal(
          parent = classSymbol,
          name = UniqueStubIdx,
          tpe = TypeRepr.of[Int],
          flags = Flags.EmptyFlags,
          privateWithin = Symbol.noSymbol
        )
      ),
      extraBody = (classSymbol, methods) => List(
        // Creates a method to clear this stub
        // val stub$<name>$<idx>
        // def stub$macro$clear(): Unit = stub$<name>$<idx>.clear()
        DefDef(
          symbol = classSymbol.methodMember(ClearStubsMethodName).head,
          _ =>
            Some(
              Block(
                methods.map { method => '{ ${ method.stubbedMethodRef(classSymbol) }.clear() }.asTerm },
                '{}.asTerm
              )
            )
        ),
        ValDef(
          symbol = classSymbol.declaredField(UniqueStubIdx),
          Some('{ $stubUniqueIndexGenerator.nextIdx() }.asTerm)
        )
      ),
      methodDecls = (classSymbol, method) => List(
        // Creates a field to store stubbed method representation
        // val stub$<name>$<idx>: StubbedMethod.Internal[Any, Any]
        Symbol.newVal(
          parent = classSymbol,
          name = method.stubValName,
          tpe = TypeRepr.of[StubbedMethod.Internal[Any, Any]],
          flags = Flags.EmptyFlags,
          privateWithin = Symbol.noSymbol
        ),
        // Creates an override for the val
        // override val <name>: type
        if method.symbol.isValDef then
          Symbol.newVal(
            parent = classSymbol,
            name = method.symbol.name,
            tpe = This(classSymbol).tpe.memberType(method.symbol),
            flags = Flags.Override,
            privateWithin = Symbol.noSymbol
          )
        else
          // Creates an implementation for the method, which will use StubbedMethod.Internal under the hood
          // override def <name>(...args): type
          Symbol.newMethod(
            parent = classSymbol,
            name = method.symbol.name,
            tpe = This(classSymbol).tpe.memberType(method.symbol),
            flags = Flags.Override,
            privateWithin = Symbol.noSymbol
          ),
      ),
      methodBody = (classSymbol, method) => List(
        ValDef(
          classSymbol.declaredField(method.stubValName),
          Some {
            val callLog = Expr.summon[CallLog] match {
              case Some(log) => '{ Some(${log}) }
              case None => '{ None }
            }
            val idx = Ref(classSymbol.declaredField(UniqueStubIdx)).asExprOf[Int]
            val ioOpt = Expr.summon[StubIO].filter(_.isMatches(method.rawResType)) match
              case Some(io) => '{ Some($io) }
              case None => '{None}

            '{
              new StubbedMethod.Internal[Any, Any](
                s"<stub-" + $idx + "> " + ${ Expr(method.show) },
                $callLog,
                $ioOpt
              )
            }.asTerm
          }
        ),
        if (method.symbol.isValDef)
          ValDef(method.symbol.overridingSymbol(classSymbol), Some('{ null }.asTerm))
        else
          DefDef(
            symbol = method.symbol.overridingSymbol(classSymbol),
            params => Some {
              method.prepareResType(classSymbol, params).asType match
                case '[res] =>
                  val tupledArgs = tupled(params.flatMap {_.collect { case term: Term => term }}).asExpr
                  '{ ${ method.stubbedMethodRef(classSymbol) }.impl(${tupledArgs}).asInstanceOf[res] }
                    .asTerm
                    .changeOwner(method.symbol.overridingSymbol(classSymbol))
            }
          )
      )
    )
    //println(instance.show)
    '{
      ${ collector }.bind(${ instance.asExprOf[T] }.asInstanceOf[Stub[T]])
    }
  end newInstance

  @tailrec
  private def tupleTypeToList(
    tpe: TypeRepr,
    acc: mutable.ListBuffer[TypeRepr] = new ListBuffer[TypeRepr]
  ): List[TypeRepr] =
    tpe.asType match
      case _ if acc.length > 1000 =>
        report.errorAndAbort("Infinite loop occurred. Provide type arguments explicitly")
      case '[h *: t] =>
        tupleTypeToList(TypeRepr.of[t], acc += TypeRepr.of[h])
      case '[EmptyTuple] =>
        acc.toList
      case _ =>
        (acc += tpe).toList


  def getStubbed00[R: Type](select: Expr[R]): Expr[StubbedMethod[Unit, R]] =
    searchTermWithMethod(select.asTerm, Nil).selectReflect[StubbedMethod[Unit, R]](_.stubValName)

  def getStubbed0[R: Type](select: Expr[() => R]): Expr[StubbedMethod[Unit, R]] =
    searchTermWithMethod(select.asTerm, Nil).selectReflect[StubbedMethod[Unit, R]](_.stubValName)

  def getStubbed1[T1: Type, R: Type](select: Expr[Any]): Expr[StubbedMethod[T1, R]] =
    searchTermWithMethod(select.asTerm, List(TypeRepr.of[T1])).selectReflect[StubbedMethod[T1, R]](_.stubValName)

  def getStubbed[Args: Type, R: Type](select: Expr[Any]): Expr[StubbedMethod[Args, R]] =
    searchTermWithMethod(select.asTerm, tupleTypeToList(TypeRepr.of[Args])).selectReflect[StubbedMethod[Args, R]](_.stubValName)

  private def tupled(args: List[Term]): Term =
    args match
      case Nil => '{()}.asTerm
      case oneArg :: Nil => oneArg
      case args =>
        args.foldRight[Term]('{ EmptyTuple }.asTerm) { (el, acc) =>
          Select
            .unique(acc, "*:")
            .appliedToTypes(List(el.tpe, acc.tpe))
            .appliedToArgs(List(el))
        }


  extension (method: MockableDefinition)
    private def stubbedMethodRef(classSymbol: Symbol): Expr[StubbedMethod.Internal[Any, Any]] =
      Ref(classSymbol.declaredField(method.stubValName)).asExprOf[StubbedMethod.Internal[Any, Any]]

    private def show: String =
      given Printer[TypeRepr] = Printer.TypeReprShortCode
      s"${method.ownerTpe.show}.${method.symbol.name}${method.tpe.show}"


  extension (io: Expr[StubIO])
    private def isMatches(resTpe: TypeRepr): Boolean =
      resTpe <:< AppliedType(
        io.asTerm.tpe.select(io.asTerm.tpe.typeSymbol.typeMember("F")),
        List(TypeRepr.of[Any], TypeRepr.of[Any])
      )
