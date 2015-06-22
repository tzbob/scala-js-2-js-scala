import org.scalatest.FunSuite
import scala.js.exp.JSExp
import scala.js.gen.js.GenJS
import scala.js.language.JS
import scala.scalajs.js.annotation.JSExport
import scala.virtualization.lms.common._
import scalajs2jsscala._
import scalajs2jsscala.annotation.JsScalaProxy

import scala.language.existentials

class TestMacros extends FunSuite {

  class Clazz { class Projection[A] }
  trait Trait
  object Constant { class Path }
  trait Param[A, B, C]

  @JsScalaProxy
  trait Behavior[+A] {
    @JSExport val clazz: Clazz
    @JSExport val compound: Clazz with Trait
    @JSExport val compoundParam: Clazz with Param[Int, Clazz, Trait]
    @JSExport val projection: Clazz#Projection[Trait]
    @JSExport val path: Constant.Path
    @JSExport val param: Param[Int, Clazz, Trait]
    @JSExport val existential: Seq[T forSome { type T }]
    @JSExport val constant: Constant.type
    @JSExport val primitive: Int

    @JSExport def unbound[B](f: A => B): Behavior[B]
    @JSExport def primitiveBound[B <: Int](f: A => B): Behavior[B]
    @JSExport def regularBound[B <: Behavior[_]](f: A => B): Behavior[B]

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
    @JSExport def newer[B](f: A => B): DiscreteBehavior[B]
    @JSExport override def map[B](f: A => B): DiscreteBehavior[B]
  }

  test("Generated js-scala components should be available") {
    trait Test extends JS
      with Behavior.BehaviorStaticLib
      with Behavior.BehaviorLib
      with DiscreteBehavior.DiscreteBehaviorLib {
      implicit lazy val context: scala.reflect.SourceContext = ???

      val behavior: Rep[ScalaJs[Behavior[String]]] = BehaviorRep.make()

      behavior.clazz: Rep[ScalaJs[Clazz]]
      behavior.compound: Rep[ScalaJs[Clazz] with ScalaJs[Trait]]
      behavior.compoundParam: Rep[ScalaJs[Clazz] with ScalaJs[Param[Int, ScalaJs[Clazz], ScalaJs[Trait]]]]

      behavior.projection: Rep[ScalaJs[Clazz#Projection[ScalaJs[Trait]]]]
      behavior.path: Rep[ScalaJs[Constant.Path]]
      behavior.param: Rep[ScalaJs[Param[Int, ScalaJs[Clazz], ScalaJs[Trait]]]]

      behavior.existential: Rep[ScalaJs[Seq[T forSome { type T }]]]
      behavior.constant: Rep[ScalaJs[Constant.type]]
      behavior.primitive: Rep[Int]

      behavior.unbound[Int] _: Function1[Rep[ScalaJs[String => Int]], Rep[ScalaJs[Behavior[Int]]]]
      behavior.primitiveBound[Int] _: Function1[Rep[ScalaJs[String => Int]], Rep[ScalaJs[Behavior[Int]]]]
      behavior.regularBound[ScalaJs[Behavior[Int]]] _: Function1[Rep[ScalaJs[String => ScalaJs[Behavior[Int]]]], Rep[ScalaJs[Behavior[ScalaJs[Behavior[Int]]]]]]

      val testTuplesAndExistentials: Rep[ScalaJs[Seq[ScalaJs[(ScalaJs[Behavior[String]], String)]]]] => Rep[Unit] = BehaviorRep.fire

      val testUnit: () => Rep[Unit] = BehaviorRep.testUnit

      val repString: Rep[String] = "test"

      val dbehavior: Rep[ScalaJs[DiscreteBehavior[String]]] = BehaviorRep.makeDiscrete(repString)

      val fun: Rep[String => Int] = fun { x => 5 }

      val fun2: Rep[((String, Int)) => Int] = fun { (x: Rep[String], y: Rep[Int]) => 5 }
    }
  }
}
