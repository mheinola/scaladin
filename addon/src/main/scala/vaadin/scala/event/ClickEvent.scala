package vaadin.scala.event

import vaadin.scala.{ MouseButton, Component }

case class ClickEvent(
  component: Component,
  button: MouseButton.Value,
  clientX: Int,
  clientY: Int,
  relativeX: Int,
  relativeY: Int,
  doubleClick: Boolean,
  altKey: Boolean,
  ctrlKey: Boolean,
  metaKey: Boolean,
  shiftKey: Boolean)
    extends AbstractClickEvent(component,
      button,
      clientX,
      clientY,
      relativeX,
      relativeY,
      doubleClick,
      altKey,
      ctrlKey,
      metaKey,
      shiftKey)