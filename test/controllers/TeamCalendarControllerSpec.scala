package controllers

import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.TeamCalendarService

class TeamCalendarControllerSpec extends PlaySpec with Results with MockitoSugar with ScalaFutures {

  lazy val teamCalendarService = mock[TeamCalendarService]

  "Team Calendar Controller" should {
    "get preview of number of days of leaves between given two dates" in {

      val from = new DateTime()
      val to = from plusDays 5
      val requestBody: JsObject = Json.obj(
        "from" -> from.toString,
        "to" -> to.toString
      )

      when(teamCalendarService.getWorkingDaysBetween(any[DateTime], any[DateTime]))
        .thenReturn(List[DateTime](from, from plusDays 1, from plusDays 2))

      val request = FakeRequest("POST", "/api/leaves/preview")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      val leaveDaysJson = JsObject(Seq(
        "numberOfLeaveDays" -> JsString("3")
      ))

      val controller = new TeamCalendarController(teamCalendarService)

      val result = controller.getPreviewOfLeaveApplication()(request)
      whenReady(result) { r =>
        Json.parse(contentAsString(result)) mustBe leaveDaysJson
        r.header.status mustBe OK
      }
    }
  }
}


