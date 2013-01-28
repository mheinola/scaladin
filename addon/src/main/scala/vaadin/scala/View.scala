package vaadin.scala

import vaadin.scala.mixins.ScaladinMixin
import vaadin.scala.mixins.ComponentMixin
import vaadin.scala.mixins.ViewMixin
import vaadin.scala.mixins.ViewDisplayMixin
import vaadin.scala.mixins.ViewProviderMixin
import vaadin.scala.internal.Listener
import ViewImplicits._

package mixins {
  trait ViewMixin extends AbstractComponentMixin with com.vaadin.navigator.View { self: com.vaadin.navigator.View with com.vaadin.ui.AbstractComponent => }
  trait ViewDisplayMixin extends ScaladinMixin with com.vaadin.navigator.ViewDisplay
  trait ViewProviderMixin extends ScaladinMixin with com.vaadin.navigator.ViewProvider
}

object ViewImplicits {
  implicit def view2scaladinView(vv: com.vaadin.navigator.View): View = vv.asInstanceOf[View]
}

/**
 * @see com.vaadin.navigator.View
 * @author Matti Heinola / Viklo
 */
trait View extends AbstractComponent with com.vaadin.navigator.View {
  override def enter(e: com.vaadin.navigator.ViewChangeListener.ViewChangeEvent): Unit = {
    enter(ViewChangeListener.ViewChangeEvent(wrapperFor(e.getNavigator()).get, e.getOldView(), e.getNewView(), e.getViewName(), e.getParameters()))
  }
  def enter(e: vaadin.scala.ViewChangeListener.ViewChangeEvent): Unit
}

trait ViewDisplay extends Wrapper with com.vaadin.navigator.ViewDisplay {

  override def showView(v: com.vaadin.navigator.View): Unit = showView(v.asInstanceOf[View])

  def showView(v: View): Unit

}

trait ViewProvider extends Wrapper with com.vaadin.navigator.ViewProvider {

  override def getViewName(viewAndParameters: String): String = p match {
    case provider: com.vaadin.navigator.ViewProvider => provider.getViewName(viewAndParameters)
    case _ => ""
  }

  override def getView(viewName: String): View = p match {
    case provider: com.vaadin.navigator.ViewProvider => provider.getView(viewName).asInstanceOf[View]
    case _ => null
  }
}

object ViewChangeListener {
  case class ViewChangeEvent(navigator: Navigator, oldView: View, newView: View, viewName: String, parameters: String) extends com.vaadin.navigator.ViewChangeListener.ViewChangeEvent(navigator.p, oldView, newView, viewName, parameters) with Event
}

class ViewChangeListener(val action: ViewChangeListener.ViewChangeEvent => Boolean) extends com.vaadin.navigator.ViewChangeListener with Listener {
  def beforeViewChange(e: com.vaadin.navigator.ViewChangeListener.ViewChangeEvent) = action(ViewChangeListener.ViewChangeEvent(wrapperFor[Navigator](e.getNavigator()).get, e.getOldView(), e.getNewView(), e.getViewName(), e.getParameters()))
  def afterViewChange(e: com.vaadin.navigator.ViewChangeListener.ViewChangeEvent) = action(ViewChangeListener.ViewChangeEvent(wrapperFor[Navigator](e.getNavigator()).get, e.getOldView(), e.getNewView(), e.getViewName(), e.getParameters()))
}