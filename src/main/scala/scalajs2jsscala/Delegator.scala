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

  class Converter[T, R](
    val encoder: Rep[T] => Rep[R],
    val decoder: Rep[R] => Rep[T]
  )

  type ScalaJsRuntime[T] = Converter[T, ScalaJs[T]]

  val ScalaJsRuntime: ScalaJsRuntimeImpl

  trait ScalaJsRuntimeImpl {
    def encodeListAsSeq[T: Manifest](v: Rep[List[T]])(implicit ctx: SourceContext): Rep[ScalaJs[Seq[T]]] =
      encodeSeqs(v)

    def encodeOptions[A: Manifest](r: Rep[Option[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Option[A]]]
    def encodeSeqs[A: Manifest](r: Rep[Seq[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Seq[A]]]
    def encodeMaps[A: Manifest](r: Rep[Map[String, A]])(implicit ctx: SourceContext): Rep[ScalaJs[Map[String, A]]]

    def encodeFn0[A: Manifest](r: Rep[Function0[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Function0[A]]]
    def encodeFn1[A: Manifest, R: Manifest](r: Rep[Function1[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function1[A, R]]]
    def encodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[Function1[Tuple2[A, B], R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function2[A, B, R]]]
    def encodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Function1[Tuple3[A, B, C], R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function3[A, B, C, R]]]

    def encodeTup2[A: Manifest, R: Manifest](r: Rep[Tuple2[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple2[A, R]]]
    def encodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[Tuple3[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple3[A, B, R]]]
    def encodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Tuple4[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple4[A, B, C, R]]]

    def decodeOptions[A: Manifest](r: Rep[ScalaJs[Option[A]]])(implicit ctx: SourceContext): Rep[Option[A]]
    def decodeSeqs[A: Manifest](r: Rep[ScalaJs[Seq[A]]])(implicit ctx: SourceContext): Rep[Seq[A]]
    def decodeMaps[A: Manifest](r: Rep[ScalaJs[Map[String, A]]])(implicit ctx: SourceContext): Rep[Map[String, A]]

    def decodeFn0[A: Manifest](r: Rep[ScalaJs[Function0[A]]])(implicit ctx: SourceContext): Rep[Function0[A]]
    def decodeFn1[A: Manifest, R: Manifest](r: Rep[ScalaJs[Function1[A, R]]])(implicit ctx: SourceContext): Rep[Function1[A, R]]
    def decodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Function2[A, B, R]]])(implicit ctx: SourceContext): Rep[Function1[Tuple2[A, B], R]]
    def decodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Function3[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Function1[Tuple3[A, B, C], R]]

    def decodeTup2[A: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple2[A, R]]])(implicit ctx: SourceContext): Rep[Tuple2[A, R]]
    def decodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple3[A, B, R]]])(implicit ctx: SourceContext): Rep[Tuple3[A, B, R]]
    def decodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple4[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Tuple4[A, B, C, R]]
  }

  def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[T]
  def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Rep[Any]])(implicit ctx: SourceContext): Rep[T]
  def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[T]
}

trait DelegatorExp extends DelegatorLib with FFIExp {
  case class Constant[T](name: String) extends Def[T]
  case class CallDef[T](self: Exp[ScalaJs[Any]], name: String, params: List[Exp[Any]]) extends Def[T]
  case class CallVal[T](self: Exp[ScalaJs[Any]], name: String) extends Def[T]

  val ScalaJsRuntime = new ScalaJsRuntimeImpl {
    def encodeOptions[A: Manifest](r: Rep[Option[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Option[A]]] =
      foreign"$runtime.encodeOptions($r)".pure[ScalaJs[Option[A]]]
    def encodeSeqs[A: Manifest](r: Rep[Seq[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Seq[A]]] =
      foreign"$runtime.encodeArray($r)".pure[ScalaJs[Seq[A]]]
    def encodeMaps[A: Manifest](r: Rep[Map[String, A]])(implicit ctx: SourceContext): Rep[ScalaJs[Map[String, A]]] =
      foreign"$runtime.encodeDict($r)".pure[ScalaJs[Map[String, A]]]

    def encodeFn0[A: Manifest](r: Rep[Function0[A]])(implicit ctx: SourceContext): Rep[ScalaJs[Function0[A]]] =
      foreign"$runtime.encodeFn0($r)".pure[ScalaJs[Function0[A]]]
    def encodeFn1[A: Manifest, R: Manifest](r: Rep[Function1[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function1[A, R]]] =
      foreign"$runtime.encodeFn1($r)".pure[ScalaJs[Function1[A, R]]]
    def encodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[Function1[Tuple2[A, B], R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function2[A, B, R]]] =
      foreign"$runtime.encodeFn2($r)".pure[ScalaJs[Function2[A, B, R]]]
    def encodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Function1[Tuple3[A, B, C], R]])(implicit ctx: SourceContext): Rep[ScalaJs[Function3[A, B, C, R]]] =
      foreign"$runtime.encodeFn3($r)".pure[ScalaJs[Function3[A, B, C, R]]]

    def encodeTup2[A: Manifest, R: Manifest](r: Rep[Tuple2[A, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple2[A, R]]] =
      foreign"$runtime.encodeTup2($r)".pure[ScalaJs[Tuple2[A, R]]]
    def encodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[Tuple3[A, B, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple3[A, B, R]]] =
      foreign"$runtime.encodeTup3($r)".pure[ScalaJs[Tuple3[A, B, R]]]
    def encodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[Tuple4[A, B, C, R]])(implicit ctx: SourceContext): Rep[ScalaJs[Tuple4[A, B, C, R]]] =
      foreign"$runtime.encodeTup4($r)".pure[ScalaJs[Tuple4[A, B, C, R]]]

    def decodeOptions[A: Manifest](r: Rep[ScalaJs[Option[A]]])(implicit ctx: SourceContext): Rep[Option[A]] =
      foreign"$runtime.decodeOptions($r)".pure[Option[A]]
    def decodeSeqs[A: Manifest](r: Rep[ScalaJs[Seq[A]]])(implicit ctx: SourceContext): Rep[Seq[A]] =
      foreign"$runtime.decodeSeq($r)".pure[Seq[A]]
    def decodeMaps[A: Manifest](r: Rep[ScalaJs[Map[String, A]]])(implicit ctx: SourceContext): Rep[Map[String, A]] =
      foreign"$runtime.decodeMap($r)".pure[Map[String, A]]

    def decodeFn0[A: Manifest](r: Rep[ScalaJs[Function0[A]]])(implicit ctx: SourceContext): Rep[Function0[A]] =
      foreign"$runtime.decodeFn0($r)".pure[Function0[A]]
    def decodeFn1[A: Manifest, R: Manifest](r: Rep[ScalaJs[Function1[A, R]]])(implicit ctx: SourceContext): Rep[Function1[A, R]] =
      foreign"$runtime.decodeFn1($r)".pure[Function1[A, R]]
    def decodeFn2[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Function2[A, B, R]]])(implicit ctx: SourceContext): Rep[Function1[Tuple2[A, B], R]] =
      foreign"$runtime.decodeFn2($r)".pure[Function1[Tuple2[A, B], R]]
    def decodeFn3[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Function3[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Function1[Tuple3[A, B, C], R]] =
      foreign"$runtime.decodeFn3($r)".pure[Function1[Tuple3[A, B, C], R]]

    def decodeTup2[A: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple2[A, R]]])(implicit ctx: SourceContext): Rep[Tuple2[A, R]] =
      foreign"$runtime.decodeTup2($r)".pure[Tuple2[A, R]]
    def decodeTup3[A: Manifest, B: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple3[A, B, R]]])(implicit ctx: SourceContext): Rep[Tuple3[A, B, R]] =
      foreign"$runtime.decodeTup3($r)".pure[Tuple3[A, B, R]]
    def decodeTup4[A: Manifest, B: Manifest, C: Manifest, R: Manifest](r: Rep[ScalaJs[Tuple4[A, B, C, R]]])(implicit ctx: SourceContext): Rep[Tuple4[A, B, C, R]] =
      foreign"$runtime.decodeTup4($r)".pure[Tuple4[A, B, C, R]]
  }

  def constant[T: Manifest](name: String)(implicit ctx: SourceContext): Rep[T] =
    toAtom(Constant(name))
  def callDef[T: Manifest](self: Rep[ScalaJs[Any]], name: String, params: List[Exp[Any]])(implicit ctx: SourceContext): Rep[T] =
    reflectEffect(CallDef(self, name, params))
  def callVal[T: Manifest](self: Rep[ScalaJs[Any]], name: String)(implicit ctx: SourceContext): Rep[T] =
    reflectEffect(CallVal(self, name))

  def runtime(implicit ctx: SourceContext): Exp[Any] = toAtom(foreign"scalajs2jsscala.Runtime()".pure[Any])
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
