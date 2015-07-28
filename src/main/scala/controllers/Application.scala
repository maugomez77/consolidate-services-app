package controllers

import _root_.util.{Constants, ParserJson, Activity}
import play.api.Logger
import play.api.libs.json._
import play.api.libs.ws.{WS, WSCookie, WSRequest, WSResponse}
import play.api.mvc._

import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Application extends Controller with Constants {

  //application/json   + rawString

  def parserJson: ParserJson = new ParserJson

  def listDocuments(client_app_uuid: String, access_token: String,
                    user_email: String, esign_document_reference_uuid: Option[String],
                    documentTypeName: Option[String]) = Action.async { request =>

    request.cookies.get(COOKIE_NAME).map { valuesSession => {
      val activityData = parserJson.parserJson(ACTIVITY_DOCUMENT)
      executeActivity(activityData, valuesSession.value,
                      RequiredParams(List(client_app_uuid,
                                          access_token,
                                          user_email)),
                      OptionalParams(List(esign_document_reference_uuid, documentTypeName)))//,
    }
    }.getOrElse {
      Future.successful(Unauthorized(INVALID_CREDENTIALS_MSG))
    }
  }

  def listDashboards = Action.async { request =>
    request.cookies.get(COOKIE_NAME).map { valuesSession => {
      val activityData = parserJson.parserJson(ACTIVITY_DASHBOARD)
      executeActivity(activityData, valuesSession.value, RequiredParams(List()), OptionalParams(List()))
    }
    }.getOrElse {
      Future.successful(Unauthorized(INVALID_CREDENTIALS_MSG))
    }
  }

  def listLegal(questionId: String, assignee: String) = Action.async { request =>

    val rlSessionId: String = request.cookies.get(COOKIE_NAME).map(_.value).getOrElse("")
    val activityData = parserJson.parserJson(ACTIVITY_LEGALS)
    executeActivity(activityData, rlSessionId, RequiredParams(List(questionId, assignee)), OptionalParams(List()))
  }

  /**
    * module for internal testing
    *
    * @param  activity
    * @return Action.async
    */
  def listActivities(activity: String) = Action.async { request =>
    val activityData: Option[Activity] = parserJson.parserJson(activity)
    executeActivity(activityData, "", RequiredParams(List()), OptionalParams(List()))
  }

  def prepareRequest(activity: Activity,
                     indexEndPoint: Int,
                     rlSessionId: String,
                     timeout: Long,
                     requiredParams: RequiredParams,
                     optionalParams: OptionalParams): WSRequest = {

    val resourceURL = (activity.activityName, indexEndPoint) match {
      case ("documents", 1) => {

        val reqParams =
          activity.details(indexEndPoint).endPoint.replaceAll("client_app_uuid=", "client_app_uuid=" + requiredParams.params(0)) +
          activity.details(indexEndPoint).endPoint.replaceAll("access_token=", "access_token=" + requiredParams.params(1)) +
          activity.details(indexEndPoint).endPoint.replaceAll("user_email=", "user_email=" + requiredParams.params(2))

        val optParams =
          optionalParams.params(0).map { "esign_document_reference_uuid=" + _.toString }.getOrElse("") +
          optionalParams.params(1).map { "documentTypeName=" + _.toString }.getOrElse("")

        reqParams + optParams
      }

      case ("legals", 1) => {
        activity.details(indexEndPoint).endPoint.replaceAll("requestId", requiredParams.params(0))
      }

      case ("legals", 2) => {
        activity.details(indexEndPoint).endPoint.replaceAll("requestId", requiredParams.params(0)) +
        activity.details(indexEndPoint).endPoint.replaceAll("?assignee", "?assignee=" + requiredParams.params(1))
      }

      case _ => {
        activity.details(indexEndPoint).endPoint
      }
    }

    val urlEndPoint = activity.baseURL + resourceURL
    WS.url(urlEndPoint)
      .withHeaders(("Cookie", COOKIE_NAME + "=" + rlSessionId))
      .withRequestTimeout(timeout)
  }

  def executeActivity(activityData: Option[Activity],
                      rlSessionId: String = "",
                      requiredParams: RequiredParams,
                      optionalParams: OptionalParams): Future[Result] = {

    activityData.map { activity =>

      val totalEndPoints = activity.numberEndPoints.toInt

      val futures: IndexedSeq[Future[WSResponse]] = for {
        indexEndPoint <- 0 until totalEndPoints
      } yield {
          val f = withInstrumentation(
            prepareRequest(activity, indexEndPoint, rlSessionId, activity.timeOut.toLong,
                           requiredParams, optionalParams).get(),
                           new ResponseFailureMessage(activity.details(indexEndPoint).endPoint))
          //needs to be executed
          f
        }

      val allWork: IndexedSeq[Future[String]] = for {
        indexEndPoint <- 0 until totalEndPoints
      } yield {
          for {
            future <- futures(indexEndPoint)
          } yield future.allHeaders.get("Content-Type").flatMap(_.headOption).getOrElse("") match {
            case "application/json" => Json.stringify(future.json)
            case _ => future.body
          }
        }

      val allFutures = Future.sequence(allWork).map { ele =>
        ele.mkString("")
      }

      allFutures.map {
        Ok(_).as("text/html")
      }

    }.getOrElse {
      Future.successful(Ok(NO_ACTIVITY_SPECIFIED))
    }
  }

  // Handle Exceptions in Futures by logging them and returning a fallback value
  def withErrorHandling[T](f: Future[T], fallback: T): Future[T] = {
    f.recover {
      case t: Throwable => {
        Logger.error(ERROR_MESSAGE + t.getMessage, t)
        fallback
      }
    }
  }

  // Log how long a Future took to process
  def withTiming[T](f: => Future[T]): Future[T] = {
    val startTime = System.currentTimeMillis()
    Logger.debug(FUTURE_ENTERING.format(f, new java.sql.Timestamp(startTime)))
    f.map {
      case r => {
        val endTime = System.currentTimeMillis()
        val latency = endTime - startTime
        Logger.debug(FUTURE_TOOK_TIME.format(f, latency))
        Logger.debug(FUTURE_EXITING.format(f, new java.sql.Timestamp(endTime)))
        r
      }
    }
  }

  // A single wrapper method for all the instrumentation functions
  def withInstrumentation[T](f: => Future[T], fallback: T): Future[T] = {
    withErrorHandling(
      withTiming(f),
      fallback
    )
  }

}

case class RequiredParams(params: List[String])
case class OptionalParams(params: List[Option[String]])

class ResponseFailureMessage(endPoint: String) extends WSResponse {

  override def statusText: String = "There was a failure with this endpoint " + endPoint

  override def underlying[T]: T = ???

  import scala.xml.Elem
  override def xml: Elem = ???

  override def body: String = "There was a failure with this endpoint " + endPoint

  override def header(key: String): Option[String] = ???

  override def cookie(name: String): Option[WSCookie] = ???

  override def bodyAsBytes: Array[Byte] = ???

  override def cookies: Seq[WSCookie] = ???

  override def status: Int = 500

  override def json: JsValue = Json.parse("{ There was an error in this end point " + endPoint)

  override def allHeaders: Map[String, Seq[String]] = ???
}