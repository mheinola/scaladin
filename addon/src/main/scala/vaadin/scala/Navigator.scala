package vaadin.scala

import vaadin.scala.mixins.ViewMixin
import vaadin.scala.mixins.EmptyViewMixin
import vaadin.scala.mixins.ComponentContainerViewDisplayMixin
import vaadin.scala.mixins.SingleComponentContainerViewDisplayMixin
import vaadin.scala.mixins.NavigatorMixin
import vaadin.scala.mixins.PanelMixin
import internal._
import scala._
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.Some

package mixins {

  trait ViewMixin extends ScaladinMixin { self: com.vaadin.navigator.View =>
    override def enter(e: com.vaadin.navigator.ViewChangeListener.ViewChangeEvent): Unit = {
      // FIXME asInstanceOf
      wrapper.asInstanceOf[Navigator.View].enter(Navigator.ViewChangeEvent(
        WrapperUtil.wrapperFor(e.getNavigator()).get,
        WrapperUtil.wrapperFor(e.getOldView()).getOrElse(null),
        WrapperUtil.wrapperFor(e.getNewView()).get,
        e.getViewName(),
        e.getParameters()))
    }
  }
  trait ViewDisplayMixin extends ScaladinMixin with com.vaadin.navigator.ViewDisplay
  trait NavigationStateManagerMixin extends ScaladinMixin with com.vaadin.navigator.NavigationStateManager

  trait EmptyViewMixin extends CssLayoutMixin with ViewMixin { self: com.vaadin.navigator.Navigator.EmptyView => }
  trait UriFragmentManagerMixin extends NavigationStateManagerMixin { self: com.vaadin.navigator.Navigator.UriFragmentManager => }
  trait ComponentContainerViewDisplayMixin extends ViewDisplayMixin { self: com.vaadin.navigator.Navigator.ComponentContainerViewDisplay => }
  trait SingleComponentContainerViewDisplayMixin extends ViewDisplayMixin { self: com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay => }

  trait NavigatorMixin extends ScaladinMixin { self: com.vaadin.navigator.Navigator =>
    override def fireBeforeViewChange(event: com.vaadin.navigator.ViewChangeListener.ViewChangeEvent): Boolean = {
      !wrapper.asInstanceOf[Navigator].beforeViewChangeListeners.listenerSet.exists(listener => !listener.beforeViewChange(event))
    }

    override def fireAfterViewChange(event: com.vaadin.navigator.ViewChangeListener.ViewChangeEvent): Unit = {
      wrapper.asInstanceOf[Navigator].afterViewChangeListeners.listenerSet.foreach(listener => listener.afterViewChange(event))
    }
  }
}

object Navigator {

  trait View extends Wrapper {
    val p: com.vaadin.navigator.View with ViewMixin = new com.vaadin.navigator.View with ViewMixin
    val pView: com.vaadin.navigator.View with ViewMixin = p
    def enter(e: Navigator.ViewChangeEvent): Unit = pView.enter(new com.vaadin.navigator.ViewChangeListener.ViewChangeEvent(e.navigator.p, e.oldView.pView, e.newView.pView, e.viewName, e.parameters))
  }

  case class ViewChangeEvent(navigator: Navigator, oldView: Navigator.View, newView: Navigator.View, viewName: String, parameters: String) extends Event

  trait ViewDisplay extends Wrapper { viewDisplay =>
    def p: com.vaadin.navigator.ViewDisplay

    def showView(v: Navigator.View): Unit
  }

  class EmptyView(override val p: com.vaadin.navigator.Navigator.EmptyView with EmptyViewMixin with com.vaadin.navigator.View with ViewMixin = new com.vaadin.navigator.Navigator.EmptyView with EmptyViewMixin with com.vaadin.navigator.View with ViewMixin) extends CssLayout with View { p.wrapper = this }

  // TODO: How to wrap the View interface from Vaadin to support any Layout?
  class PanelView(override val p: com.vaadin.ui.Panel with PanelMixin with com.vaadin.navigator.View with ViewMixin = new com.vaadin.ui.Panel with PanelMixin with com.vaadin.navigator.View with ViewMixin) extends Panel with View { p.wrapper = this }

  class ComponentContainerViewDisplay(container: ComponentContainer) extends ViewDisplay {
    override val p: com.vaadin.navigator.Navigator.ComponentContainerViewDisplay with ComponentContainerViewDisplayMixin = new com.vaadin.navigator.Navigator.ComponentContainerViewDisplay(container.p) with ComponentContainerViewDisplayMixin

    override def showView(v: Navigator.View): Unit = v match {
      case c: Component => {
        container.removeAllComponents
        container.addComponent(c)
      }
      case _ => p.showView(v.pView)
    }
  }

  class SingleComponentContainerViewDisplay(container: SingleComponentContainer) extends ViewDisplay {
    override def p: com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay with SingleComponentContainerViewDisplayMixin = new com.vaadin.navigator.Navigator.SingleComponentContainerViewDisplay(container.p) with SingleComponentContainerViewDisplayMixin

    override def showView(v: Navigator.View): Unit = v match {
      case c: Component => container.content_=(c)
      case _ => p.showView(v.pView)
    }
  }

  /**
   * ViewProviders implement directly the Vaadin interface.
   */
  trait ViewProvider extends com.vaadin.navigator.ViewProvider {
    override def getViewName(viewAndParameters: String): String = viewName(viewAndParameters).getOrElse(null)
    override def getView(viewName: String): com.vaadin.navigator.View = view(viewName).map(_.pView).getOrElse(null)

    def viewName: String
    def viewInstance: Navigator.View
    def viewName(viewAndParameters: String): Option[String] = {
      if (viewAndParameters != null && (viewAndParameters == viewName || viewAndParameters.startsWith(viewName + "/"))) {
        Some(viewName)
      } else {
        None
      }
    }
    def view(viewName: String): Option[Navigator.View] = if (viewName == name) {
      Some(viewInstance)
    } else {
      None
    }
  }

  class StaticViewProvider(override val viewName: String, override val viewInstance: Navigator.View) extends ViewProvider

  class ClassBasedViewProvider(override val viewName: String, viewClass: Class[_ <: Navigator.View]) extends ViewProvider {
    override def viewInstance: Navigator.View = {
      try { viewClass.newInstance() } catch {
          case e: InstantiationException => throw new RuntimeException(e)
          case e: IllegalAccessException => throw new RuntimeException(e)
      }
    }
  }

  /**
   * State manager implements directly the Vaadin interface.
   */
  trait NavigationStateManager extends com.vaadin.navigator.NavigationStateManager {

    def navigator: Option[Navigator]
    def navigator_=(n: Navigator): Unit = navigator_=(Some(n))
    def navigator_=(n: Option[Navigator]): Unit

    override def getState(): String = state

    def state: String

  }

  class UriFragmentManager(val page: Page, override var navigator: Option[Navigator] = None)
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

    def uriFragmentChanged(e: Page.UriFragmentChangedEvent): Unit = navigator.map(_.navigateTo(state))

    protected def getFragment(): String = page.uriFragment.orNull

    protected def setFragment(fragment: String): Unit = {
      page.setUriFragment(fragment, false)
    }

  }

  def apply(ui: UI, container: ComponentContainer): Navigator = {
    new Navigator(ui, new ComponentContainerViewDisplay(container))
  }
  def apply(ui: UI, container: SingleComponentContainer): Navigator = {
    new Navigator(ui, new SingleComponentContainerViewDisplay(container))
  }
}

/**
 * @see com.vaadin.navigator.Navigator
 * @author Matti Heinola / Viklo
 */
class Navigator(val ui: UI, val display: Navigator.ViewDisplay) extends Wrapper { navigator =>

  val stateManager: Navigator.NavigationStateManager = new Navigator.UriFragmentManager(ui.page)
  val p: com.vaadin.navigator.Navigator with NavigatorMixin = new com.vaadin.navigator.Navigator(ui.p, stateManager, display.p) with NavigatorMixin
  p.wrapper = this
  stateManager.navigator = this

  def navigateTo(navigationState: String): Unit = {
    p.navigateTo(navigationState)
  }

  def state = stateManager.state

  def addView(viewName: String, view: Navigator.View): Unit = addViewProvider(viewName, new Navigator.StaticViewProvider(viewName, view))
  def addView(viewName: String, viewClass: Class[_ <: Navigator.View]): Unit = addViewProvider(viewName, new Navigator.ClassBasedViewProvider(viewName, viewClass))
  private def addViewProvider(viewName: String, provider: Navigator.ViewProvider): Unit = {
    removeView(viewName)
    addProvider(provider)
  }
  def removeView(viewName: String): Unit = p.removeView(viewName)
  def addProvider(provider: Navigator.ViewProvider): Unit = p.addProvider(provider)
  def removeProvider(provider: Navigator.ViewProvider): Unit = p.removeProvider(provider)

  // TODO: def setErrorView(viewClass: Class[_ <: Navigator.View]): Unit = {
  //  p.setErrorView(viewClass)
  //}
  def setErrorView(view: Navigator.View): Unit = p.setErrorView(view.pView)

  lazy val beforeViewChangeListeners = new DecisionListenersTrait[Navigator.ViewChangeEvent, ViewChangeListener] {
    val listenerSet: mutable.Set[ViewChangeListener] = mutable.HashSet()
    override def listeners: java.util.Collection[_] = listenerSet
    override def addListener(elem: Navigator.ViewChangeEvent => Boolean) = listenerSet.add(new BeforeViewChangeListener(elem)) //p.addViewChangeListener(new BeforeViewChangeListener(elem))
    override def removeListener(elem: ViewChangeListener) = listenerSet.remove(elem) // p.removeViewChangeListener(elem)
  }

  lazy val afterViewChangeListeners = new ListenersTrait[Navigator.ViewChangeEvent, ViewChangeListener] {
    val listenerSet: mutable.Set[ViewChangeListener] = mutable.HashSet()
    override def listeners: java.util.Collection[_] = listenerSet
    override def addListener(elem: Navigator.ViewChangeEvent => Unit) = listenerSet.add(new AfterViewChangeListener(elem)) //p.addViewChangeListener(new AfterViewChangeListener(elem))
    override def removeListener(elem: ViewChangeListener) = listenerSet.remove(elem) // p.removeViewChangeListener(elem)
  }

}