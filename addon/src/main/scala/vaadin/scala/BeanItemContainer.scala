package vaadin.scala

import vaadin.scala.mixins.BeanItemContainerMixin
import scala.collection.JavaConverters._
import scala.reflect.ClassTag

package mixins {
  trait BeanItemContainerMixin extends ContainerIndexedMixin
}

class BeanItemContainer[BT](override val p: com.vaadin.data.util.BeanItemContainer[BT] with BeanItemContainerMixin)
    extends Wrapper with Container.Indexed {

  p.wrapper = this

  def this()(implicit m: ClassTag[BT]) = {
    this(new com.vaadin.data.util.BeanItemContainer[BT](m.runtimeClass.asInstanceOf[Class[BT]]) with BeanItemContainerMixin)
  }

  def this(beans: Iterable[BT])(implicit m: ClassTag[BT]) = {
    this(new com.vaadin.data.util.BeanItemContainer[BT](m.runtimeClass.asInstanceOf[Class[BT]], beans.asJavaCollection) with BeanItemContainerMixin)
  }

  def addBean(bean: BT): BeanItem[BT] = new BeanItem[BT](bean)

  def wrapItem(unwrapped: com.vaadin.data.Item): Item = {
    // must create BeanItem with the constructor that takes a Vaadin BeanItem not a bean.
    new BeanItem[BT](unwrapped.asInstanceOf[com.vaadin.data.util.BeanItem[BT]])
  }
}

class BeanItem[BT](override val p: com.vaadin.data.util.BeanItem[BT]) extends Item {

  def this(bean: BT) {
    this(new com.vaadin.data.util.BeanItem[BT](bean))
  }

  def this(bean: BT, propertyIds: Iterable[Any]) = {
    this(new com.vaadin.data.util.BeanItem[BT](bean, propertyIds.asJavaCollection))
  }

  def bean: BT = p.getBean
}