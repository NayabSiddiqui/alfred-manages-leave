package domain

import akka.http.scaladsl.model.DateTime
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import play.api.libs.json._

case class LeaveSummary(leaveApplications: List[LeaveApplication], balance: Float)

object LeaveSummary {

  implicit val yourJodaDateWrites = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val leaveApplicationWrites = new Writes[LeaveApplication] {
    def writes(application: LeaveApplication) = Json.obj(
      "id" -> application.id,
      "days" -> application.days,
      "halfDayLeaves" -> application.halfDayLeaves
    )
  }

  implicit val leaveSummaryWrites = new Writes[LeaveSummary] {
    def writes(leaveSummary: LeaveSummary) = Json.obj(
      "leaveApplications" -> leaveSummary.leaveApplications,
      "balance" -> leaveSummary.balance.toString
    )
  }

}


