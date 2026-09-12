// Copyright (c) 2011-2015 ScalaMock Contributors (https://github.com/paulbutcher/ScalaMock/graphs/contributors)
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

package org.scalamock.util

import scala.reflect.macros.blackbox


/**
 * Helper functions to work with Scala macros and to create scala.reflect Trees.
 */
private[scalamock] class Utils[C <: blackbox.Context](val ctx: C, val newApi: Boolean = false) {
  import ctx.universe._

  private val namePrefix: String = if (newApi) "stub" else "mock"

  private val isScalaJs =
    ctx.compilerSettings
      .exists(o => o.startsWith("-Xplugin:") && o.contains("scalajs-compiler"))

  def scalaJSAnnotations(termName: TermName): List[Tree] =
    if (isScalaJs)
      List(q"new _root_.scala.scalajs.js.annotation.JSExport(${termName.encodedName.toString})")
    else
      Nil

  private def isJsAny(typeToMock: Type): Boolean =
    isScalaJs && typeToMock.baseClasses.exists(_.fullName == "scala.scalajs.js.Any")

  case class MockableDefinition(
    symbol: MethodSymbol,
    typeToMock: Type
  ) {
    val name: ctx.universe.TermName = {
      val index = typeToMock.member(symbol.name).asTerm.alternatives.indexOf(symbol)
      assert(index >= 0)
      TermName(namePrefix + "$" + symbol.name + "$" + index)
    }

    val annotations = if (isJsAny(typeToMock)) Nil else scalaJSAnnotations(name)

    val tpe = symbol.typeSignatureIn(
      internal.superType(internal.thisType(typeToMock.typeSymbol), typeToMock)
    )

    val resTpe = forwarderParamType(typeToMock, finalResultType(tpe))
  }

  private val jsObjectFullNames = Set("scala.scalajs.js.Object", "scala.scalajs.js.Any")

  private def isMemberOfJsObject(s: Symbol): Boolean =
    jsObjectFullNames.contains(s.owner.fullName)

  object MockableDefinitions {
    def apply(typeToMock: Type): List[MockableDefinition] = {
      val jsAny = isJsAny(typeToMock)
      typeToMock.members
        .filter(m => m.isMethod && !isMemberOfObject(m) && !(jsAny && isMemberOfJsObject(m)))
        .map(_.asMethod)
        .filter { m =>
          val flags = m.asInstanceOf[reflect.internal.HasFlags]
          !m.isConstructor && !m.isPrivate && m.privateWithin == NoSymbol &&
            !m.isFinal &&
            !flags.hasFlag(reflect.internal.Flags.BRIDGE) &&
            !m.isParamWithDefault && // see issue #43
            !m.annotations.exists(_.tree.tpe =:= typeOf[scala.deprecatedOverriding]) &&
            (!(m.isStable || m.isAccessor) || flags.isDeferred)
        }.toList
        .map(MockableDefinition(_, typeToMock))
    }
  }

  /**
   * Translates forwarder parameters into Trees.
   * Also maps Java repeated params into Scala repeated params.
   * Also handles path-dependent this types in type arguments.
   */
  def forwarderParamType(typeToMock: ctx.universe.Type, t: ctx.universe.Type): ctx.universe.Tree = t match {
    case TypeRef(pre, sym, args) if sym == definitions.JavaRepeatedParamClass =>
      TypeTree(internal.typeRef(pre, definitions.RepeatedParamClass, args))
    case TypeRef(_, sym, args) if isPathDependentThis(typeToMock, t) =>
      AppliedTypeTree(Ident(TypeName(sym.name.toString)), args.map(forwarderParamType(typeToMock, _)))
    case TypeRef(_, sym, args) if args.exists(arg => isThisType(typeToMock, arg) || arg.exists(isThisType(typeToMock, _))) =>
      AppliedTypeTree(Ident(sym), args.map(forwarderParamType(typeToMock, _)))
    case t if isThisType(typeToMock, t) =>
      SingletonTypeTree(This(TypeName("")))
    case _ =>
      TypeTree(t)
  }

  def isPathDependentThis(typeToMock: ctx.universe.Type, t: ctx.universe.Type): Boolean = t match {
    case TypeRef(pre, _, _) => isPathDependentThis(typeToMock, pre)
    case ThisType(tpe) => tpe == typeToMock.typeSymbol
    case _ => false
  }

  def wrapByNameParam(p: Symbol): Tree = {
    val ident = Ident(TermName(p.name.toString))
    p.typeSignature match {
      case TypeRef(_, sym, _) if sym == definitions.ByNameParamClass =>
        q"() => $ident"
      case _ =>
        ident
    }
  }

  /**
   * Checks if a type is a ThisType that refers to the type being mocked.
   */
  def isThisType(typeToMock: ctx.universe.Type, t: ctx.universe.Type): Boolean = t match {
    case ThisType(tpe) => tpe == typeToMock.typeSymbol
    case _ => false
  }

  def instance[T: ctx.WeakTypeTag](
    buildMembers: List[MockableDefinition] => List[ValOrDefDef]
  ): ctx.Expr[T] = {
    import ctx.universe._

    val typeToMock = weakTypeOf[T]
    val anon = TypeName("$anon")

    val parent =
      typeToMock.members
        .collectFirst { case method: MethodSymbolApi if method.isPrimaryConstructor =>
          method.typeSignatureIn(typeToMock)
        }
        .map(con => con.paramLists.map(_.map(_.typeSignatureIn(con))))
        .getOrElse(Nil)
        .foldLeft[Tree](TypeTree(typeToMock)) { case (acc, paramTypes) =>
          Apply(acc, paramTypes.map(tpe => q"null.asInstanceOf[$tpe]"))
        }

    val constructor =
      DefDef(
        Modifiers(),
        TermName("<init>"),
        List(),
        List(List()),
        TypeTree(),
        Block(List(pendingSuperCall), q"()")
      )

    val methodsToMock = MockableDefinitions(typeToMock)

    def forwarderArgs(m: MockableDefinition): Tree = {
      val args = paramss(m.tpe).flatten.map(wrapByNameParam)
      if (newApi)
        tupledArgs(args)
      else args match {
        case Nil => q"None"
        case List(arg0) => q"scala.Tuple1($arg0)"
        case _ => tupledArgs(args)
      }
    }

    val forwarders = methodsToMock.map { m =>
      if (m.symbol.isVal) {
        ValDef(
          Modifiers(Flag.OVERRIDE),
          m.symbol.name,
          m.resTpe,
          q"null.asInstanceOf[${m.resTpe}]"
        )
      } else {
        // def <|name|>(p1: T1, p2: T2, ...): T = <|mockname|>(p1, p2, ...)
        DefDef(
          Modifiers(Flag.OVERRIDE),
          m.symbol.name,
          m.tpe.typeParams.map(internal.typeDef),
          paramss(m.tpe).map(_.map { p =>
            val needsExplicitDefault = p.asTerm.isParamWithDefault && isJsAny(typeToMock)
            val flags = Flag.PARAM |
              (if (p.isImplicit) Flag.IMPLICIT else NoFlags) |
              (if (needsExplicitDefault) Flag.DEFAULTPARAM else NoFlags)
            val defaultValue =
              if (needsExplicitDefault) q"null.asInstanceOf[${forwarderParamType(typeToMock, p.typeSignature)}]"
              else EmptyTree
            ValDef(
              Modifiers(flags),
              TermName(p.name.toString),
              forwarderParamType(typeToMock, p.typeSignature),
              defaultValue
            )
          }),
          m.resTpe,
          q"""
           ${m.name}
             .impl(${forwarderArgs(m)})
             .asInstanceOf[${m.resTpe}]
          """
        )
      }
    }



    /** final class $anon extends [[parent]] {
     *     def <init>() = super.<init>()
     *
     *     [[forwarders]]
     *     [[buildMembers(...)]]
     *  }
     *  (new $anon()).asInstanceOf[T]
     **/
    val result = Block(
      List(
        ClassDef(
          Modifiers(Flag.FINAL),
          anon,
          List(),
          Template(
            List(parent),
            noSelfType,
            constructor +: (buildMembers(methodsToMock) ++ forwarders)
          )
        )
      ),
      Apply(Select(New(Ident(anon)), termNames.CONSTRUCTOR), Nil)
    )

    //  if (typeToMock.typeSymbol.fullName == "???") {
    //    println("------------")
    //    println(showRaw(result))
    //    println("------------")
    //    println(show(result))
    //    println("------------")
    //  }

    ctx.Expr(q"$result.asInstanceOf[$typeToMock]")
  }

  def tupledArgs(args: List[ctx.universe.Tree]): Tree =
    args match {
      case Nil => Literal(Constant(()))
      case List(arg0) => arg0
      case List(arg0, arg1) => q"($arg0, $arg1)"
      case List(arg0, arg1, arg2) => q"($arg0, $arg1, $arg2)"
      case List(arg0, arg1, arg2, arg3) => q"($arg0, $arg1, $arg2, $arg3)"
      case List(arg0, arg1, arg2, arg3, arg4) => q"($arg0, $arg1, $arg2, $arg3, $arg4)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15, $arg16)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15, $arg16, $arg17)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15, $arg16, $arg17, $arg18)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15, $arg16, $arg17, $arg18, $arg19)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19, arg20) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15, $arg16, $arg17, $arg18, $arg19, $arg20)"
      case List(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15, arg16, arg17, arg18, arg19, arg20, arg21) => q"($arg0, $arg1, $arg2, $arg3, $arg4, $arg5, $arg6, $arg7, $arg8, $arg9, $arg10, $arg11, $arg12, $arg13, $arg14, $arg15, $arg16, $arg17, $arg18, $arg19, $arg20, $arg21)"
      case _ => ctx.abort(ctx.enclosingPosition, "ScalaMock: Can't handle methods with more than 22 parameters (yet)")
    }

  // Convert a methodType into its ultimate result type
  // For nullary and normal methods, this is just the result type
  // For curried methods, this is the final result type of the result type
  def finalResultType(methodType: Type): Type = methodType match {
    case NullaryMethodType(result) => result
    case MethodType(_, result) => finalResultType(result)
    case PolyType(_, result) => finalResultType(result)
    case _ => methodType
  }

  // Convert a methodType into a list of lists of params:
  // UnaryMethodType => Nil
  // Normal method => List(List(p1, p2, ...))
  // Curried method => List(List(p1, p2, ...), List(q1, q2, ...), ...)
  def paramss(methodType: Type): List[List[Symbol]] = methodType match {
    case MethodType(params, result) => params :: paramss(result)
    case PolyType(_, result) => paramss(result)
    case _ => Nil
  }

  def paramTypes(methodType: Type): List[Type] = paramss(methodType).flatten map { _.typeSignature }

  def isMemberOfObject(s: Symbol) = {
    val res = TypeTag.Object.tpe.member(s.name)
    res != NoSymbol && res.typeSignature == s.typeSignature
  }

  def reportError(message: String) = {
    // Report with both info and abort so that the user still sees something, even if this is within an
    // implicit conversion (see https://issues.scala-lang.org/browse/SI-5902)
    ctx.info(ctx.enclosingPosition, message, true)
    ctx.abort(ctx.enclosingPosition, message)
  }
}

