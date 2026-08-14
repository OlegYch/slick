package slick.dbio

import cats.*
import cats.syntax.all.*

import scala.util.*

trait DBIOInstances3 {
  implicit val dbioInstance3: MonadError[[R] =>> DBIOAction[NoStream, Effect, R], Throwable] =
    new MonadError[[R] =>> DBIOAction[NoStream, Effect, R], Throwable] {
      override def pure[A](x: A): DBIOAction[NoStream, Effect, A] = DBIO.successful(x)

      override def flatMap[A, B](fa: DBIOAction[NoStream, Effect, A])(f: (A) => DBIOAction[NoStream, Effect, B]): DBIOAction[ NoStream, Effect, B] = fa.flatMap(f)

      /**
       * While this is roughly the same implementation as in `FutureInstances`,
       * I'm not entirely sure this is indeed stack safe. It certainly looks
       * like it should be.
       */
      override def tailRecM[A, B](a: A)(f: A => DBIOAction[NoStream, Effect, Either[A, B]]): DBIOAction[NoStream, Effect, B] =
        f(a).flatMap {
          case Left(a1) => tailRecM(a1)(f)
          case Right(b) => DBIO.successful(b)
        }

      override def handleError[A](fea: DBIOAction[NoStream, Effect, A])(f: (Throwable) => A): DBIOAction[NoStream, Effect, A] =
        fea.asTry.map {
          case Success(a) => a
          case Failure(t) => f(t)
        }

      override def raiseError[A](e: Throwable): DBIOAction[NoStream, Effect, A] = DBIO.failed(e)

      override def map[A, B](fa: DBIOAction[NoStream, Effect, A])(f: A => B): DBIOAction[NoStream, Effect, B] = fa.map(f)

      override def handleErrorWith[A](fa: DBIOAction[NoStream, Effect, A])(f: (Throwable) => DBIOAction[NoStream, Effect, A]): DBIOAction[NoStream, Effect, A] =
        fa.asTry.flatMap {
          case Success(a) => DBIO.successful(a)
          case Failure(t) => f(t)
        }
    }

  implicit def dbioGroup3[A: Group]: Group[DBIOAction[NoStream, Effect, A]] =
    new DBIOGroup[A]

  implicit def dbioMonoid3[A: Monoid]: Monoid[DBIOAction[NoStream, Effect, A]] =
    new DBIOMonoid[A]

  implicit def dbioSemigroup3[A: Semigroup]: Semigroup[DBIOAction[NoStream, Effect, A]] =
    new DBIOSemigroup[A]

  private class DBIOSemigroup[A: Semigroup] extends Semigroup[DBIOAction[NoStream, Effect, A]] {
    override def combine(fx: DBIOAction[NoStream, Effect, A], fy: DBIOAction[NoStream, Effect, A]): DBIOAction[NoStream, Effect, A] =
      (fx zip fy).map { case (x, y) => x |+| y }
  }

  private class DBIOMonoid[A](implicit A: Monoid[A]) extends DBIOSemigroup[A] with Monoid[DBIOAction[NoStream, Effect, A]] {
    def empty: DBIOAction[NoStream, Effect, A] = DBIO.successful(A.empty)
  }

  private class DBIOGroup[A](implicit A: Group[A]) extends DBIOMonoid[A] with Group[DBIOAction[NoStream, Effect, A]] {
    def inverse(fx: DBIOAction[NoStream, Effect, A]): DBIOAction[NoStream, Effect, A] = fx.map(_.inverse())

    override def remove(fx: DBIOAction[NoStream, Effect, A], fy: DBIOAction[NoStream, Effect, A]): DBIOAction[NoStream, Effect, A] =
      (fx zip fy).map { case (x, y) => x |-| y }
  }
}
