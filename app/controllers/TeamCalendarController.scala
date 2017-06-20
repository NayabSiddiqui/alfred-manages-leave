package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc.{Action, Controller}
import service.TeamCalendarService

case class TeamCalendarController @Inject()(private val teamCalendarService: TeamCalendarService) extends Controller {

  def getPreviewOfLeaveApplication() = Action(parse.json) { implicit request =>
    val from = DateTime.parse((request.body \ "from").as[String])
    val to = DateTime.parse((request.body \ "to").as[String])

    val numberOfLeaveDays = teamCalendarService.getWorkingDaysBetween(from, to).length
    Ok(JsObject(Seq{
      "numberOfLeaveDays" -> JsString(numberOfLeaveDays.toString)
    }))
  }

}
