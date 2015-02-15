package scalajs2jsscala

import scala.annotation.StaticAnnotation
import scala.js.gen.js.GenBase
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.virtualization.lms.common._

trait DelegatorLib extends Base {
  def constant[T: Manifest](name: String): Rep[T]
  def callDef[T: Manifest](self: Rep[Any], name: String, params: List[Rep[Any]]): Rep[T]
  def callVal[T: Manifest](self: Rep[Any], name: String): Rep[T]
}

trait DelegatorExp extends DelegatorLib with BaseExp with EffectExp {
  case class Constant[T](name: String) extends Def[T]
  case class CallDef[T](self: Exp[Any], name: String, params: List[Exp[Any]]) extends Def[T]
  case class CallVal[T](self: Exp[Any], name: String) extends Def[T]

  def constant[T: Manifest](name: String): Exp[T] = toAtom(Constant(name))(implicitly[Manifest[T]], null)
  def callDef[T: Manifest](self: Exp[Any], name: String, params: List[Exp[Any]]): Exp[T] =
    reflectEffect(CallDef(self, name, params))(implicitly[Manifest[T]], null)
  def callVal[T: Manifest](self: Exp[Any], name: String): Exp[T] =
    reflectEffect(CallVal(self, name))(implicitly[Manifest[T]], null)
}

trait GenDelegator extends GenBase {
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
