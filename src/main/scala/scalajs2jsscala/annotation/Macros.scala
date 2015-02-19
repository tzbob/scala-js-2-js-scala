package scalajs2jsscala.annotation

import scala.annotation.StaticAnnotation
import scala.js.gen.js.GenBase
import scala.language.experimental.macros
import scala.reflect.macros.Context

class JsScalaProxy(proxies: String*) extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro JsProxyMacro.impl
}

private object JsProxyMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._

    def stop(msg: String) = c.abort(c.enclosingPosition, msg)

    def mkOpsName(tName: TypeName) = newTypeName(s"${tName.encoded}Ops")

    def depTypeTree(tgtImpl: Template) = {
      val Template(types, _, _) = tgtImpl
      val dependencies = types.collect {
        case Ident(typeName: TypeName) => typeName.toTermName
        case AppliedTypeTree(Ident(typeName: TypeName), _) => typeName.toTermName
      }
      dependencies.map { d =>
        Select(Ident(d), newTypeName(s"${d.encoded}Lib"))
      }
    }

    def createReifiedMembers(body: List[Tree]): List[DefDef] = {
      def containsProxyAnnotation(mods: Modifiers): Boolean = mods.annotations.exists {
        case q"new JSExport()" => true
        case q"new scala.scalajs.js.annotation.JSExport()" => true
        case _ => false
      }

      def toOverrideMods(mods: Modifiers): Modifiers = {
        val annots = mods.annotations.filter {
          case q"new JSExport()" => false
          case q"new scala.scalajs.js.annotation.JSExport()" => false
          case _ => true
        }
        val flags = if (mods.hasFlag(OVERRIDE)) OVERRIDE else NoFlags
        Modifiers(flags, mods.privateWithin, annots)
      }

      body.collect {
        case q"$mods val $tname: $tpt = ${ _ }" if containsProxyAnnotation(mods) =>
          val result = q"def $tname(implicit ctx: scala.reflect.SourceContext): Rep[$tpt] = callVal(self$$, ${tname.encoded})"
          q"${toOverrideMods(mods)} def $tname(implicit ctx: scala.reflect.SourceContext): Rep[$tpt] = callVal(self$$, ${tname.encoded})"

        case q"$mods def $name[..$tparams](...$vparamss): $tpt = ${ _ }" if containsProxyAnnotation(mods) =>
          val vRepParams = vparamss.map { subList =>
            subList.map { v =>
              q"val ${v.name}: Rep[${v.tpt}]"
            }
          }
          val params = vRepParams.flatten.map(_.name)
          q"""${toOverrideMods(mods)} def $name[..$tparams](...$vRepParams)(implicit ctx: scala.reflect.SourceContext): Rep[$tpt] = callDef(self$$, ${name.encoded}, $params)"""
      }
    }

    def expandCompanion(target: ClassDef, mod: ModuleDef): ModuleDef = {
      val q"$compMods object $compName extends ..$compBase { ..$compBody }" = mod

      val tName = target.name
      val tParams = target.tparams
      val tParamsUnmodded = tParams.map {
        case TypeDef(_, n, l, tpe) => TypeDef(Modifiers(), n, l, tpe)
      }
      val tNames = tParams.map(_.name)

      val libName = newTypeName(s"${compName.encoded}Lib")

      val opsName = mkOpsName(tName)
      val implicitOpsName = newTermName(c.fresh)

      val reifiedMembers = createReifiedMembers(target.impl.body)

      val opsParents = {
        val ClassDef(_, _, _, Template(types, _, _)) = target
        types.collect {
          case AppliedTypeTree(Ident(tName: TypeName), xs) =>
            AppliedTypeTree(Ident(mkOpsName(tName)), xs)
          case Ident(tName: TypeName) =>
            Ident(mkOpsName(tName))
        }
      }

      val lib = q"""trait $libName extends scalajs2jsscala.DelegatorLib with ..${depTypeTree(target.impl)} { 
            trait $opsName[..$tParams] extends ..$opsParents { 
                val self$$: Rep[$tName[..$tNames]]
                ..$reifiedMembers
            }

            import scala.language.implicitConversions
            implicit def $implicitOpsName[..$tParamsUnmodded](x: Rep[$tName[..$tNames]]): $opsName[..$tNames] = 
                new $opsName[..$tNames] {
                    val self$$: Rep[$tName[..$tNames]] = x
                }
        }"""


      q"$compMods object $compName extends ..$compBase { ..$compBody; $lib }"
    }

    def modify(target: ClassDef, mod: Option[ModuleDef]): c.Expr[Any] = {
      val companion = mod.getOrElse(q"object ${target.name.toTermName}")
      val modifiedCompanion = expandCompanion(target, companion)
      c.Expr[Any](Block(List(target, modifiedCompanion), Literal(Constant(()))))
    }

    def expandSelf(mod: ModuleDef) = {
      val q"$compMods object $compName extends ..$compBase { ..$compBody }" = mod

      val staticName = newTermName(s"${compName.encoded}Rep")
      val libName = newTypeName(s"${compName.encoded}StaticLib")
      val opsName = newTypeName(s"${compName.encoded}StaticOps")
      val implicitOpsName = newTermName(c.fresh)

      val reifiedMembers = createReifiedMembers(mod.impl.body)

      val selfType = tq"Rep[$compName.type]"

      val lib = q"""trait $libName extends scalajs2jsscala.DelegatorLib {
            def $staticName(implicit ctx: scala.reflect.SourceContext): $selfType = constant(${compName.encoded + "()"})
            trait $opsName { 
                val self$$: $selfType
                ..$reifiedMembers
            }

            import scala.language.implicitConversions
            implicit def $implicitOpsName(x: $selfType): $opsName = 
                new $opsName {
                    val self$$: $selfType = x
                }
        }"""

      val modifiedCompanion = q"$compMods object $compName extends ..$compBase { ..$compBody; $lib }"
      println(modifiedCompanion)
      c.Expr[Any](Block(List(modifiedCompanion), Literal(Constant(()))))
    }

    val result = annottees.map(_.tree).toList match {
      case (companion: ModuleDef) :: Nil => expandSelf(companion)
      case (target: ClassDef) :: Nil => modify(target, None)
      case (target: ClassDef) :: (companion: ModuleDef) :: Nil => modify(target, Some(companion))
    }
    result
  }
}

