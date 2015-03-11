package scalajs2jsscala

import scala.annotation.StaticAnnotation
import scala.js.exp.FFIExp
import scala.js.gen.js.GenFFI
import scala.language.experimental.macros
import scala.reflect.SourceContext
import scala.reflect.macros.Context
import scala.virtualization.lms.common._

trait DelegatorLib extends Base {
  trait ScalaJs[+A]

  class ScalaJsRuntime[T](
    val encoder: Rep[T] => Rep[ScalaJs[T]],
    val decoder: Rep[ScalaJs[T]] => Rep[T] = null
  ) 

  implicit class Encoder[T: ScalaJsRuntime](v: Rep[T]) {
    val encode: Rep[ScalaJs[T]] =
      implicitly[ScalaJsRuntime[T]].encoder(v)
  }

  implicit class Decoder[T: ScalaJsRuntime](v: Rep[ScalaJs[T]]) {
    val decode: Rep[T] = implicitly[ScalaJsRuntime[T]].decoder(v)
  }

  object ScalaJsRuntime {
    // http://www.scala-js.org/doc/js-interoperability.html
    def directInterop[A: Manifest](implicit ctx: SourceContext) =
      new ScalaJsRuntime(encodeIdentical[A], decodeIdentical[A])

    // direct correspondence
    implicit def strings(implicit ctx: SourceContext): ScalaJsRuntime[String] = directInterop
    implicit def booleans(implicit ctx: SourceContext): ScalaJsRuntime[Boolean] = directInterop

    implicit def bytes(implicit ctx: SourceContext): ScalaJsRuntime[Byte] = directInterop
    implicit def shorts(implicit ctx: SourceContext): ScalaJsRuntime[Short] = directInterop
    implicit def ints(implicit ctx: SourceContext): ScalaJsRuntime[Int] = directInterop
    implicit def floats(implicit ctx: SourceContext): ScalaJsRuntime[Float] = directInterop
    implicit def doubles(implicit ctx: SourceContext): ScalaJsRuntime[Double] = directInterop

    implicit def units(implicit ctx: SourceContext): ScalaJsRuntime[Unit] = directInterop

    // js.Array[T] <--> mutable.Seq[T]
    implicit def seqs[A: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Seq[A]] = directInterop
    // js.Dictionary[T] <--> mutable.Map[String, T]
    implicit def dictionaries[V: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Map[String, V]] = directInterop

    // convert from js.UndefOr
    implicit def options[A: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Option[A]] =
      new ScalaJsRuntime(encodeOptions[A], decodeOptions[A])

    // www.scala-js.org/doc/calling-javascript.html
    implicit def fn0[R: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Function0[R]] =
      new ScalaJsRuntime(encodeFn0[R], decodeFn0[R])
    implicit def fn1[A: Manifest, R: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Function1[A, R]] =
      new ScalaJsRuntime(encodeFn1[A, R], decodeFn1[A, R])
    implicit def fn2[A: Manifest, B: Manifest, R: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Function2[A, B, R]] =
      new ScalaJsRuntime(encodeFn2[A, B, R], decodeFn2[A, B, R])
    implicit def fn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Function3[A, B, C, R]] =
      new ScalaJsRuntime(encodeFn3[A, B, C, R], decodeFn3[A, B, C, R])
  }

  def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[ScalaJs[T]]
  def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Rep[ScalaJs[Any]]])(implicit ctx: SourceContext): Rep[ScalaJs[T]]
  def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[ScalaJs[T]]

  def encodeIdentical[A: Manifest](r: Rep[A])(implicit ctx: SourceContext): Rep[ScalaJs[A]]
  def encodeOptions[A: Manifest](r: Rep[Option[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Option[A]]]
  def encodeFn0[A: Manifest](r: Rep[Function0[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Function0[A]]]
  def encodeFn1[A: Manifest, R: Manifest](r: Rep[Function1[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function1[A, R]]]
  def encodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[Function2[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function2[A, B, R]]]
  def encodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Function3[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function3[A, B, C, R]]]

  def decodeIdentical[A: Manifest](r: Rep[ScalaJs[A]])(implicit ctx: SourceContext): Rep[A]
  def decodeOptions[A: Manifest](r: Rep[ScalaJs[Option[A]]])(implicit ctx: SourceContext): Rep[Option[A]]
  def decodeFn0[A: Manifest](r: Rep[ScalaJs[Function0[A]]])(implicit ctx: SourceContext): Rep[Function0[A]]
  def decodeFn1[A: Manifest, R: Manifest](r: Rep[ScalaJs[Function1[A, R]]])(implicit ctx: SourceContext): Rep[Function1[A, R]]
  def decodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Function2[A, B, R]]])(implicit ctx: SourceContext): Rep[Function2[A, B, R]]
  def decodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Function3[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Function3[A, B, C, R]]
}

trait DelegatorExp extends DelegatorLib with FFIExp {
  case class Constant[T](name: String) extends Def[ScalaJs[T]]
  case class CallDef[T](self: Exp[ScalaJs[Any]], name: String, params: List[Exp[Any]]) extends Def[ScalaJs[T]]
  case class CallVal[T](self: Exp[ScalaJs[Any]], name: String) extends Def[ScalaJs[T]]

  def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[ScalaJs[T]] =
    toAtom(Constant(name))
  def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Exp[ScalaJs[Any]]])(implicit ctx: SourceContext): Rep[ScalaJs[T]] =
    reflectEffect(CallDef(self, name, params))
  def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[ScalaJs[T]] =
    reflectEffect(CallVal(self, name))

  def runtime(implicit ctx: SourceContext): Exp[Any] = toAtom(foreign"scalajs2jsscala.Runtime()".pure[Any])

  def encodeIdentical[A: Manifest](r: Rep[A])(implicit ctx: SourceContext): Rep[ScalaJs[A]] =
    foreign"$r".pure[ScalaJs[A]]
  def encodeOptions[A: Manifest](r: Rep[Option[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Option[A]]] =
    foreign"$runtime.encodeOptions($r)".pure[ScalaJs[Option[A]]]
  def encodeFn0[A: Manifest](r: Rep[Function0[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Function0[A]]] =
    foreign"$runtime.encodeFn0($r)".pure[ScalaJs[Function0[A]]]
  def encodeFn1[A: Manifest, R: Manifest](r: Rep[Function1[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function1[A, R]]] =
    foreign"$runtime.encodeFn1($r)".pure[ScalaJs[Function1[A, R]]]
  def encodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[Function2[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function2[A, B, R]]] =
    foreign"$runtime.encodeFn2($r)".pure[ScalaJs[Function2[A, B, R]]]
  def encodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Function3[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function3[A, B, C, R]]] =
    foreign"$runtime.encodeFn3($r)".pure[ScalaJs[Function3[A, B, C, R]]]

  def decodeIdentical[A: Manifest](r: Rep[ScalaJs[A]])(implicit ctx: SourceContext): Rep[A] =
    foreign"$r".pure[A]
  def decodeOptions[A: Manifest](r: Rep[ScalaJs[Option[A]]])(implicit ctx: SourceContext): Rep[Option[A]] =
    foreign"$runtime.decodeOptions($r)".pure[Option[A]]
  def decodeFn0[A: Manifest](r: Rep[ScalaJs[Function0[A]]])(implicit ctx: SourceContext): Rep[Function0[A]] =
    foreign"$runtime.decodeFn0($r)".pure[Function0[A]]
  def decodeFn1[A: Manifest, R: Manifest](r: Rep[ScalaJs[Function1[A, R]]])(implicit ctx: SourceContext): Rep[Function1[A, R]] =
    foreign"$runtime.decodeFn1($r)".pure[Function1[A, R]]
  def decodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Function2[A, B, R]]])(implicit ctx: SourceContext): Rep[Function2[A, B, R]] =
    foreign"$runtime.decodeFn2($r)".pure[Function2[A, B, R]]
  def decodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Function3[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Function3[A, B, C, R]] =
    foreign"$runtime.decodeFn3($r)".pure[Function3[A, B, C, R]]
}

trait GenDelegator extends GenFFI {
  val IR: DelegatorExp
  import IR._
  override def emitNode(sym: Sym[Any], rhs: Def[Any]) = rhs match {
    case Constant(name) => emitValDef(sym, name)
    case CallVal(self, name) =>
      emitValDef(sym, s"${quote(self)}.$name")
    case CallDef(self, name, params) =>
      val paramsString = params.map(quote).mkString(", ")
      emitValDef(sym, s"${quote(self)}.$name($paramsString)")
    case _ => super.emitNode(sym, rhs)
  }
}
