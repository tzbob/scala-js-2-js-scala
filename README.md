# Scala JS to JS Scala

[JS-Scala](https://github.com/js-scala/js-scala) is the most complete Javascript DSL for Scala.
[Scala JS](https://github.com/scala-js/scala-js) is the Scala to Javascript compiler.
These two projects have two completely different targets.
Having a Javascript DSL allows you to mix both regular Scala code and Javascript code within the same codebase while Scala JS allows you to use the full power of the Scala language while compiling down to Javascript.

So what if you want to write a library in Scala, using the full power of Scala, while having it accessible both as a server library and as a Js-Scala library?
This macro project does just that, it does this transformation:

```scala
@JsScalaProxy trait Behavior[+A] {
  @JSExport def map[B](f: A => B): Behavior[B]
}

@JsScalaProxy
object Behavior {
  @JSExport def make[A](): Behavior[A] = ???
}

// TO

@JsScalaProxy
object Behavior {
  @JSExport def make[A](): Behavior[A] = ???

  trait BehaviorLib extends scalajs2jsscala.DelegatorLib {
    trait BehaviorOps {
      val self: Rep[Behavior[A]]
      def map[B](f: Rep[A => B])(implicit ctx: scala.reflect.SourceContext): Rep[Behavior[B]] =
        callDef(self, "map", List(f))
    }

    implicit def addRepOps1(x: Rep[Behavior[A]]): BehaviorOps[A] =
      new BehaviorOps {
        val self = x
      }
  }

  trait BehaviorStaticLib extends scalajs2jsscala.Delegatorlib {
    def BehaviorRep(implicit ctx: scala.reflect.SourceContext): Rep[Behavior.type] =
      constant("Behavior()")
    trait BehaviorStaticOps {
      val self: Rep[Behavior.type]
      def make[A]()(implicit ctx: scala.reflect.SourceContext): Rep[Behavior[A]] =
        callDef(self, "make", List())
      implicit def addRepOps2(x: Rep[Behavior.type]): BehaviorStaticOps[A] =
        new BehaviorStaticOps {
          val self = x
        }
    }
  }
}
```

## TODO

Currently the macro is limited to Scala-Js's @JSExport annotations to mark the to-be-proxied methods. 
The transformation however is not limited to Scala-Js nor is it limited to JS-Scala.
We could generalize the approach to an LMS generator agnostic macro while providing our own annotations.
