package slick.dbio

import cats.effect.unsafe.implicits.global
import cats.instances.AllInstances
import cats.kernel.laws.discipline.MonoidTests
import cats.laws.discipline.*
import cats.syntax.all.*
import cats.{Eq, Monad}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Cogen, Gen}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.Checkers
import org.typelevel.discipline.scalatest.FunSuiteDiscipline
import slick.memory.MemoryProfile

class SlickActionInstancesTest extends AnyFunSuite with Matchers with FunSuiteDiscipline with Checkers with AllInstances {
  def run[R](a: SlickAction[NoStream, _, R]) = MemoryProfile.backend.Database().use(_.run(a)).unsafeRunSync()

  implicit val throwableEq: Eq[Throwable] = Eq.fromUniversalEquals

  implicit def actionEq[Effect <: slick.dbio.Effect, A: Eq]: Eq[SlickAction[NoStream, Effect, A]] = Eq.by(a => run(a.asTry))

  implicit def arbSlickAction[Effect <: slick.dbio.Effect, A: Arbitrary]: Arbitrary[SlickAction[NoStream, Effect, A]] = {
    val pure = Arbitrary.arbitrary[A].map(a => DBIO.successful(a))
    val failed = Arbitrary.arbitrary[Throwable].map(t => DBIO.failed(t))
    val nested = for {a <- pure; b <- Gen.frequency(3 -> pure, 1 -> failed)} yield a.flatMap(_ => b)
    val gen = Gen.frequency(4 -> pure, 1 -> failed, 2 -> nested)
    Arbitrary(gen)
  }

  private type Action[Effect <: slick.dbio.Effect, R] = SlickAction[NoStream, Effect, R]
  checkAll("DBIO[Int]", MonadErrorTests[DBIO, Throwable].monadError[Int, Int, Int])
  checkAll("DBIO[Int]", MonoidTests[DBIO[Int]].monoid)
  checkAll("SlickAction[Int]", MonadErrorTests[({type L[A] = Action[Effect, A]})#L, Throwable].monadError[Int, Int, Int])
  checkAll("SlickAction[Int]", MonoidTests[SlickAction[NoStream, Effect, Int]].monoid)
  checkAll("SlickAction.Write[Int]", MonadErrorTests[({type L[A] = Action[Effect.Write, A]})#L, Throwable].monadError[Int, Int, Int])
  checkAll("SlickAction.Write[Int]", MonoidTests[SlickAction[NoStream, Effect.Write, Int]].monoid)

  (0 to 10).toList.traverse { i => DBIO.successful(i) }
  (0 to 10).toList.traverse { i => DBIO.successful(i).transactionally }
  (0 to 10).toList.traverse { i => (DBIO.successful(i): DBIO[Int]) }

  def monad[F[_] : Monad, A](fa: F[A]): F[A] = fa

  val action = DBIO.successful("hello")
  val io: DBIO[String] = action
  monad(action.flatMap(_ => action.transactionally))
  monad(action)
  monad(action.flatMap(_ => io))
  monad(io)
  io >>= (_ => action)
  io |+| action
  val x: SlickAction[NoStream, Effect, String] = action |+| action
  val xx: SlickAction[NoStream, Effect.Write, String] = action |+| action
//    val xxx: SlickAction[NoStream, Effect, String] = action.transactionally |+| action
  //  action |+| io
  //  action >>= (_ => io)
}

