package org.scalamock.stubs

import org.scalamock.stubs.StubbedKyoMethod

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.util.NotGiven

import kyo.*

trait KyoStubs extends StubsBase {
  private[scalamock] class KyoStubIO extends StubIO {
    type F[+A, +B] = IO[A, B]

    def die(ex: Throwable): F[Nothing, Nothing] = Kyo.die(ex)
    def succeed[T](t: => T): F[Nothing, T] = Kyo.succeed(t)
    def flatMap[E, EE >: E, T, T2](fa: IO[E, T])(f: T => IO[EE, T2]) = fa.flatMap(f)
  }
  final given KyoStubIO = KyoStubIO()

  implicit inline def stubbed[E, A](inline f: => IO[E, A])(using A =:= A, E =:= E): StubbedKyoMethod[Unit, IO[E, A]] =
    StubbedKyoMethod[Unit, IO[E, A]](stubbed00Impl[IO[E, A]](f))

  implicit inline def stubbed[R](inline f: => Future[R]): StubbedKyoMethod[Unit, Future[R]] =
    StubbedKyoMethod[Unit, Future[R]](stubbed00Impl[Future[R]](f))

  implicit inline def stubbed[T1, R](inline f: () => R)(using R =:= R): StubbedKyoMethod[Unit, R] =
    StubbedKyoMethod[Unit, R](stubbed0Impl[R](f))

  implicit inline def stubbed[T1, R](inline f: T1 => R)(using (T1, R) =:= (T1, R)): StubbedKyoMethod[T1, R] =
    StubbedKyoMethod[T1, R](stubbed1Impl[T1, R](f))

  implicit inline def stubbed[T1, T2, R](inline f: (T1, T2) => R)(using (T1, T2, R) =:= (T1, T2, R)): StubbedKyoMethod[(T1, T2), R] =
    StubbedKyoMethod[(T1, T2), R](stubbed2Impl[T1, T2, R](f))

  implicit inline def stubbed[T1, T2, T3, R](inline f: (T1, T2, T3) => R)(using (T1, T2, T3, R) =:= (T1, T2, T3, R)): StubbedKyoMethod[(T1, T2, T3), R] =
    StubbedKyoMethod(stubbed3Impl[T1, T2, T3, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, R](inline f: (T1, T2, T3, T4) => R)(using (T1, T2, T3, T4, R) =:= (T1, T2, T3, T4, R)): StubbedKyoMethod[(T1, T2, T3, T4), R] =
    StubbedKyoMethod(stubbed4Impl[T1, T2, T3, T4, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, R](inline f: (T1, T2, T3, T4, T5) => R)(using (T1, T2, T3, T4, T5, R) =:= (T1, T2, T3, T4, T5, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5), R] =
    StubbedKyoMethod(stubbed5Impl[T1, T2, T3, T4, T5, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, R](inline f: (T1, T2, T3, T4, T5, T6) => R)(using (T1, T2, T3, T4, T5, T6, R) =:= (T1, T2, T3, T4, T5, T6, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6), R] =
    StubbedKyoMethod(stubbed6Impl[T1, T2, T3, T4, T5, T6, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, R](inline f: (T1, T2, T3, T4, T5, T6, T7) => R)(using (T1, T2, T3, T4, T5, T6, T7, R) =:= (T1, T2, T3, T4, T5, T6, T7, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7), R] =
    StubbedKyoMethod(stubbed7Impl[T1, T2, T3, T4, T5, T6, T7, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8), R] =
    StubbedKyoMethod(stubbed8Impl[T1, T2, T3, T4, T5, T6, T7, T8, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9), R] =
    StubbedKyoMethod(stubbed9Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10), R] =
    StubbedKyoMethod(stubbed10Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11), R] =
    StubbedKyoMethod(stubbed11Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12), R] =
    StubbedKyoMethod(stubbed12Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13), R] =
    StubbedKyoMethod(stubbed13Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14), R] =
    StubbedKyoMethod(stubbed14Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15), R] =
    StubbedKyoMethod(stubbed15Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16), R] =
    StubbedKyoMethod(stubbed16Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17), R] =
    StubbedKyoMethod(stubbed17Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18), R] =
    StubbedKyoMethod(stubbed18Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19), R] =
    StubbedKyoMethod(stubbed19Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20), R] =
    StubbedKyoMethod(stubbed20Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21), R] =
    StubbedKyoMethod(stubbed21Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, R](f))

  implicit inline def stubbed[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](inline f: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22) => R)(using (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R) =:= (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R)): StubbedKyoMethod[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22), R] =
    StubbedKyoMethod(stubbed22Impl[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, R](f))

}
