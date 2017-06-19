package event

import org.joda.time.DateTime

sealed trait EmployeeEvent extends Event

case class EmployeeRegistered(email: String, givenName: String) extends EmployeeEvent

case class LeavesCredited(creditedLeaves: Float) extends EmployeeEvent

case class LeavesApplied(applicationId: String, days: List[DateTime], isHalfDay: Boolean) extends EmployeeEvent

case class LeaveCancelled(applicationId: String) extends EmployeeEvent


