package scalajs2jsscala.annotation

import scala.annotation.StaticAnnotation
import scala.js.gen.js.GenBase
import scala.language.experimental.macros
import scala.reflect.macros.Context
import scala.util.{ Try => UTry }

class JsScalaProxy(proxies: String*) extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro JsProxyMacro.impl
}

private object JsProxyMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    import Flag._

    class AbstractTypeCheck(splice: Tree => Tree) {
      def deeper(defs: List[Tree]) = new AbstractTypeCheck(
        splice compose AbstractTypeCheck.defsToSplicer(defs)
      )

      def isAbstract(tname: TypeName): Boolean = {
        val target = q"null.asInstanceOf[$tname]"
        val dummy = q"{${splice(target)}; ()}"
        val symbol = UTry(c.typeCheck(dummy.duplicate)).toOption
          .flatMap(_.find { tree =>
            tree match {
              case q"null.asInstanceOf[${ _ }]" => true
              case _ => false
            }
          })
          .map(_.tpe.typeSymbol).flatMap { symbol =>
            if (symbol.isType) Some(symbol.asType)
            else None
          }
        symbol.fold(false)(_.isAbstractType)
      }
    }

    object AbstractTypeCheck {
      private def collectAbstractTypes(tparams: List[Tree]): List[TypeDef] = {
        val selectScala = Select(Ident(nme.ROOTPKG), newTypeName("scala"))
        val bottom = Select(selectScala, newTypeName("Nothing"))
        val top = Select(selectScala, newTypeName("Any"))

        tparams.collect {
          case i @ TypeDef(mods, tname, List(), TypeBoundsTree(bottom, top)) => i
        }
      }

      def defsToSplicer(defs: List[Tree]): Tree => Tree = { tree =>
        val freshName = c.fresh(newTypeName("Dummy$"))
        val typeNames = collectAbstractTypes(defs)
        q"class $freshName { ..$typeNames; $tree }"
      }

      def apply(defs: List[TypeDef]) =
        new AbstractTypeCheck(defsToSplicer(defs))
    }

    class EncodeWithScalaJs(atc: AbstractTypeCheck) extends Transformer {
      private def shouldNotWrap(tname: TypeName) = {
        val nativeTypes = Set(
          "String",
          "Boolean",
          "Byte",
          "Short",
          "Int",
          "Float",
          "Double",
          "Unit"
        ).map(str => newTypeName(str))
        nativeTypes.contains(tname) || atc.isAbstract(tname)
      }

      override def transform(t: Tree): Tree = t match {
        case Ident(tname: TypeName) =>
          if (shouldNotWrap(tname)) tq"$tname"
          else tq"ScalaJs[$tname]"
        case ExistentialTypeTree(t, typeDefList) =>
          ExistentialTypeTree(
            new EncodeWithScalaJs(atc.deeper(typeDefList)).transform(t),
            typeDefList
          )
        case tq"$x[..$ys]" if !ys.isEmpty => tq"ScalaJs[$x[..${ys.map(x => transform(x))}]]"
        case other => super.transform(other)
      }
    }

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

    def createReifiedMembers(body: List[Tree])(parentAtc: AbstractTypeCheck): List[DefDef] = {
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
          q"${toOverrideMods(mods)} def $tname(implicit ctx: scala.reflect.SourceContext): Rep[ScalaJs[$tpt]] = callVal(self$$, ${tname.encoded})"

        case q"$mods def $name[..$tparams](...$vparamss): $tpt = ${ _ }" if containsProxyAnnotation(mods) =>

          val atc = parentAtc.deeper(tparams)
          val enc = new EncodeWithScalaJs(atc)

          def transformedRep(t: Tree) = tq"Rep[${enc.transform(t)}]"

          val vRepParams = vparamss.map { subList =>
            subList.map { v =>
              val encType = transformedRep(v.tpt)
              q"val ${v.name}: $encType"
            }
          }
          val returnType = transformedRep(tpt)
          val params = vRepParams.flatten.map(_.name)
          q"""${toOverrideMods(mods)} def $name[..$tparams](...$vRepParams)(implicit ctx: scala.reflect.SourceContext): $returnType = callDef(self$$, ${name.encoded}, $params)"""
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

      val ClassDef(_, _, tparams, Template(parents, _, _)) = target
      val atc = AbstractTypeCheck(tparams)
      val reifiedMembers = createReifiedMembers(target.impl.body)(atc)

      val opsParents = parents.collect {
        case AppliedTypeTree(Ident(tName: TypeName), xs) =>
          AppliedTypeTree(Ident(mkOpsName(tName)), xs)
        case Ident(tName: TypeName) =>
          Ident(mkOpsName(tName))
      }

      val selfType = tq"Rep[ScalaJs[$tName[..$tNames]]]"

      val lib = q"""trait $libName extends scalajs2jsscala.DelegatorLib with ..${depTypeTree(target.impl)} { 
            trait $opsName[..$tParams] extends ..$opsParents { 
                val self$$: $selfType
                ..$reifiedMembers
            }

            import scala.language.implicitConversions
            implicit def $implicitOpsName[..$tParamsUnmodded](x: $selfType): $opsName[..$tNames] = 
                new $opsName[..$tNames] {
                    val self$$: $selfType = x
                }
        }"""

      q"$compMods object $compName extends ..$compBase { ..$compBody; $lib }"
    }

    def modify(target: ClassDef, mod: Option[ModuleDef]): c.Expr[Any] = {
      val companion = mod.getOrElse(q"object ${target.name.toTermName}")
      val modifiedCompanion = expandCompanion(target, companion)
      c.Expr[Any](Block(List(target, modifiedCompanion), Literal(Constant(()))))
    }

    /**
     * Adds the StaticOps within a StaticLib for the given module
     */
    def expandSelf(mod: ModuleDef) = {
      val q"$compMods object $compName extends ..$compBase { ..$compBody }" = mod

      // typecheck a dummy to retrieve our owner
      val freshName = c.fresh(newTypeName("Probe$"))
      val probe = c.typeCheck(q""" {class $freshName; ()} """)
      val owner = probe match { case Block(List(t), r) => t.symbol.owner.fullName }

      val staticName = newTermName(s"${compName.encoded}Rep")
      val libName = newTypeName(s"${compName.encoded}StaticLib")
      val opsName = newTypeName(s"${compName.encoded}StaticOps")
      val implicitOpsName = newTermName(c.fresh)

      val atc = AbstractTypeCheck(Nil)
      val reifiedMembers = createReifiedMembers(mod.impl.body)(atc)

      val selfType = tq"Rep[ScalaJs[$compName.type]]"

      val lib = q"""trait $libName extends scalajs2jsscala.DelegatorLib {
            def $staticName(implicit ctx: scala.reflect.SourceContext): $selfType = constant(${owner + "." + compName.encoded + "()"})
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
