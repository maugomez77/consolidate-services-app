package util

import play.api.Play
import play.api.Play.current
import play.api.libs.json.Json

/**
 * Created by mgomez on 7/14/15.
 */
class ParserJson {

  //def endPointFile = Play.resource("endpoints.json").get
  //def lines = scala.io.Source.fromURL(endPointFile).getLines.mkString

  def lines = scala.io.Source.fromFile("/opt/resources/play.properties").getLines().mkString

  implicit val detailsFormat = Json.format[Details]
  implicit val activityFormat = Json.format[Activity]
  implicit val endPointsFormat = Json.format[EndPoints]

  implicit val endpointsReads = Json.reads[EndPoints]
  implicit val endpointsWrites = Json.writes[EndPoints]


  def parserJson(activityNameValue: String): Option[Activity] = {
    val json = Json.parse(lines)
    val data: EndPoints = json.as[EndPoints]
    val activityData: Option[Activity] = data.operations.toList.filter(_.activityName == activityNameValue).headOption
    activityData
  }
}

case class Details(endPoint: String)

case class Activity(activityName: String, numberEndPoints: String, baseURL: String, timeOut: String, details: Seq[Details])

case class EndPoints(operations: Seq[Activity])
