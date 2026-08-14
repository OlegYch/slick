package slick.dbio
trait DBIOInstances2
//package slick.dbio
//
//import cats.*
//import cats.syntax.all.*
//
//import scala.util.*
//
//trait DBIOInstances2 {
//  implicit val dbioInstance: MonadError[[R] =>> DBIOAction[R, NoStream, Effect], Throwable] =
//    new MonadError[[R] =>> DBIOAction[R, NoStream, Effect], Throwable] {
//      override def pure[A](x: A): DBIOAction[A, NoStream, Effect] = DBIO.successful(x)
//
//      override def flatMap[A, B](fa: DBIOAction[A, NoStream, Effect])(f: (A) => DBIOAction[B, NoStream, Effect]): DBIOAction[B, NoStream, Effect] = fa.flatMap(f)
//
//      /**
//       * While this is roughly the same implementation as in `FutureInstances`,
//       * I'm not entirely sure this is indeed stack safe. It certainly looks
//       * like it should be.
//       */
//      override def tailRecM[A, B](a: A)(f: A => DBIOAction[Either[A, B], NoStream, Effect]): DBIOAction[B, NoStream, Effect] =
//        f(a).flatMap {
//          case Left(a1) => tailRecM(a1)(f)
//          case Right(b) => DBIO.successful(b)
//        }
//
//      override def handleError[A](fea: DBIOAction[A, NoStream, Effect])(f: (Throwable) => A): DBIOAction[A, NoStream, Effect] =
//        fea.asTry.map {
//          case Success(a) => a
//          case Failure(t) => f(t)
//        }
//
//      override def raiseError[A](e: Throwable): DBIOAction[A, NoStream, Effect] = DBIO.failed(e)
//
//      override def map[A, B](fa: DBIOAction[A, NoStream, Effect])(f: A => B): DBIOAction[B, NoStream, Effect] = fa.map(f)
//
//      override def handleErrorWith[A](fa: DBIOAction[A, NoStream, Effect])(f: (Throwable) => DBIOAction[A, NoStream, Effect]): DBIOAction[A, NoStream, Effect] =
//        fa.asTry.flatMap {
//          case Success(a) => DBIO.successful(a)
//          case Failure(t) => f(t)
//        }
//    }
//
//  implicit def dbioGroup[A: Group]: Group[DBIOAction[A, NoStream, Effect]] =
//    new DBIOGroup[A]
//
//  implicit def dbioMonoid[A: Monoid]: Monoid[DBIOAction[A, NoStream, Effect]] =
//    new DBIOMonoid[A]
//
//  implicit def dbioSemigroup[A: Semigroup]: Semigroup[DBIOAction[A, NoStream, Effect]] =
//    new DBIOSemigroup[A]
//
//  private class DBIOSemigroup[A: Semigroup] extends Semigroup[DBIOAction[A, NoStream, Effect]] {
//    override def combine(fx: DBIOAction[A, NoStream, Effect], fy: DBIOAction[A, NoStream, Effect]): DBIOAction[A, NoStream, Effect] =
//      (fx zip fy).map { case (x, y) => x |+| y }
//  }
//
//  private class DBIOMonoid[A](implicit A: Monoid[A]) extends DBIOSemigroup[A] with Monoid[DBIOAction[A, NoStream, Effect]] {
//    def empty: DBIOAction[A, NoStream, Effect] = DBIO.successful(A.empty)
//  }
//
//  private class DBIOGroup[A](implicit A: Group[A]) extends DBIOMonoid[A] with Group[DBIOAction[A, NoStream, Effect]] {
//    def inverse(fx: DBIOAction[A, NoStream, Effect]): DBIOAction[A, NoStream, Effect] = fx.map(_.inverse())
//
//    override def remove(fx: DBIOAction[A, NoStream, Effect], fy: DBIOAction[A, NoStream, Effect]): DBIOAction[A, NoStream, Effect] =
//      (fx zip fy).map { case (x, y) => x |-| y }
//  }
//}
