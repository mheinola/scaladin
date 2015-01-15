package vaadin.scala.renderer

import com.vaadin.ui.renderer.{ ButtonRenderer => VaadinButtonRenderer }
import vaadin.scala.renderer.mixins.ButtonRendererMixin

package mixins {
  trait ButtonRendererMixin extends ClickableRendererMixin
}

object ButtonRenderer {

  def apply(): ButtonRenderer = new ButtonRenderer

  def apply(clickListener: ClickableRenderer.RendererClickEvent => Unit): ButtonRenderer =
    new ButtonRenderer {
      clickListeners += clickListener
    }
}

/**
 * @see com.vaadin.ui.renderer.ButtonRenderer
 * @author Henri Kerola / Vaadin
 */
class ButtonRenderer(override val p: VaadinButtonRenderer with ButtonRendererMixin = new VaadinButtonRenderer with ButtonRendererMixin)
  extends ClickableRenderer[String](p)
