package vaadin.scala

import scala.reflect.runtime._
import scala.reflect.runtime.universe._
import com.vaadin.data.util.{ PropertysetItem => VaadinPropertysetItem }
import scala.reflect.ClassTag

/**
 *
 * @author Henri Kerola / Vaadin
 */
object ImmutableScaladinItem {

  def apply[T: TypeTag](bean: T): ImmutableScaladinItem[T] = {
    new ImmutableScaladinItem(bean, ScaladinItem.getPropertyDescriptors(bean, true))
  }
}

class ImmutableScaladinItem[T: TypeTag](var bean: T, propertyDescriptors: Iterable[PropertyDescriptor[T]])
    extends PropertysetItem(new VaadinPropertysetItem) {

  propertyDescriptors foreach { pd =>
    addItemProperty(pd.name, pd.createProperty(bean))
  }

  def commit(): T = {
    implicit val classTag = ClassTag[T](bean.getClass)

    val newValues: Map[String, Any] = propertyDescriptors.map(pd => pd.name -> getProperty(pd.name).value.orNull).toMap

    bean = copy(bean, newValues)

    bean
  }

  private class Empty

  private def copy[R: ClassTag](r: R, newValues: Map[String, Any]): R = {
    val mirror = universe.runtimeMirror(r.getClass.getClassLoader)

    val instanceMirror = mirror.reflect(r)
    val decl = instanceMirror.symbol.asType.toType
    val members = decl.members.map(method => transformMethod(method, newValues, instanceMirror)).filter {
      case _: Empty => false
      case _ => true
    }.toArray.reverse
    val copyMethod = decl.declaration(newTermName("copy")).asMethod
    val copyMethodInstance = instanceMirror.reflectMethod(copyMethod)
    copyMethodInstance(members: _*).asInstanceOf[R]
  }

  private def transformMethod(method: Symbol, newValues: Map[String, Any], instanceMirror: InstanceMirror): Any = {
    val term = method.asTerm
    if (term.isAccessor) {
      if (newValues.contains(term.name.toString)) {
        newValues.apply(term.name.toString)
      } else instanceMirror.reflectField(term).get
    } else new Empty
  }
}