package org.scalamock.stubs

import zio._

/**
 * Representation of stubbed method.
 *
 * [[ZIOStubs]] interface provides implicit conversion from selected method to StubbedMethodZIO.
 * {{{
 *   trait Foo:
 *     def foo0: UIO[Int]
 *     def foo(x: Int): UIO[String]
 *     def bar(x: Int, y: String): IO[String, Int]
 *
 *   val foo = stub[Foo]
 * }}}
 *
 * Scala 3
 * {{{
 *   val foo0Stubbed: StubbedMethod[Unit, UIO[Int]] = foo.foo0
 *   val fooStubbed: StubbedMethod[Int, UIO[String]] = foo.foo
 *   val barStubbed: StubbedMethod[(Int, String), IO[String, Int]] = foo.bar
 * }}}
 *
 * Scala 2
 * {{{
 *   val foo0Stubbed: StubbedMethod[Unit, UIO[Int]] = foo.foo0 _
 *   val fooStubbed: StubbedMethod[Int, UIO[String]] = foo.foo _
 *   val barStubbed: StubbedMethod[(Int, String), IO[String, Int]] = foo.bar _
 * }}}
 * */
class StubbedZIOMethod[A, R](delegate: StubbedMethod[A, R]) extends StubbedMethod[A, R] {

  /** Allows to set result for method with arguments. Returns ZIO
   * 
   *  Scala 3
   *  {{{
   *   for
   *     _ <- foo.bar.returnsZIO((x, y) => ZIO.succeed(1))
   *   yield ()
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsZIO((x, y) => ZIO.succeed(1))
   *   } yield ()
   *  }}}
   * */
  def returnsZIO(f: A => R): UIO[Unit] = ZIO.succeed(returns(f))

  /** Allows to set result for method with arguments. Returns ZIO
   *
   * Scala 3
   * {{{
   *   for
   *     _ <- foo.bar.returnsZIOWith(ZIO.succeed(1))
   *   yield ()
   *   }}}
   *
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsZIOWith(ZIO.succeed(1))
   *   } yield ()
   * }}}
   * */
  def returnsZIOWith(value: => R): UIO[Unit] = returnsZIO(_ => value)

  /** Allows to set success for method with arguments. Returns ZIO
   *
   *  Scala 3
   *  {{{
   *   for
   *     _ <- foo.bar.succeedsWith(1)
   *   yield ()
   *  }}}
   *
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).succeedsWith(1)
   *   } yield ()
   *  }}}
   * */
  def succeedsWith[RR](result: RR)(implicit ev: IO[Nothing, RR] <:< R): UIO[Unit] =
    returnsZIO(_ => ev(ZIO.succeed(result)))


  /** Allows set fail result for method with arguments. Returns ZIO
   *
   *  Scala 3
   *  {{{
   *   for
   *     _ <- foo.bar.failsWith("foo")
   *   yield ()
   *  }}}
   *
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).failsWith("foo")
   *   } yield ()
   *  }}}
   * */
  def failsWith[RR](result: RR)(implicit ev: IO[RR, Nothing] <:< R): UIO[Unit] =
    returnsZIO(_ => ev(ZIO.fail(result)))

  /** Allows set die result for method with arguments. Returns ZIO
   *
   *  Scala 3
   *  {{{
   *   for
   *     _ <- foo.bar.diesWith(new Exception("foo"))
   *   yield ()
   *  }}}
   *
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).diesWith(new Exception("foo"))
   *   } yield ()
   *  }}}
   * */
  def diesWith(ex: => Throwable)(implicit ev: UIO[Nothing] <:< R): UIO[Unit] =
    returnsZIO(_ => ev(ZIO.die(ex)))

  /** Allows to get arguments with which method was executed. Returns ZIO
   * 
   *  Returns multiple arguments as tuple. One list item per call.
   *
   *  Scala 3
   *  {{{
   *   for {
   *     _ <- foo.bar.returnsZIO(_ => ZIO.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *     calls <- foo.bar.callsZIO
   *   } yield calls == List((1, "foo"), (2, "bar")) // true
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsZIO(_ => ZIO.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *     calls <- (foo.bar _).callsZIO
   *   } yield calls == List((1, "foo"), (2, "bar")) // true
   *  }}}
   * */
  def callsZIO: UIO[List[A]] = ZIO.succeed(calls)

  /** Allows to get number of times method was executed. Returns ZIO
   *
   *  Scala 3
   * {{{
   *    for
   *      _ <- foo.bar.returnsZIO(_ => ZIO.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- foo.bar.timesZIO
   *    yield barTimes == 11 // true
   * }}}
   *  Scala 2
   *  {{{
   *    for {
   *      _ <- (foo.bar _).returnsZIO(_ => ZIO.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- (foo.bar _).timesZIO
   *    } yield barTimes == 11 // true
   * }}}
   * */
  def timesZIO: UIO[Int] = ZIO.succeed(times)

  /** Allows to get number of times method was executed with specific arguments. Returns ZIO
   *
   *  Scala 3
   *  {{{
   *    for
   *      _ <- foo.bar.returnsZIO(_ => ZIO.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- foo.bar.timesZIO((1, "foo"))
   *    yield barTimes == 11 // true
   * }}}
   * 
   *  Scala 2
   *  {{{
   *    for {
   *      _ <- (foo.bar _).returnsZIO(_ => ZIO.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- (foo.bar _).timesZIO((1, "foo"))
   *    } yield barTimes == 11 // true
   * }}}
   * */
  def timesZIO(args: A): UIO[Int] = ZIO.succeed(times(args))

  /** Allows to set result for method with arguments.
   *
   *  Scala 3
   *  {{{
   *   foo.bar.returns((x, y) => ZIO.succeed(1))
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   (foo.bar _).returns((x, y) => ZIO.succeed(1))
   *  }}}
   * */
  def returns(f: A => R): Unit = delegate.returns(f)

  /** Allows to set result for method without arguments.
   *
   * Scala 3
   * {{{
   *   foo.bar.returnsWith(ZIO.succeed(1))
   * }}}
   * Scala 2
   * {{{
   *   (foo.bar _).returnsWith(ZIO.succeed(1))
   * }}}
   * */
  def returnsWith(value: => R) = delegate.returnsWith(value)

  def returnsWhen(pf: PartialFunction[A, R]): Unit = delegate.returnsWhen(pf)

  /** Allows to set result depending on call number starting from 1
   *
   * Scala 3
   * {{{
   *   foo.bar.returnsOnCall:
   *     case 1 | 2 => ZIO.succeed(1)
   *     case _ => ZIO.succeed(0)
   *  }}}* Scala 2
   * {{{
   *   (foo.bar _).returnsOnCall {
   *     case 1 | 2 => ZIO.succeed(1)
   *     case _ => ZIO.succeed(0)
   *   }
   * }}}
   *
   * */
  def returnsOnCall(f: Int => R): Unit = delegate.returnsOnCall(f)

  /** Allows to set result depending on call number starting from 1. Returns ZIO
   *
   * Scala 3
   * {{{
   *   for
   *     _ <- foo.bar.returnsZIOOnCall:
   *       case 1 => ZIO.succeed(0)
   *       case _ => ZIO.succeed(1)
   *   yield ()
   *    }}}
   * Scala 2
   *
   * {{{
   *   for {
   *     _ <- foo.bar.returnsZIOOnCall {
   *       case 1 => ZIO.succeed(0)
   *       case _ => ZIO.succeed(1)
   *     }
   *   } yield ()
   * }}}
   *
   * */
  def returnsZIOOnCall(f: Int => R): UIO[Unit] = ZIO.succeed(delegate.returnsOnCall(f))

  /** Allows to get number of times method was executed.
   *
   * {{{
   *    for {
   *      _ <- foo.fooIO.returnsZIO(ZIO.succeed(1))
   *      _ <- foo.fooIO.repeatN(10)
   *    } yield foo.fooIO.times == 11 // true
   * }}}
   * */
  def times: Int = delegate.times

  /** Allows to get arguments with which method was executed.
   *  Returns multiple arguments as tuple.
   *  One list item per call.
   *
   *  Scala 3
   *  {{{
   *   for {
   *     _ <- foo.bar.returnsZIO(_ => ZIO.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *   } yield foo.bar.calls == List((1, "foo"), (2, "bar")) // true
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsZIO(_ => ZIO.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *   } yield (foo.bar _).calls == List((1, "foo"), (2, "bar")) // true
   *   }}}
   * */
  def calls: List[A] = delegate.calls

  /**
   * Returns true if this method was called before other method.
   */
  def isBefore(other: StubbedMethod[_, _])(implicit callLog: CallLog): Boolean =
    delegate.isBefore(other)

  /**
   * Returns true if this method was called after other method.
   */
  def isAfter(other: StubbedMethod[_, _])(implicit callLog: CallLog): Boolean =
    delegate.isAfter(other)

  /**
   *  Returns string representation of method.
   *  Representation currently depends on scala version.
   * */
  def asString: String = delegate.asString

  override def toString: String = asString
}
