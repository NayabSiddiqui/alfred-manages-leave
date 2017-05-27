package event

import org.joda.time.DateTime

sealed trait EmployeeEvent extends Event

case class EmployeeRegistered(firstName: String, lastName: String) extends EmployeeEvent

case class LeavesCredited(creditedLeaves: Float) extends EmployeeEvent

case class LeavesApplied(from: DateTime, to: DateTime, isHalfDay: Boolean) extends EmployeeEvent


