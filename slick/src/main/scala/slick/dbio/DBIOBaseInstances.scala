package slick.dbio

import cats._
import cats.syntax.all._
import scala.util._

trait DBIOBaseInstances {
  implicit val DBIOBaseInstance: MonadError[DBIOBase, Throwable] =
    new MonadError[DBIOBase, Throwable] {
      override def pure[A](x: A): DBIOBase[A] = DBIO.successful(x)

      override def flatMap[A, B](fa: DBIOBase[A])(f: (A) => DBIOBase[B]): DBIOBase[B] = ???// fa.flatMap(f)

      /**
       * While this is roughly the same implementation as in `FutureInstances`,
       * I'm not entirely sure this is indeed stack safe. It certainly looks
       * like it should be.
       */
      override def tailRecM[A, B](a: A)(f: A => DBIOBase[Either[A, B]]): DBIOBase[B] = ???
//        f(a).flatMap {
//          case Left(a1) => tailRecM(a1)(f)
//          case Right(b) => DBIOBase.successful(b)
//        }

      override def handleError[A](fea: DBIOBase[A])(f: (Throwable) => A): DBIOBase[A] = ???
//        fea.asTry.map {
//          case Success(a) => a
//          case Failure(t) => f(t)
//        }

      override def raiseError[A](e: Throwable): DBIOBase[A] = DBIO.failed(e)

      override def map[A, B](fa: DBIOBase[A])(f: A => B): DBIOBase[B] =  ??? // fa.map(f)

      override def handleErrorWith[A](fa: DBIOBase[A])(f: (Throwable) => DBIOBase[A]): DBIOBase[A] = ???
//        fa.asTry.flatMap {
//          case Success(a) => DBIOBase.successful(a)
//          case Failure(t) => f(t)
//        }
    }

//  implicit def DBIOBaseGroup[A: Group]: Group[DBIOBase[A]] =
//    new DBIOBaseGroup[A]
//
//  implicit def DBIOBaseMonoid[A: Monoid]: Monoid[DBIOBase[A]] =
//    new DBIOBaseMonoid[A]
//
//  implicit def DBIOBaseSemigroup[A: Semigroup]: Semigroup[DBIOBase[A]] =
//    new DBIOBaseSemigroup[A]
//
//  private class DBIOBaseSemigroup[A: Semigroup] extends Semigroup[DBIOBase[A]] {
//    override def combine(fx: DBIOBase[A], fy: DBIOBase[A]): DBIOBase[A] =
//      (fx zip fy).map { case (x, y) => x |+| y }
//  }
//
//  private class DBIOBaseMonoid[A](implicit A: Monoid[A]) extends DBIOBaseSemigroup[A] with Monoid[DBIOBase[A]] {
//    def empty: DBIOBase[A] = DBIO.successful(A.empty)
//  }
//
//  private class DBIOBaseGroup[A](implicit A: Group[A]) extends DBIOBaseMonoid[A] with Group[DBIOBase[A]] {
//    def inverse(fx: DBIOBase[A]): DBIOBase[A] = fx.map(_.inverse())
//
//    override def remove(fx: DBIOBase[A], fy: DBIOBase[A]): DBIOBase[A] =
//      (fx zip fy).map { case (x, y) => x |-| y }
//  }
}
