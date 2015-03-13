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
    val decoder: Rep[ScalaJs[T]] => Rep[T]
  ) 

  implicit class Encoder[T: ScalaJsRuntime](v: Rep[T]) {
    val encode: Rep[ScalaJs[T]] =
      implicitly[ScalaJsRuntime[T]].encoder(v)
  }

  implicit class Decoder[T: ScalaJsRuntime](v: Rep[ScalaJs[T]]) {
    val decode: Rep[T] = implicitly[ScalaJsRuntime[T]].decoder(v)
  }

  object ScalaJsRuntime {
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

    implicit def tup2[A: Manifest, B: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Tuple2[A, B]] =
      new ScalaJsRuntime(encodeTup2[A, B], decodeTup2[A, B])
    implicit def tup3[A: Manifest, B: Manifest, R: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Tuple3[A, B, R]] =
      new ScalaJsRuntime(encodeTup3[A, B, R], decodeTup3[A, B, R])
    implicit def tup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](implicit ctx: SourceContext): ScalaJsRuntime[Tuple4[A, B, C, R]] =
      new ScalaJsRuntime(encodeTup4[A, B, C, R], decodeTup4[A, B, C, R])
  }

  def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[T]
  def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Rep[Any]])(implicit ctx: SourceContext): Rep[T]
  def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[T]

  def encodeIdentical[A: Manifest](r: Rep[A])(implicit ctx: SourceContext): Rep[ScalaJs[A]]
  def encodeOptions[A: Manifest](r: Rep[Option[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Option[A]]]
  def encodeFn0[A: Manifest](r: Rep[Function0[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Function0[A]]]
  def encodeFn1[A: Manifest, R: Manifest](r: Rep[Function1[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function1[A, R]]]
  def encodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[Function2[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function2[A, B, R]]]
  def encodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Function3[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function3[A, B, C, R]]]
  def encodeTup2[A: Manifest, R: Manifest](r: Rep[Tuple2[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple2[A, R]]]
  def encodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[Tuple3[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple3[A, B, R]]]
  def encodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Tuple4[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple4[A, B, C, R]]]

  def decodeIdentical[A: Manifest](r: Rep[ScalaJs[A]])(implicit ctx: SourceContext): Rep[A]
  def decodeOptions[A: Manifest](r: Rep[ScalaJs[Option[A]]])(implicit ctx: SourceContext): Rep[Option[A]]
  def decodeFn0[A: Manifest](r: Rep[ScalaJs[Function0[A]]])(implicit ctx: SourceContext): Rep[Function0[A]]
  def decodeFn1[A: Manifest, R: Manifest](r: Rep[ScalaJs[Function1[A, R]]])(implicit ctx: SourceContext): Rep[Function1[A, R]]
  def decodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Function2[A, B, R]]])(implicit ctx: SourceContext): Rep[Function2[A, B, R]]
  def decodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Function3[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Function3[A, B, C, R]]
  def decodeTup2[A: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple2[A, R]]])(implicit ctx: SourceContext): Rep[Tuple2[A, R]]
  def decodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple3[A, B, R]]])(implicit ctx: SourceContext): Rep[Tuple3[A, B, R]]
  def decodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple4[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Tuple4[A, B, C, R]]
}

trait DelegatorExp extends DelegatorLib with FFIExp {
  case class Constant[T](name: String) extends Def[T]
  case class CallDef[T](self: Exp[ScalaJs[Any]], name: String, params: List[Exp[Any]]) extends Def[T]
  case class CallVal[T](self: Exp[ScalaJs[Any]], name: String) extends Def[T]

  def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[T] =
    toAtom(Constant(name))
  def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Exp[Any]])(implicit ctx: SourceContext): Rep[T] =
    reflectEffect(CallDef(self, name, params))
  def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[T] =
    reflectEffect(CallVal(self, name))

  def runtime(implicit ctx: SourceContext): Exp[Any] = toAtom(foreign"scalajs2jsscala.Runtime()".pure[Any])

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
  def encodeTup2[A: Manifest, R: Manifest](r: Rep[Tuple2[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple2[A, R]]] =
    foreign"$runtime.encodeTup2($r)".pure[ScalaJs[Tuple2[A, R]]]
  def encodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[Tuple3[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple3[A, B, R]]] =
    foreign"$runtime.encodeTup3($r)".pure[ScalaJs[Tuple3[A, B, R]]]
  def encodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Tuple4[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple4[A, B, C, R]]] =
    foreign"$runtime.encodeTup4($r)".pure[ScalaJs[Tuple4[A, B, C, R]]]

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
  def decodeTup2[A: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple2[A, R]]])(implicit ctx: SourceContext): Rep[Tuple2[A, R]] =
    foreign"$runtime.decodeTup2($r)".pure[Tuple2[A, R]]
  def decodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple3[A, B, R]]])(implicit ctx: SourceContext): Rep[Tuple3[A, B, R]] =
    foreign"$runtime.decodeTup3($r)".pure[Tuple3[A, B, R]]
  def decodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple4[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Tuple4[A, B, C, R]] =
    foreign"$runtime.decodeTup4($r)".pure[Tuple4[A, B, C, R]]
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
