package slick.dbio

import cats.*
import cats.syntax.all.*

import scala.util.*

trait DBIOInstances3 {
  type DBIOAll[R] = DBIOAction[R, NoStream, Effect]
  implicit val dbioInstance: MonadError[DBIOAll, Throwable] =
    new MonadError[DBIOAll, Throwable] {
      override def pure[A](x: A): DBIOAll[A] = DBIO.successful(x)

      override def flatMap[A, B](fa: DBIOAll[A])(f: (A) => DBIOAll[B]): DBIOAll[B] = fa.flatMap(f)

      /**
       * While this is roughly the same implementation as in `FutureInstances`,
       * I'm not entirely sure this is indeed stack safe. It certainly looks
       * like it should be.
       */
      override def tailRecM[A, B](a: A)(f: A => DBIOAll[Either[A, B]]): DBIOAll[B] =
        f(a).flatMap {
          case Left(a1) => tailRecM(a1)(f)
          case Right(b) => DBIO.successful(b)
        }

      override def handleError[A](fea: DBIOAll[A])(f: (Throwable) => A): DBIOAll[A] =
        fea.asTry.map {
          case Success(a) => a
          case Failure(t) => f(t)
        }

      override def raiseError[A](e: Throwable): DBIOAll[A] = DBIO.failed(e)

      override def map[A, B](fa: DBIOAll[A])(f: A => B): DBIOAll[B] = fa.map(f)

      override def handleErrorWith[A](fa: DBIOAll[A])(f: (Throwable) => DBIOAll[A]): DBIOAll[A] =
        fa.asTry.flatMap {
          case Success(a) => DBIO.successful(a)
          case Failure(t) => f(t)
        }
    }

  implicit def dbioGroup[A: Group]: Group[DBIOAll[A]] =
    new DBIOGroup[A]

  implicit def dbioMonoid[A: Monoid]: Monoid[DBIOAll[A]] =
    new DBIOMonoid[A]

  implicit def dbioSemigroup[A: Semigroup]: Semigroup[DBIOAll[A]] =
    new DBIOSemigroup[A]

  private class DBIOSemigroup[A: Semigroup] extends Semigroup[DBIOAll[A]] {
    override def combine(fx: DBIOAll[A], fy: DBIOAll[A]): DBIOAll[A] =
      (fx zip fy).map { case (x, y) => x |+| y }
  }

  private class DBIOMonoid[A](implicit A: Monoid[A]) extends DBIOSemigroup[A] with Monoid[DBIOAll[A]] {
    def empty: DBIOAll[A] = DBIO.successful(A.empty)
  }

  private class DBIOGroup[A](implicit A: Group[A]) extends DBIOMonoid[A] with Group[DBIOAll[A]] {
    def inverse(fx: DBIOAll[A]): DBIOAll[A] = fx.map(_.inverse())

    override def remove(fx: DBIOAll[A], fy: DBIOAll[A]): DBIOAll[A] =
      (fx zip fy).map { case (x, y) => x |-| y }
  }
}
