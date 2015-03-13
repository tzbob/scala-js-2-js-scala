import org.scalatest.FunSuite
import scala.js.exp.JSExp
import scala.js.gen.js.GenJS
import scala.js.language.JS
import scala.scalajs.js.annotation.JSExport
import scala.virtualization.lms.common._
import scalajs2jsscala._
import scalajs2jsscala.annotation.JsScalaProxy

class TestMacros extends FunSuite {

  @JsScalaProxy
  trait Behavior[+A] {
    @JSExport val testVal: Behavior[A]
    @JSExport def testSelf[B](f: A => B): Behavior[B]
    @JSExport def map[B](f: A => B): Behavior[B]
  }

  @JsScalaProxy
  @JSExport
  object Behavior {
    @JSExport def fire(pulses: Seq[(Behavior[A], A) forSome { type A }]): Unit = ???
    @JSExport def testUnit(): Unit = ???
    @JSExport def merge[A](events: Seq[Behavior[A]]): Behavior[Seq[A]] = ???
    @JSExport def make[A](): Behavior[A] = ???
    @JSExport def makeDiscrete[A](): DiscreteBehavior[A] = ???
    @JSExport def makeDiscrete[A](test: A): DiscreteBehavior[A] = ???
  }

  @JsScalaProxy
  trait IncrementalBehavior[+A, +DeltaA] extends DiscreteBehavior[A]

  @JsScalaProxy
  trait DiscreteBehavior[+A] extends Behavior[A] {
    @JSExport override val testVal: DiscreteBehavior[A]
    @JSExport def newer[B](f: A => B): DiscreteBehavior[B]
    @JSExport override def map[B](f: A => B): DiscreteBehavior[B]
  }

  test("Generated js-scala components should be available") {
    trait Test extends JS
      with Behavior.BehaviorStaticLib
      with Behavior.BehaviorLib
      with DiscreteBehavior.DiscreteBehaviorLib {
      implicit lazy val context: scala.reflect.SourceContext = ???

      val testTuplesAndExistentials: Rep[Seq[ScalaJs[(ScalaJs[Behavior[String]], String)]]] => Rep[Unit] = BehaviorRep.fire

      val testUnit: () => Rep[Unit] = BehaviorRep.testUnit

      val test: Rep[Seq[ScalaJs[Behavior[String]]]] => Rep[ScalaJs[Behavior[Seq[String]]]] = BehaviorRep.merge

      val repString: Rep[String] = "test"

      val behavior: Rep[ScalaJs[Behavior[String]]] = BehaviorRep.make()

      // test the use of an unwrapped type in an abstract type position
      val dbehavior: Rep[ScalaJs[DiscreteBehavior[String]]] = BehaviorRep.makeDiscrete(repString)

      val testVal: Rep[ScalaJs[Behavior[String]]] = behavior.testVal

      val fun: Rep[String => Int] = fun { x => 5 }

      val a: Rep[ScalaJs[Behavior[Int]]] = behavior.testSelf(fun.encode)
      val b: Rep[ScalaJs[DiscreteBehavior[Int]]] = dbehavior.newer(fun.encode)
    }
  }
}
