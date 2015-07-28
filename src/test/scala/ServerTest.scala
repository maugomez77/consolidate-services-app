import controllers.Application._
import org.specs2.mutable._
import play.api.libs.ws.{WSAuthScheme, WS}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._

import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

class ServerTest extends Specification {

  val portNumber: Int = 9000
  val domainName: String = "localhost"
  val baseURL: String = "http://" + domainName + ":" + portNumber

  val stageURL: String = "http://rllastestweb001:8080"

  "Server starting " should {

    /*
    "Checking for test for hitting www.google.com " in {

      running(TestServer(portNumber)) {

        val responseFuture = WS.url(baseURL + "/activities/tests").get()
        val resultFuture = responseFuture map { response =>
          response.status match {
            case 200 => Some(response.body)
            case _ => None
          }
        }

        val result = Await.result(resultFuture, 5 seconds)

        result.isDefined must equalTo(true)
        result.get.contains("Search the world") must equalTo(true)
      }
    }

    "Checking for test for unknow activity " in {

      running(TestServer(portNumber)) {

        val responseFuture = WS.url(baseURL + "/activities/no_activities").get()
        val resultFuture = responseFuture map { response =>
          response.status match {
            case 200 => Some(response.body)
            case _ => None
          }
        }

        val result = Await.result(resultFuture, 5 seconds)
        result.isDefined must equalTo(true)
        result.get.contains("No activity specified") must equalTo(true)
      }
    }

    "render the activities for tests " in {
      running(FakeApplication()) {
        val activities = route(FakeRequest(GET, "/activities/tests")).get
        //println("tt " + contentType(home))
        //println("ttt " + contentAsString(home))
        status(activities) must equalTo(OK)
        contentType(activities) must beSome.which(_ == "text/html")
        contentAsString(activities) must contain("html")
      }
    }

    */

    "Checking for test for listDocuments with no cookies specified" in {

      running(TestServer(portNumber)) {

        val responseFuture = WS.url(baseURL + "/documents").get()
        val resultFuture = responseFuture map { response =>
          response.status match {
            case 400 => Some(response.body)
            case _ => None
          }
        }
        val result = Await.result(resultFuture, 5 seconds)
        result.isDefined must equalTo(true)
        //result.get.contains("No activity specified") must equalTo(true)
      }
    }

    "Checking for test for listDocuments with cookies specified an user specified" in {

      running(TestServer(portNumber)) {

        val urlString: String = baseURL + "/documents?client_app_uuid=client_app_uuid&access_token=access_token&user_email=user_email"

        val responseFuture = WS.url(urlString)
          .withHeaders(("Cookie", COOKIE_NAME + "=" + "123123"))
          .get()
        val resultFuture = responseFuture map { response =>
          response.status match {
            case 200 => Some(response.body)
            case _ => None
          }
        }
        val result = Await.result(resultFuture, 5 seconds)
        result.isDefined must equalTo(true)
        result.get.contains("user not authenticated") must equalTo(true)
      }
    }

    /*
    "Checking for test for listDocuments with cookies specified and user log in" in {

      running(TestServer(portNumber)) {

        val firstLogin: String = stageURL + "/authentication-service/identity/v1/auth/login?username=mauricio.gomez.77@gmail.com&password=Rose743525"

        val responseFuture1 = WS.url(firstLogin)
          .withAuth("mauricio.gomez.77@gmail.com", "Rose743525", WSAuthScheme.BASIC)
          .get()


        val resultFuture1 = responseFuture1 map { response =>
          response.status match {
            case 200 => response.cookie(COOKIE_NAME).get.value
            case _ => Some(response.body.toString)
          }
        }

        val result = Await.result(resultFuture1, 5 seconds)
        result.isDefined must equalTo(true)


        //val urlString: String = stageURL + "/documents?client_app_uuid=client_app_uuid&access_token=access_token&user_email=user_email"

        val urlString: String = stageURL + "/dashboards"

        val responseFuture2 = WS.url(urlString)
          .withHeaders(("Cookie", COOKIE_NAME + "=" + result))
          .get()

        val resultFuture2 = responseFuture2 map { response =>
          response.status match {
            case 200 => Some(response.body)
            case _ => Some(response.body)
          }
        }
        val result2 = Await.result(resultFuture2, 5 seconds)
        println (result2.get.contents.toString)

        result2.isDefined must equalTo(true)
        result2.get.contains("user not authenticated") must equalTo(true)

      }
    }*/
  }
}
