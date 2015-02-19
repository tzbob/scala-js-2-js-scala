import java.io.PrintWriter
import java.io.StringWriter
import scala.js.gen.js.GenJS
import scala.scalajs.js.annotation.JSExport
import scala.virtualization.lms.common._
import scalajs2jsscala.annotation.JsScalaProxy
import scalajs2jsscala._
import scala.js.language.JS
import scala.js.exp.JSExp

import org.scalatest.FunSuite

class TestMacros extends FunSuite {

  @JsScalaProxy
  trait Behavior[+A] {
    @JSExport val testVal: Behavior[A]
    @JSExport def testSelf[B](f: A => B): Behavior[B]
    @JSExport def map[B](f: A => B): Behavior[B]
  }

  @JsScalaProxy
  object Behavior {
    @JSExport def varargs[A](bs: Behavior[A]*): Behavior[Seq[A]] = ???
    @JSExport def make[A](): Behavior[A] = ???
    @JSExport def makeDiscrete[A](): DiscreteBehavior[A] = ???
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
    implicit lazy val context: scala.reflect.SourceContext = ???
    trait Test extends JS
        with Behavior.BehaviorStaticLib
        with Behavior.BehaviorLib
        with DiscreteBehavior.DiscreteBehaviorLib {

      val behavior: Rep[Behavior[String]] = BehaviorRep.make()
      val dbehavior: Rep[DiscreteBehavior[String]] = BehaviorRep.makeDiscrete()

      val testVal: Rep[Behavior[String]] = behavior.testVal

      val testVarargs: Rep[Behavior[Seq[String]]] = Behavior.varargs(behavior, testVal)

      val fun: Rep[String => Int] = fun { x => 5 }
      val a: Rep[Behavior[Int]] = behavior.testSelf(fun)
      val b: Rep[DiscreteBehavior[Int]] = dbehavior.newer(fun)
    }
  }
}