package slick.dbio

import cats.*
import cats.syntax.all.*

import scala.util.*

trait DBIOInstances3 {
  implicit val dbioInstance3: MonadError[[R] =>> SlickAction[NoStream, Effect, R], Throwable] =
    new MonadError[[R] =>> SlickAction[NoStream, Effect, R], Throwable] {
      override def pure[A](x: A): SlickAction[NoStream, Effect, A] = DBIO.successful(x)

      override def flatMap[A, B](fa: SlickAction[NoStream, Effect, A])(f: (A) => SlickAction[NoStream, Effect, B]): SlickAction[ NoStream, Effect, B] = fa.flatMap(f)

      /**
       * While this is roughly the same implementation as in `FutureInstances`,
       * I'm not entirely sure this is indeed stack safe. It certainly looks
       * like it should be.
       */
      override def tailRecM[A, B](a: A)(f: A => SlickAction[NoStream, Effect, Either[A, B]]): SlickAction[NoStream, Effect, B] =
        f(a).flatMap {
          case Left(a1) => tailRecM(a1)(f)
          case Right(b) => DBIO.successful(b)
        }

      override def handleError[A](fea: SlickAction[NoStream, Effect, A])(f: (Throwable) => A): SlickAction[NoStream, Effect, A] =
        fea.asTry.map {
          case Success(a) => a
          case Failure(t) => f(t)
        }

      override def raiseError[A](e: Throwable): SlickAction[NoStream, Effect, A] = DBIO.failed(e)

      override def map[A, B](fa: SlickAction[NoStream, Effect, A])(f: A => B): SlickAction[NoStream, Effect, B] = fa.map(f)

      override def handleErrorWith[A](fa: SlickAction[NoStream, Effect, A])(f: (Throwable) => SlickAction[NoStream, Effect, A]): SlickAction[NoStream, Effect, A] =
        fa.asTry.flatMap {
          case Success(a) => DBIO.successful(a)
          case Failure(t) => f(t)
        }
    }

  implicit def dbioGroup3[A: Group]: Group[SlickAction[NoStream, Effect, A]] =
    new DBIOGroup[A]

  implicit def dbioMonoid3[A: Monoid]: Monoid[SlickAction[NoStream, Effect, A]] =
    new DBIOMonoid[A]

  implicit def dbioSemigroup3[A: Semigroup]: Semigroup[SlickAction[NoStream, Effect, A]] =
    new DBIOSemigroup[A]

  private class DBIOSemigroup[A: Semigroup] extends Semigroup[SlickAction[NoStream, Effect, A]] {
    override def combine(fx: SlickAction[NoStream, Effect, A], fy: SlickAction[NoStream, Effect, A]): SlickAction[NoStream, Effect, A] =
      (fx zip fy).map { case (x, y) => x |+| y }
  }

  private class DBIOMonoid[A](implicit A: Monoid[A]) extends DBIOSemigroup[A] with Monoid[SlickAction[NoStream, Effect, A]] {
    def empty: SlickAction[NoStream, Effect, A] = DBIO.successful(A.empty)
  }

  private class DBIOGroup[A](implicit A: Group[A]) extends DBIOMonoid[A] with Group[SlickAction[NoStream, Effect, A]] {
    def inverse(fx: SlickAction[NoStream, Effect, A]): SlickAction[NoStream, Effect, A] = fx.map(_.inverse())

    override def remove(fx: SlickAction[NoStream, Effect, A], fy: SlickAction[NoStream, Effect, A]): SlickAction[NoStream, Effect, A] =
      (fx zip fy).map { case (x, y) => x |-| y }
  }
}
