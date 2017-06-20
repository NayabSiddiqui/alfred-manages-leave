package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller}
import service.TeamCalendarService

case class TeamCalendarController @Inject()(private val teamCalendarService: TeamCalendarService) extends Controller {

  def getPreviewOfLeaveApplication(): Action[JsValue] = Action(parse.json) { implicit request =>
    val from = DateTime.parse((request.body \ "from").as[String])
    val to = DateTime.parse((request.body \ "to").as[String])

    val numberOfLeaveDays = teamCalendarService.getWorkingDaysBetween(from, to).length
    Ok(numberOfLeaveDays.toString)
  }

}
