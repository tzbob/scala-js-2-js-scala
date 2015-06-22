# Scala JS to JS Scala

[JS-Scala](https://github.com/js-scala/js-scala) is the most complete Javascript DSL for Scala.
[Scala JS](https://github.com/scala-js/scala-js) is the Scala to Javascript compiler.
These two projects have two completely different goals.
Having a Javascript DSL allows you to mix both regular Scala code and Javascript code within the same codebase while Scala JS allows you to use the full power of the Scala language while compiling down to Javascript.

So what if you want to write a library in Scala, using the full power of Scala, while having it accessible both as a server library and as a Js-Scala library?
This macro project does just that, it does the following transformation:

```scala
  @JsScalaProxy
  trait Maybe[A] {
    @JSExport() def map[B](f: A => B): Behavior[B]
  };

  @JsScalaProxy
  object Maybe {
    @JSExport() def make[A](): Maybe[A] = ???
    trait MaybeLib extends scalajs2jsscala.DelegatorLib {
      trait MaybeOps[A] extends  {
        val self$: Rep[ScalaJs[Maybe[A]]];
        def map[B](f: Rep[ScalaJs[A => B]])(implicit ctx: scala.reflect.SourceContext): Rep[ScalaJs[Behavior[B]]] =
          callDef(self$, "map", scala.collection.immutable.List(f))
      };
      import scala.language.implicitConversions;
      implicit def $1$[A](x: Rep[ScalaJs[Maybe[A]]]): MaybeOps[A] =
        new MaybeOps[A] {
          val self$: Rep[ScalaJs[Maybe[A]]] = x
        }
      }


    trait MaybeStaticLib extends scalajs2jsscala.DelegatorLib {
      def MaybeRep(implicit ctx: scala.reflect.SourceContext): Rep[ScalaJs[Maybe.type]] = constant("TestMacros.Maybe()");
      abstract trait MaybeStaticOps extends scala.AnyRef {
        val self$: Rep[ScalaJs[Maybe.type]];
        def make[A]()(implicit ctx: scala.reflect.SourceContext): Rep[ScalaJs[Maybe[A]]] =
          callDef(self$, "make", scala.collection.immutable.List())
      }
      import scala.language.implicitConversions;
      implicit def $2$(x: Rep[ScalaJs[Maybe.type]]): MaybeStaticOps =
        new MaybeStaticOps {
          val self$: Rep[ScalaJs[Maybe.type]] = x
        }
      }
    }
```
## Try it out!

Please note that this README describes a way to get our proof-of-concept
implementation running for experimentation.  Because it uses unstable
versions of some libraries and the libraries have changed since the
implementation, this README describes downloading old versions from
github for those libraries.  We have verified that this procedure
works for a user with a fresh Scala install on our system, but this is
hard to guarantee.

### Install [SBT](http://www.scala-sbt.org/)

Please follow the install guide on the official [scala-sbt.org](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html#installing-sbt) website.

### Install [LMS](https://github.com/TiarkRompf/virtualization-lms-core)

    git clone https://github.com/TiarkRompf/virtualization-lms-core.git
    cd virtualization-lms-core
    git checkout fabff2a64983cfb7a787db186d51c249228b9ffc
    sbt publish-local
    cd ..

### Install [JS-Scala](https://github.com/js-scala/js-scala)

    git clone https://github.com/js-scala/js-scala.git
    cd js-scala
    git checkout febdc39e3e1da7803ec51268781bc883da2dcda6
    sbt publish-local
    cd ..

### Install
    git clone git@github.com:Tzbob/scala-js-2-js-scala.git
    cd scala-js-2-js-scala
    git checkout gpce15
    sbt test

## Technical Details

Code in LMS is written in 3 (or 4) layers: Interface, Intermediate Representation, (Optimisation) and Generation.
The macro in this project takes care of the first layer and generates interfaces as shown above.
The two other mandatory layers are handled with a small library implementation named ```DelegatorLib```.

### DelegatorLib

As you can see the in the generated interfaces above, all methods forward to invocations of methods such as ```callDef```.
These methods are provided by ```DelegatorLib```.

It contains the following definitions:
```scala
    def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[T]
    def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Rep[Any]])(implicit ctx: SourceContext): Rep[T]
    def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[T]
```

Which corresponds to the intermediate representations of calling constants(pure), definitions(effectful) and values(effectful).

A small implementation for JS-Scala is given:
```scala
    override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
        case Constant(name) => emitValDef(sym, name)
        case CallVal(self, name) => emitValDef(sym, s"${quote(self)}.$name")
        case CallDef(self, name, params) => val paramsString = params.map(quote).mkString(", ")
            emitValDef(sym, s"${quote(self)}.$name($paramsString)") case _ => super.emitNode(sym, rhs)
    }
```

### Scala-Virtualized and Macro Annotations

This project targets Scala 2.10.2 Virtualized.
We use [Macro Annotations](http://docs.scala-lang.org/overviews/macros/annotations.html) which are added by the Scala Macroparadise compiler plugin.
However, the combination of both is not supported.
Adding the macro paradise plugin breaks the ```SourceContext``` injection by Scala-Virtualized which is why the generated interfaces all explicitly require the ```SourceContext``` at their call-site.
As soon as JS-Scala supports the macro-based LMS on Scala 2.11.x we will adapt our project as well to remove the dependency on Scala-Virtualized.
