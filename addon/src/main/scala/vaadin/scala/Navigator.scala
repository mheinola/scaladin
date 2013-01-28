package vaadin.scala

import vaadin.scala.mixins.ScaladinMixin
import vaadin.scala.mixins.NavigationStateManagerMixin
import vaadin.scala.mixins.ViewDisplayMixin
import vaadin.scala.mixins.CssLayoutMixin
import vaadin.scala.mixins.EmptyViewMixin
import vaadin.scala.mixins.UriFragmentManagerMixin
import vaadin.scala.mixins.ComponentContainerViewDisplayMixin
import vaadin.scala.mixins.SingleComponentContainerViewDisplayMixin
import vaadin.scala.mixins.ViewProviderMixin
import vaadin.scala.mixins.StaticViewProviderMixin
import vaadin.scala.mixins.ClassBasedViewProviderMixin
import vaadin.scala.mixins.NavigatorMixin
import vaadin.scala.internal.UriFragmentChangedListener
import internal.WrapperUtil

package mixins {
  trait NavigationStateManagerMixin extends ScaladinMixin
  trait EmptyViewMixin extends CssLayoutMixin with ViewMixin { self: com.vaadin.navigator.Navigator.EmptyView => }
  trait UriFragmentManagerMixin extends NavigationStateManagerMixin
  trait ComponentContainerViewDisplayMixin extends ViewDisplayMixin { self: com.vaadin.navigator.Navigator.ComponentContainerViewDisplay => }
  trait SingleComponentContainerViewDisplayMixin extends ViewDisplayMixin { self: com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay => }
  trait StaticViewProviderMixin extends ViewProviderMixin
  trait ClassBasedViewProviderMixin extends ViewProviderMixin
  trait NavigatorMixin extends ScaladinMixin { self: com.vaadin.navigator.Navigator =>
    def wrapperNavigator: Navigator = wrapper.asInstanceOf[Navigator]
  }
}

object Navigator {

  case class EmptyView(override val p: com.vaadin.navigator.Navigator.EmptyView with EmptyViewMixin = new com.vaadin.navigator.Navigator.EmptyView with EmptyViewMixin) extends CssLayout(p) with View {
    override def enter(e: vaadin.scala.ViewChangeListener.ViewChangeEvent): Unit = {}
  }

  case class ComponentContainerViewDisplay(container: ComponentContainer) extends ViewDisplay {
    override val p: com.vaadin.navigator.Navigator.ComponentContainerViewDisplay with ComponentContainerViewDisplayMixin =
      new com.vaadin.navigator.Navigator.ComponentContainerViewDisplay(container.p) with ComponentContainerViewDisplayMixin

    def showView(v: View): Unit = v match {
      case c: Component => {
        container.removeAllComponents
        container.addComponent(c)
      }
      case c: com.vaadin.ui.Component => {
        container.removeAllComponents
        container.addComponent(c)
      }
      case _ => throw new IllegalArgumentException("View is not a component: " + v);
    }
  }

  case class SingleComponentContainerViewDisplay(container: SingleComponentContainer) extends ViewDisplay {
    override def p: com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay with SingleComponentContainerViewDisplayMixin =
      new com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay(container.p) with SingleComponentContainerViewDisplayMixin

    def showView(v: View): Unit = v match {
      case c: Component => {
        container.content_=(c)
      }
      case c: com.vaadin.ui.Component => container.content_=(c)
      case _ => throw new IllegalArgumentException("View is not a component: " + v);
    }
  }

  case class StaticViewProvider(viewName: String, view: View) extends ViewProvider {
    override def p: com.vaadin.navigator.Navigator.StaticViewProvider with StaticViewProviderMixin = new com.vaadin.navigator.Navigator.StaticViewProvider(viewName, view) with StaticViewProviderMixin
  }
  case class ClassBasedViewProvider[T <: com.vaadin.navigator.View](viewName: String, viewClass: Class[T]) extends ViewProvider {
    override def p: com.vaadin.navigator.Navigator.ClassBasedViewProvider with ClassBasedViewProviderMixin = new com.vaadin.navigator.Navigator.ClassBasedViewProvider(viewName, viewClass) with ClassBasedViewProviderMixin
  }

  def apply(ui: UI, container: ComponentContainer): Navigator = {
    new Navigator(ui, ComponentContainerViewDisplay(container))
  }
  def apply(ui: UI, container: SingleComponentContainer): Navigator = {
    new Navigator(ui, SingleComponentContainerViewDisplay(container))
  }
}

trait NavigationStateManager extends com.vaadin.navigator.NavigationStateManager {

  def navigator: Option[Navigator]
  def navigator_=(n: Navigator): Unit = navigator_=(Some(n))
  def navigator_=(n: Option[Navigator]): Unit

  override def getState(): String = state

  def state: String

}

case class ScaladinUriFragmentManager(val page: Page, override var navigator: Option[Navigator] = None)
  extends UriFragmentChangedListener(action = (event: Page.UriFragmentChangedEvent) => navigator.map(_.navigateTo("")))
  with NavigationStateManager {

  page.p.addUriFragmentChangedListener(this)

  override def setNavigator(navigator: com.vaadin.navigator.Navigator): Unit = {
    this.navigator = wrapperFor[Navigator](navigator)
  }

  def state: String = page.uriFragment.map(fragment => {
    if (fragment == null || !fragment.startsWith("!")) {
      "";
    } else {
      fragment.substring(1);
    }
  }).getOrElse("")

  override def setState(state: String): Unit = setFragment(fragment = "!" + state)

  override def uriFragmentChanged(e: com.vaadin.server.Page.UriFragmentChangedEvent): Unit = navigator.map(_.navigateTo(state))

  def uriFragmentChanged(e: vaadin.scala.Page.UriFragmentChangedEvent): Unit = navigator.map(_.navigateTo(state))

  protected def getFragment(): String = page.uriFragment.orNull

  protected def setFragment(fragment: String): Unit = page.setUriFragment(fragment, false);

}

/**
 * @see com.vaadin.navigator.Navigator
 * @author Matti Heinola / Viklo
 */
class Navigator(val ui: UI, val display: ViewDisplay) extends Wrapper {
  val stateManager: NavigationStateManager = ScaladinUriFragmentManager(ui.page)
  val p: com.vaadin.navigator.Navigator with NavigatorMixin = new com.vaadin.navigator.Navigator(ui.p, stateManager, display) with NavigatorMixin
  p.wrapper = this
  stateManager.navigator = this
  def navigateTo(navigationState: String): Unit = p.navigateTo(navigationState)

  def state = stateManager.state

  def addView(viewName: String, view: View): Unit = {
    p.addProvider(Navigator.StaticViewProvider(viewName, view))
  }

  def addView[T <: com.vaadin.navigator.View](viewName: String, viewClass: Class[T]): Unit = p.addProvider(Navigator.ClassBasedViewProvider(viewName, viewClass))
  def removeView(viewName: String): Unit = p.removeView(viewName)
  def addProvider(provider: ViewProvider): Unit = p.addProvider(provider.p.asInstanceOf[com.vaadin.navigator.ViewProvider])
  def removeProvider(provider: ViewProvider): Unit = p.removeProvider(provider.p.asInstanceOf[com.vaadin.navigator.ViewProvider])
  def setErrorView[T <: com.vaadin.navigator.View with View](viewClass: Class[T]): Unit = p.setErrorView(viewClass)
  def setErrorView(view: View): Unit = p.setErrorView(view)
  def addViewChangeListener(listener: ViewChangeListener): Unit = p.addViewChangeListener(listener)
  def removeViewChangeListener(listener: ViewChangeListener): Unit = p.removeViewChangeListener(listener)

}