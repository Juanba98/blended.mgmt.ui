package blended.mgmt.app.test

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.scalatest.selenium.WebBrowser
import org.scalatest.{BeforeAndAfterAll, FreeSpecLike}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class SampleSpec extends TestKit(ActorSystem("uitest"))
  with FreeSpecLike
  with WebBrowser
  with BeforeAndAfterAll {

  implicit val actorSystem : ActorSystem = system
  implicit val materializer : ActorMaterializer = ActorMaterializer()
  implicit val executionContext : ExecutionContext = system.dispatcher

  private[this] lazy val svrBinding : ServerBinding = {
    val binding = Http().bindAndHandle(route, interface = "localhost", port=0)
    Await.result(binding, 10.seconds)
  }

  val chromeOptions = new ChromeOptions()
  chromeOptions.addArguments("--headless", "--disable-gpu")

  implicit val driver : WebDriver = new ChromeDriver(chromeOptions)

  val route : Route = getFromBrowseableDirectory(System.getProperty("appUnderTest"))

  override protected def beforeAll(): Unit = port()

  override protected def afterAll(): Unit = svrBinding.unbind().flatMap(_ => system.terminate())

  private[this] def port() : Int = svrBinding.localAddress.getPort

  "The Mgmt App should" - {

    "show up" in {
      val url = s"http://localhost:${port()}/index-dev.html"

      go.to(url)
      assert(pageTitle == "Blended Management Console")
      if (isScreenshotSupported) {
        setCaptureDir("target")
        capture.to("screen.png")
      }
      close()
    }
  }
}