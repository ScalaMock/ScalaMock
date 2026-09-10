package org.scalamock.stubs

import kyo._

/**
 * Representation of stubbed method.
 *
 * [[KyoStubs]] interface provides implicit conversion from selected method to StubbedMethodKyo.
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
class StubbedKyoMethod[A, R](delegate: StubbedMethod[A, R]) extends StubbedMethod[A, R] {

  /** Allows to set result for method with arguments. Returns Kyo
   * 
   *  Scala 3
   *  {{{
   *   for
   *     _ <- foo.bar.returnsKyo((x, y) => Kyo.succeed(1))
   *   yield ()
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsKyo((x, y) => Kyo.succeed(1))
   *   } yield ()
   *  }}}
   * */
  def returnsKyo(f: A => R): UIO[Unit] = Kyo.succeed(returns(f))

  /** Allows to set result for method with arguments. Returns Kyo
   *
   * Scala 3
   * {{{
   *   for
   *     _ <- foo.bar.returnsKyoWith(Kyo.succeed(1))
   *   yield ()
   *   }}}
   *
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsKyoWith(Kyo.succeed(1))
   *   } yield ()
   * }}}
   * */
  def returnsKyoWith(value: => R): UIO[Unit] = returnsKyo(_ => value)

  /** Allows to set success for method with arguments. Returns Kyo
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
    returnsKyo(_ => ev(Kyo.succeed(result)))


  /** Allows set fail result for method with arguments. Returns Kyo
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
    returnsKyo(_ => ev(Kyo.fail(result)))

  /** Allows set die result for method with arguments. Returns Kyo
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
    returnsKyo(_ => ev(Kyo.die(ex)))

  /** Allows to get arguments with which method was executed. Returns Kyo
   * 
   *  Returns multiple arguments as tuple. One list item per call.
   *
   *  Scala 3
   *  {{{
   *   for {
   *     _ <- foo.bar.returnsKyo(_ => Kyo.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *     calls <- foo.bar.callsKyo
   *   } yield calls == List((1, "foo"), (2, "bar")) // true
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsKyo(_ => Kyo.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *     calls <- (foo.bar _).callsKyo
   *   } yield calls == List((1, "foo"), (2, "bar")) // true
   *  }}}
   * */
  def callsKyo: UIO[List[A]] = Kyo.succeed(calls)

  /** Allows to get number of times method was executed. Returns Kyo
   *
   *  Scala 3
   * {{{
   *    for
   *      _ <- foo.bar.returnsKyo(_ => Kyo.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- foo.bar.timesKyo
   *    yield barTimes == 11 // true
   * }}}
   *  Scala 2
   *  {{{
   *    for {
   *      _ <- (foo.bar _).returnsKyo(_ => Kyo.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- (foo.bar _).timesKyo
   *    } yield barTimes == 11 // true
   * }}}
   * */
  def timesKyo: UIO[Int] = Kyo.succeed(times)

  /** Allows to get number of times method was executed with specific arguments. Returns Kyo
   *
   *  Scala 3
   *  {{{
   *    for
   *      _ <- foo.bar.returnsKyo(_ => Kyo.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- foo.bar.timesKyo((1, "foo"))
   *    yield barTimes == 11 // true
   * }}}
   * 
   *  Scala 2
   *  {{{
   *    for {
   *      _ <- (foo.bar _).returnsKyo(_ => Kyo.succeed(1))
   *      _ <- foo.bar(1, "foo").repeatN(10)
   *      barTimes <- (foo.bar _).timesKyo((1, "foo"))
   *    } yield barTimes == 11 // true
   * }}}
   * */
  def timesKyo(args: A): UIO[Int] = Kyo.succeed(times(args))

  /** Allows to set result for method with arguments.
   *
   *  Scala 3
   *  {{{
   *   foo.bar.returns((x, y) => Kyo.succeed(1))
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   (foo.bar _).returns((x, y) => Kyo.succeed(1))
   *  }}}
   * */
  def returns(f: A => R): Unit = delegate.returns(f)

  /** Allows to set result for method without arguments.
   *
   * Scala 3
   * {{{
   *   foo.bar.returnsWith(Kyo.succeed(1))
   * }}}
   * Scala 2
   * {{{
   *   (foo.bar _).returnsWith(Kyo.succeed(1))
   * }}}
   * */
  def returnsWith(value: => R) = delegate.returnsWith(value)

  /** Allows to set result depending on call number starting from 1
   *
   * Scala 3
   * {{{
   *   foo.bar.returnsOnCall:
   *     case 1 | 2 => Kyo.succeed(1)
   *     case _ => Kyo.succeed(0)
   *  }}}* Scala 2
   * {{{
   *   (foo.bar _).returnsOnCall {
   *     case 1 | 2 => Kyo.succeed(1)
   *     case _ => Kyo.succeed(0)
   *   }
   * }}}
   *
   * */
  def returnsOnCall(f: Int => R): Unit = delegate.returnsOnCall(f)

  /** Allows to set result depending on call number starting from 1. Returns Kyo
   *
   * Scala 3
   * {{{
   *   for
   *     _ <- foo.bar.returnsKyoOnCall:
   *       case 1 => Kyo.succeed(0)
   *       case _ => Kyo.succeed(1)
   *   yield ()  
   *    }}}
   * Scala 2
   *
   * {{{
   *   for {
   *     _ <- foo.bar.returnsKyoOnCall {
   *       case 1 => Kyo.succeed(0)
   *       case _ => Kyo.succeed(1)
   *     }
   *   } yield () 
   * }}}
   *
   * */
  def returnsKyoOnCall(f: Int => R): UIO[Unit] = Kyo.succeed(delegate.returnsOnCall(f))

  /** Allows to get number of times method was executed.
   *
   * {{{
   *    for {
   *      _ <- foo.fooIO.returnsKyo(Kyo.succeed(1))
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
   *     _ <- foo.bar.returnsKyo(_ => Kyo.succeed(5))
   *     _ <- foo.bar(1, "foo")
   *     _ <- foo.bar(2, "bar")
   *   } yield foo.bar.calls == List((1, "foo"), (2, "bar")) // true
   *  }}}
   *  
   *  Scala 2
   *  {{{
   *   for {
   *     _ <- (foo.bar _).returnsKyo(_ => Kyo.succeed(5))
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
