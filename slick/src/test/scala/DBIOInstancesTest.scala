/**
 * Copyright 2017-present, Risk Management Solutions, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

import cats.data.EitherT
import cats.effect.*
import cats.instances.AllInstances
import cats.laws.discipline.SemigroupalTests.Isomorphisms
import cats.laws.discipline.*
import cats.syntax.all.*
import cats.{Comonad, Eq, Monad}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Cogen, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.Checkers
import org.typelevel.discipline.scalatest.FunSuiteDiscipline
import slick.dbio.{DBIO, DBIOAction, DBIOInstances, DBIOInstances3, DBIOInstances2, Effect, NoStream}
import cats.effect.unsafe.implicits.global

import scala.util.{Failure, Success}

class DBIOInstancesTest extends AnyFunSuite with Matchers with FunSuiteDiscipline with Checkers with AllInstances
  with DBIOInstances
  with DBIOInstances2
//  with DBIOInstances3
  {
  private val db = slick.memory.MemoryProfile.backend.Database()


  def dbioEither[A](f: DBIO[A]): DBIO[Either[Throwable, A]] =
    f.map(Right[Throwable, A]).asTry.map {
      case Success(x) => x
      case Failure(t) => Left(t)
    }

  implicit def eqfa[A: Eq]: Eq[DBIO[A]] =
    (fx: DBIO[A], fy: DBIO[A]) => {
      val fz = dbioEither(fx) zip dbioEither(fy)
      db.use(db => db.run(fz.map { case (tx, ty) => tx === ty })).unsafeRunSync()
    }

  implicit def arbDBIO[T](implicit a: Arbitrary[T]): Arbitrary[DBIO[T]] =
    Arbitrary(Gen.oneOf(arbitrary[T].map(DBIO.successful), arbitrary[Throwable].map(DBIO.failed)))

  implicit val throwableEq: Eq[Throwable] = Eq.fromUniversalEquals
  implicit val iso: Isomorphisms[DBIO] = SemigroupalTests.Isomorphisms.invariant[DBIO]

  // Need non-fatal Throwable for Future recoverWith/handleError
  implicit val nonFatalArbitrary: Arbitrary[Throwable] =
    Arbitrary(arbitrary[Exception].map(identity))

  implicit def cogenForDbio[A]: Cogen[DBIO[A]] =
    Cogen[Unit].contramap(_ => ())

  checkAll("DBIO[Int]", MonadErrorTests[DBIO, Throwable].monadError[Int, Int, Int])

  (0 to 10).toList.traverse{i => DBIO.successful(i)}
//
  def monad[F[_] : Monad, A](fa: F[A]): F[A] = fa
  val fail1: DBIOAction[String, NoStream, Effect.All] = DBIO.successful("hello")
  val fail2 = DBIO.successful("hello")
  val success: DBIO[String] = DBIO.successful("hello")
  monad(fail1)
  monad(fail2)
}

