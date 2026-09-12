package com.paulbutcher.test

import org.scalamock.matchers.Matchers
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec

class PathDependentParamSpec extends AnyFunSpec with Matchers with MockFactory {

  trait Command {
    type Answer
    type AnswerConstructor[A]
  }

  case class IntCommand() extends Command {
    override type Answer = Int
    override type AnswerConstructor[A] = Option[A]
  }

  val cmd = IntCommand()

  trait PathDependent {

    def call0[T <: Command](cmd: T): cmd.Answer

    def call1[T <: Command](x: Int)(cmd: T): cmd.Answer

    def call2[T <: Command](y: String)(cmd: T)(x: Int): cmd.Answer

    def call3[T <: Command](cmd: T)(y: String)(x: Int): cmd.Answer

    def call4[T <: Command](cmd: T): Option[cmd.Answer]

    def call5[T <: Command](cmd: T)(x: cmd.Answer): Unit

    def call6[T <: Command](cmd: T): cmd.AnswerConstructor[Int]

    def call7[T <: Command](cmd: T)(x: cmd.AnswerConstructor[String])(y: cmd.Answer): Unit
  }


  it("path dependent in return type") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call0[IntCommand] _).expects(cmd).returns(5)

    assert(pathDependent.call0(cmd) == 5)
  }

  it("path dependent in return type and parameter in last parameter list") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call1(_: Int)(_: IntCommand)).expects(5, cmd).returns(5)

    assert(pathDependent.call1(5)(cmd) == 5)
  }

  it("path dependent in return type and parameter in middle parameter list ") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call2(_: String)(_: IntCommand)(_: Int)).expects("5", cmd, 5).returns(5)

    assert(pathDependent.call2("5")(cmd)(5) == 5)
  }

  it("path dependent in return type and parameter in first parameter list ") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call3(_: IntCommand)(_: String)(_: Int)).expects(cmd, "5", 5).returns(5)

    assert(pathDependent.call3(cmd)("5")(5) == 5)
  }

  it("path dependent in tycon return type") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call4[IntCommand] _).expects(cmd).returns(Some(5))

    assert(pathDependent.call4(cmd) == Some(5))
  }

  it("path dependent in parameter list") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call5(_: IntCommand)(_: Int)).expects(cmd, 5).returns(())

    assert(pathDependent.call5(cmd)(5) == ())
  }

  it("path dependent tycon in return type") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call6[IntCommand] _).expects(cmd).returns(Some(5))

    assert(pathDependent.call6(cmd) == Some(5))
  }

  it("path dependent tycon in parameter list") {
    val pathDependent = mock[PathDependent]

    (pathDependent.call7[IntCommand](_: IntCommand)(_: Option[String])(_: Int))
      .expects(cmd, Some("5"), 6)
      .returns(())

    assert(pathDependent.call7(cmd)(Some("5"))(6) == ())
  }

}
