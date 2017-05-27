package event

import org.joda.time.DateTime
import play.api.libs.json.Json

sealed trait EmployeeEvent extends Event

case class EmployeeRegistered(firstName: String, lastName: String) extends EmployeeEvent

case class LeavesCredited(creditedLeaves: Float) extends EmployeeEvent

case class LeavesApplied(from: DateTime, to: DateTime, isHalfDay: Boolean) extends EmployeeEvent


object EmployeeRegistered {
  implicit val employeeRegisteredFormat = Json.format[EmployeeRegistered]
}

object LeavesCredited {
  implicit val leavesCreditedFormat = Json.format[LeavesCredited]
}

object LeavesApplied {
  implicit val fullDayLeavesApplied = Json.format[LeavesApplied]
}



