package org.scalamock.clazz

import org.scalamock.stubs.Stub
import org.scalamock.util.Defaultable

import scala.annotation.tailrec
import scala.quoted.{Expr, Quotes, Type}

private[scalamock]
class MakerUtils(using override val quotes: Quotes) extends Utils:
  import quotes.reflect.*

  /**
   *  To create mock object we should extend out T.
   *  This method builds constructor with applied args
   *  and returns correct parents of a trait and a class.
   *  
   *  Note that if T is a trait, we also should extend Object.
   */
  def parentsOf[T: Type]: List[TypeTree | Term] =
    val tpe = TypeRepr.of[T]
    val isTrait = tpe.dealias.typeSymbol.flags.is(Flags.Trait)
    val isJavaClass = tpe.classSymbol.exists(sym => sym.flags.is(Flags.JavaDefined) && !sym.flags.is(Flags.Trait) && !sym.flags.is(Flags.Abstract))

    if (isJavaClass)
      report.errorAndAbort("Can't mock a java class due to https://github.com/lampepfl/dotty/issues/18694. Extend it manually with scala and then mock")

    def asParent(tree: TypeTree): TypeTree | Term =
      val constructorTypes = tree.tpe.typeSymbol.primaryConstructor
        .paramSymss.flatten.filter(_.isType).map(_.name)

      val constructorFields = tree.tpe.dealias.typeSymbol.primaryConstructor
        .paramSymss.filterNot(_.exists(_.isType))

      if tree.tpe.typeArgs.length < constructorTypes.length then
        report.errorAndAbort("Not all types are applied")

      if constructorFields.forall(_.isEmpty) then
        tree
      else
        val con = Select(
          New(TypeIdent(tree.tpe.typeSymbol)),
          tree.tpe.typeSymbol.primaryConstructor
        ).appliedToTypes(tree.tpe.typeArgs)

        val typeNames = {
          @tailrec
          def collectTypes(tpe: TypeRepr, acc: Map[String, TypeRepr]): Map[String, TypeRepr] =
            tpe match
              case MethodType(names, types, res) => collectTypes(res, acc ++ names.zip(types))
              case tpe => acc

          collectTypes(con.tpe.widenTermRefByName, Map.empty)
            .view.mapValues {
              case n: ByNameType => n.widenByName
              case n => n
            }
            .toMap
        }

        con.appliedToArgss(
          constructorFields
            .map(_.map { sym => typeNames(sym.name).asType })
            .map(_.map { case '[t] => '{ ${ Expr.summon[Defaultable[t]].get }.default }.asTerm })
        )
    end asParent

    if isTrait then
      List(TypeTree.of[Object], asParent(TypeTree.of[T]), TypeTree.of[scala.reflect.Selectable])
    else
      List(asParent(TypeTree.of[T]), TypeTree.of[scala.reflect.Selectable])
  end parentsOf

  /**
   *  Builds `class $anonName extends ...T { ... }` and instantiates it.
   *  This is the Scala 3 equivalent of the shared `Utils.instance` helper used by Scala 2 macros
   *  to reuse code between classic mocks and stubs: it takes care of extending T (via [[parentsOf]])
   *  and wiring up the anonymous class/instantiation boilerplate, while the caller only supplies
   *  the members that differ between mocks and stubs.
   *
   * @param anonName    name of the generated anonymous class
   * @param extraDecls  extra symbols to declare on the class, not tied to any particular mocked method
   *                    (e.g. a field holding the mock's default name, or stub bookkeeping members)
   * @param extraBody   definitions matching `extraDecls`
   * @param methodDecls symbols to declare for a single mocked method (e.g. a backing field plus its override)
   * @param methodBody  definitions matching `methodDecls` for a single mocked method
   * @return an instance of the anonymous class, typed as `T & scala.reflect.Selectable`
   */
  def buildInstance[T: Type](
    anonName: String,
    extraDecls: (Symbol, List[MockableDefinition]) => List[Symbol],
    extraBody: (Symbol, List[MockableDefinition]) => List[Statement],
    methodDecls: (Symbol, MockableDefinition) => List[Symbol],
    methodBody: (Symbol, MockableDefinition) => List[Statement]
  ): Term =
    val tpe = TypeRepr.of[T]
    val parents = parentsOf[T]
    val methods = MockableDefinitions(tpe)

    val classSymbol: Symbol = Symbol.newClass(
      owner = Symbol.spliceOwner,
      name = anonName,
      parents = parents.map {
        case term: Term => term.tpe
        case tree: TypeTree => tree.tpe
      },
      decls = classSymbol => extraDecls(classSymbol, methods) ::: methods.flatMap(methodDecls(classSymbol, _)),
      selfType = None
    )

    val classDef = ClassDef(
      cls = classSymbol,
      parents = parents,
      body = extraBody(classSymbol, methods) ::: methods.flatMap(methodBody(classSymbol, _))
    )

    Block(
      List(classDef),
      Typed(
        Apply(Select(New(TypeIdent(classSymbol)), classSymbol.primaryConstructor), Nil),
        TypeTree.of[T & scala.reflect.Selectable]
      )
    )
  end buildInstance

  /**
   *  Loops through inlined method call to find mocked object and called method name.
   *  Searches method info (MockableDefinition) through all relevant methods collected from TypeRepr.
   * 
   * @param term initial inlined method call, converted to function via ETA expansion
   * @param argTypes provided function argument types without res type
   * @param appliedTypes collected applied types, when method is generic
   * @return StubWithMethod
   */
  def searchTermWithMethod(term: Term, argTypes: List[TypeRepr], appliedTypes: List[TypeTree] = Nil): StubWithMethod =
    term match
      case Select(apply @ Apply(term, _), "apply") if apply.tpe.isContextFunctionType =>
        searchTermWithMethod(term, argTypes, appliedTypes)

      case Select(mock, methodName) =>
        val tpe =
          mock.tpe.widenTermRefByName match
            case AppliedType(tycon, List(typ)) if newApi && tycon =:= TypeRepr.of[Stub] =>
              typ
            case tpe if newApi =>
              report.errorAndAbort(s"This syntax is only available on Stub[T]")
            case other =>
              other

        val method = MockableDefinitions.find(tpe, methodName, argTypes, appliedTypes.map(_.tpe))
        StubWithMethod(mock, method)

      case Block(_, Inlined(_, _, Inlined(_, _, term: Select))) =>
        searchTermWithMethod(term, argTypes)

      case Inlined(_, _, term) =>
        searchTermWithMethod(term, argTypes)

      case Block(stats@List(_: ValDef), term) => // var m = mock[T]
        val StubWithMethod(underlying, method) = searchTermWithMethod(term, argTypes)
        StubWithMethod(Block(stats, underlying), method)

      case Block(List(DefDef(_, _, _, Some(term))), _) =>
        searchTermWithMethod(term, argTypes)

      case Typed(term, teps) =>
        searchTermWithMethod(term, argTypes)

      case Lambda(_, term) =>
        searchTermWithMethod(term, argTypes)

      case Apply(term, _) =>
        searchTermWithMethod(term, argTypes)

      case TypeApply(term, types) =>
        searchTermWithMethod(term, argTypes, types)

      case Ident(fun) if !newApi =>
        report.errorAndAbort(
          s"please declare '$fun' as MockFunctionX or StubFunctionX (e.g val $fun: MockFunction1[X, R] = ... if it has 1 parameter)"
        )
      case _ =>
        report.errorAndAbort(
          s"""
              This syntax is only available on Stub[T].
              Current structure is not recognised: ${term.show}
              If you think this is a bug - please open a ticket at https://github.com/ScalaMock/ScalaMock/issues"
          """
        )
  end searchTermWithMethod
