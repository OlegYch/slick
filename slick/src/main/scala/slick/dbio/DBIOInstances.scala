package slick.dbio

import cats._
import cats.syntax.all._
import scala.util._

trait DBIOInstances {
  implicit val dbioInstance: MonadError[DBIO, Throwable] =
    new MonadError[DBIO, Throwable] {
      override def pure[A](x: A): DBIO[A] = DBIO.successful(x)

      override def flatMap[A, B](fa: DBIO[A])(f: (A) => DBIO[B]): DBIO[B] = fa.flatMap(f)

      /**
       * While this is roughly the same implementation as in `FutureInstances`,
       * I'm not entirely sure this is indeed stack safe. It certainly looks
       * like it should be.
       */
      override def tailRecM[A, B](a: A)(f: A => DBIO[Either[A, B]]): DBIO[B] =
        f(a).flatMap {
          case Left(a1) => tailRecM(a1)(f)
          case Right(b) => DBIO.successful(b)
        }

      override def handleError[A](fea: DBIO[A])(f: (Throwable) => A): DBIO[A] =
        fea.asTry.map {
          case Success(a) => a
          case Failure(t) => f(t)
        }

      override def raiseError[A](e: Throwable): DBIO[A] = DBIO.failed(e)

      override def map[A, B](fa: DBIO[A])(f: A => B): DBIO[B] = fa.map(f)

      override def handleErrorWith[A](fa: DBIO[A])(f: (Throwable) => DBIO[A]): DBIO[A] =
        fa.asTry.flatMap {
          case Success(a) => DBIO.successful(a)
          case Failure(t) => f(t)
        }
    }

  implicit def dbioGroup[A: Group]: Group[DBIO[A]] =
    new DBIOGroup[A]

  implicit def dbioMonoid[A: Monoid]: Monoid[DBIO[A]] =
    new DBIOMonoid[A]

  implicit def dbioSemigroup[A: Semigroup]: Semigroup[DBIO[A]] =
    new DBIOSemigroup[A]

  private class DBIOSemigroup[A: Semigroup] extends Semigroup[DBIO[A]] {
    override def combine(fx: DBIO[A], fy: DBIO[A]): DBIO[A] =
      (fx zip fy).map { case (x, y) => x |+| y }
  }

  private class DBIOMonoid[A](implicit A: Monoid[A]) extends DBIOSemigroup[A] with Monoid[DBIO[A]] {
    def empty: DBIO[A] = DBIO.successful(A.empty)
  }

  private class DBIOGroup[A](implicit A: Group[A]) extends DBIOMonoid[A] with Group[DBIO[A]] {
    def inverse(fx: DBIO[A]): DBIO[A] = fx.map(_.inverse())

    override def remove(fx: DBIO[A], fy: DBIO[A]): DBIO[A] =
      (fx zip fy).map { case (x, y) => x |-| y }
  }
}
