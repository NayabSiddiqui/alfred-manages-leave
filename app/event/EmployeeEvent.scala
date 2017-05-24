package event

import org.joda.time.DateTime
import play.api.libs.json.Json
import spray.json.DefaultJsonProtocol

sealed trait EmployeeEvent extends Event

case class EmployeeRegistered(firstName: String, lastName: String) extends EmployeeEvent

case class LeavesCredited(creditedLeaves: Float) extends EmployeeEvent

case class FullDayLeavesApplied(from: DateTime, to: DateTime) extends EmployeeEvent

case class HalfDayLeavesApplied(from: DateTime, to: DateTime) extends EmployeeEvent

object EmployeeRegistered {
  implicit val employeeRegisteredFormat = Json.format[EmployeeRegistered]
}

object LeavesCredited {
  implicit val leavesCreditedFormat = Json.format[LeavesCredited]
}

object FullDayLeavesApplied {
  implicit val fullDayLeavesApplied = Json.format[FullDayLeavesApplied]
}

object HalfDayLeavesApplied {
  implicit val halfDayLeavesAppliedFormat = Json.format[HalfDayLeavesApplied]
}