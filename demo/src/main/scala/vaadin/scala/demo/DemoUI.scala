package vaadin.scala.demo

import vaadin.scala._

class DemoUI extends UI(title = "Hello World") {

  val layout = new VerticalLayout { sizeFull }

  val headerLayout = new VerticalLayout { width = 100 pct; height = 70 px }

  val contentLayout = new VerticalLayout { sizeFull; margin = true; }

  override def init(request: ScaladinRequest): Unit = {
    val navigator = Navigator(this, contentLayout)
    navigator.addView(DemoView1.NAME, new DemoView1);
    navigator.addView(DemoView2.NAME, new DemoView2);

    navigator_=(navigator)

    content_=(layout)

    headerLayout.add(buildApplicationHeader)
    headerLayout.add(buildApplicationMenu(navigator))

    layout.add(headerLayout)
    layout.add(contentLayout, ratio = 1)

  }

  private def buildApplicationHeader: HorizontalLayout = new HorizontalLayout {
    width = 100 pct;
    height = 45 px;
    add(alignment = Alignment.MiddleLeft, component = new Label { value = "Scaladin" })
    add(alignment = Alignment.MiddleCenter, component = new Label { value = "Demo Application" })
    add(alignment = Alignment.MiddleRight, component = new Label { value = "Hello World!" })
  }

  private def buildApplicationMenu(navigator: Navigator): HorizontalLayout = new HorizontalLayout {
    width = 100 pct;
    height = 25 px;
    val menuBar = new MenuBar {
      addItem("DemoView1", (e: MenuBar.MenuItem) => navigator.navigateTo(DemoView1.NAME))
      addItem("DemoView2", (e: MenuBar.MenuItem) => navigator.navigateTo(DemoView2.NAME))
    }
    addComponent(menuBar)
  }

}

object DemoView1 {
  val NAME = ""
}

class DemoView1 extends Navigator.PanelView {

  def init: Unit = {

    val layout = new VerticalLayout() {
      sizeFull
      add(Label("Hello from DemoView1"))
      add(Label("I'm vertical"))
    }
    layout.margin = true
    content = layout
  }
  init

  override def enter(event: Navigator.ViewChangeEvent): Unit = {

  }
}

object DemoView2 {
  val NAME = "demo2"
}

class DemoView2 extends Navigator.PanelView {

  def init: Unit = {

    val layout = new HorizontalLayout() {
      sizeFull
      add(Label("Hello from DemoView2"))
      add(Label("I'm horizontal"))
    }
    layout.margin = true
    content = layout
  }
  init

  override def enter(event: Navigator.ViewChangeEvent): Unit = {
    Notification.show("Entering DemoView2");
  }
}