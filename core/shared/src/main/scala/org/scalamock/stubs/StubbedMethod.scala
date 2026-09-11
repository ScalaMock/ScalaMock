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

package org.scalamock.stubs

import java.util.concurrent.atomic.AtomicReference

/**
 * Representation of stubbed method with arguments.
 *
 * [[Stubs]] interface provides implicit conversions from selected method to StubbedMethod.
 * {{{
 *   trait Foo:
 *     def foo0: Int
 *     def foo(x: Int): Int
 *     def fooBar(bar: Boolean, baz: String): String
 *
 *   val foo = stub[Foo]
 * }}}
 *
 * Scala 2
 * {{{
 *   val foo0Stubbed: StubbedMethod[Unit, Int] = () => foo.foo0
 *   val fooStubbed: StubbedMethod[Int, Int] = foo.foo _
 *   val fooBarStubbed: StubbedMethod[(Boolean, String), String] = foo.fooBar _
 * }}}
 *
 * Scala 3
 * {{{
 *   val foo0Stubbed: StubbedMethod[Unit, Int] = () => foo.foo0
 *   val fooStubbed: StubbedMethod[Int, Int] = foo.foo
 *   val fooBarStubbed: StubbedMethod[(Boolean, String), String] = foo.fooBar
 * }}}
 * */
trait StubbedMethod[A, R] {
  /** Allows to set result for method with arguments.
   *
   *  Scala 3
   *  {{{
   *   foo.fooBar.returns:
   *     case (true, "bar") => "true"
   *     case _ => "false
   *  }}}
   *  Scala 2
   * {{{
   *   (foo.fooBar _).returns {
   *     case (true, "bar") => "true"
   *     case _ => "false"
   *   }
   * }}}
   *
   * */
  def returns(f: A => R): Unit

  /** Allows to set result for method with arguments.
   *
   *  Scala 3
   *  {{{
   *   foo.fooBar.returnsWith("true")
   *  }}}
   *  Scala 2
   * {{{
   *   (foo.fooBar _).returnsWith("true")
   * }}}
   *
   * */
  def returnsWith(value: => R): Unit

  /** Allows to set a result for the method with arguments using a partial function
   * in order to stub a certain set of argument values only and fail otherwise.
   *
   * Scala 3
   * {{{
   *   foo.fooBar.returnsWhen:
   *     case (true, "foo") => "true"
   *     case (_, "bar") => "false
   * }}}
   * Scala 2
   * {{{
   *   (foo.fooBar _).returnsWhen {
   *     case (true, "foo") => "true"
   *     case (_, "bar") => "false
   *   }
   * }}}
   * If the stubbed method is called with an argument for which the partial function is not defined,
   * then a `StubNotImplementedException` will be thrown:
   * {{{
   *   foo.fooBar((false, "bar")) // returns "false"
   *   foo.fooBar((false, "foo")) // throws StubNotImplementedException
   * }}}
   *
   * @param pf partial function from arguments of type `A` to result of type `R`
   */
  def returnsWhen(pf: PartialFunction[A, R]): Unit

  /** Allows to set result depending on call number starting from 1
   *
   * Scala 3
   * {{{
   *   foo.fooBar.returnsOnCall:
   *     case 1 | 2 => "true"
   *     case _ => "false
   *   }}}
   *  Scala 2
   * {{{
   *   (foo.fooBar _).returnsOnCall {
   *     case 1 | 2 => "true"
   *     case _ => "false
   *   }
   * }}}
   *
   * */
  def returnsOnCall(f: Int => R): Unit

  /** Allows to get number of times method was executed.
   *
   *  Scala 3
   *  {{{
   *   foo.foo.returns(x => 5)
   *   foo.foo(1)
   *
   *   foo.foo.times // 1
   *  }}}
   *  Scala 2
   * {{{
   *    (foo.foo _).returns(_ => 5)
   *    foo.foo(1)
   *
   *    (foo.foo _).times // 1
   * }}}
   * */
  def times: Int

  /** Allows to get number of times method was executed with specific arguments.
   *
   *  Scala 3
   *  {{{
   *   foo.foo.returns(_ => 5)
   *   foo.foo(1)
   *
   *   foo.foo.times(1) // 1
   *   foo.foo.times(100) // 0
   *  }}}
   *  Scala 2
   * {{{
   *    (foo.foo _).returns(_ => 5)
   *    foo.foo(1)
   *
   *   (foo.foo _).times(1) // 1
   *   (foo.foo _).times(100) // 0
   * }}}
   * */
  final def times(args: A): Int = calls.count(_ == args)

  /** Allows to get arguments with which method was executed.
   *  Returns multiple arguments as tuple.
   *  One list item per call.
   *
   *  Scala 3
   *  {{{
   *   foo.foo.returns(_ => 5)
   *   foo.foo(1)
   *   foo.foo(100)
   *
   *   foo.foo.calls // List(1, 100)
   *  }}}
   *  Scala 2
   * {{{
   *    (foo.foo _).returns(_ => 5)
   *    foo.foo(1)
   *    foo.foo(100)
   *
   *   (foo.foo _).calls // List(1, 100)
   * }}}
   * */
  def calls: List[A]

  /**
   * Returns true if this method was called before other method.
   *
   * Scala 3
   * {{{
   *   foo.foo.returns(_ => 5)
   *   foo.fooBar.returns(_ => "bar")
   *   foo.foo(1)
   *   foo.fooBar(true, "bar")
   *
   *   foo.foo.isBefore(foo.fooBar) // true
   *   }}}
   *  Scala 2
   *  {{{
   *    (foo.foo _).returns(_ => 5)
   *    (foo.fooBar _).returns(_ => "bar")
   *    foo.foo(1)
   *    foo.fooBar(true, "bar")
   *
   *   (foo.foo _).isBefore(foo.fooBar _) // true
   * }}}
   */
  def isBefore(other: StubbedMethod[_, _])(implicit callLog: CallLog): Boolean

  /** Returns true if this method was called after other method.
   *
   *  Scala 3
   *  {{{
   *   foo.foo.returns(_ => 5)
   *   foo.fooBar.returns(_ => "bar")
   *   foo.foo(1)
   *   foo.fooBar(true, "bar")
   *
   *   foo.foo.isAfter(foo.fooBar) // false
   * }}}
   *
   *  Scala 2
   *  {{{
   *    (foo.foo _).returns(_ => 5)
   *    (foo.fooBar _).returns(_ => "bar")
   *    foo.foo(1)
   *    foo.fooBar(true, "bar")
   *
   *   (foo.foo _).isAfter(foo.fooBar _) // false
   * }}}
   * */
  def isAfter(other: StubbedMethod[_, _])(implicit callLog: CallLog): Boolean

  /** Returns string representation of method.
   *  Representation currently depends on scala version.
   * */
  def asString: String
}

object StubbedMethod {
  class Internal[A, R](
    override val asString: String,
    callLog: Option[CallLog],
    io: Option[StubIO]
  ) extends StubbedMethod[A, R] {

    override def toString = asString

    private val callsRef: AtomicReference[List[A]] =
      new AtomicReference[List[A]](Nil)

    private val resultRef: AtomicReference[PartialFunction[A, R]] =
      new AtomicReference[PartialFunction[A, R]](PartialFunction.empty)

    def impl(args: A): R =
      io match {
        case None =>
          callLog.foreach(_.internal.write(asString))
          callsRef.updateAndGet(args :: _)
          resultRef.get()
            .applyOrElse(
              args,
              (_: A) => throw StubNotImplementedError(this, args))

        case Some(io) =>
          io.flatMap(
            io.succeed {
              callLog.foreach(_.internal.write(asString))
              callsRef.updateAndGet(args :: _)
            }
          ) { _ =>
            resultRef.get()
              .asInstanceOf[PartialFunction[A, io.F[Any, Any]]]
              .applyOrElse(
                args,
                (_: A) => io.die(StubNotImplementedError(this, args)))
          }.asInstanceOf[R]
      }

    def clear(): Unit = {
      callsRef.set(Nil)
      resultRef.set(PartialFunction.empty)
    }

    override def returns(f: A => R): Unit =
      resultRef.set { case a => f(a) }

    override def returnsWith(value: => R): Unit =
      resultRef.set { case _ => value }

    override def returnsWhen(pf: PartialFunction[A, R]): Unit =
      resultRef.set(pf)

    override def returnsOnCall(f: Int => R): Unit =
      resultRef.set { case _ => f(callsRef.get().length) }

    override def times: Int =
      callsRef.get().length

    override def calls: List[A] =
      callsRef.get().reverse

    override def isBefore(other: StubbedMethod[_, _])(implicit callLog: CallLog): Boolean = {
      val actual = callLog.internal.calledMethods
      actual.indexOf(other.asString, actual.indexOf(asString)) != -1
    }

    override def isAfter(other: StubbedMethod[_, _])(implicit callLog: CallLog): Boolean = {
      val actual = callLog.internal.calledMethods
      actual.indexOf(asString, actual.indexOf(other.asString)) != -1
    }
  }
}
