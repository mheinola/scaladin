package vaadin.scala.tests

import org.scalatest.FunSuite
import vaadin.scala._
import com.vaadin.server.{ Page => VaadinPage }
import internal.WrappedVaadinUI
import org.scalatest.BeforeAndAfter
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.mockito.Mockito

@RunWith(classOf[JUnitRunner])
class NavigatorTests extends ScaladinTestSuite {

  var scaladinPage: Page = _
  var vaadinPage: VaadinPage = _
  val ui = new UI {
    override def page() = scaladinPage
  }
  val contentLayout = new VerticalLayout {}
  val testView2 = new TestView2
  var navigator: Navigator = _

  class TestView1 extends Navigator.PanelView {
    content = Label("Hello from DemoView1")
  }

  class TestView2 extends TestView1 {
    content = Label("Hello from DemoView2")

    var nr: Option[Int] = None

    override def enter(event: Navigator.ViewChangeEvent): Unit = {
      nr = Some(1)
    }
  }

  before {
    vaadinPage = mock[VaadinPage]
    scaladinPage = new Page {
      override val p = vaadinPage
    }
    Mockito.when(vaadinPage.getUriFragment()).thenReturn("")
    navigator = Navigator(ui, contentLayout)
    navigator.addView("", new TestView1);
    navigator.addView("TestView2", testView2);
  }

  test("Navigator construct") {

    assert(navigator.stateManager.isInstanceOf[Navigator.UriFragmentManager])

  }

  test("Navigator navigateTo") {
    assert(navigator.stateManager.state === "")

    Mockito.when(vaadinPage.getUriFragment()).thenReturn("!TestView2")
    navigator.navigateTo("TestView2")
    assert(navigator.stateManager.state === "TestView2")
    assert(testView2.nr === Some(1))

  }

}