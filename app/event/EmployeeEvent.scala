package event

import org.joda.time.DateTime

sealed trait EmployeeEvent extends Event

case class EmployeeRegistered(firstName: String, lastName: String) extends EmployeeEvent
case class LeavesCredited(creditedLeaves: Float) extends EmployeeEvent
case class FullDayLeavesApplied(from: DateTime, to: DateTime) extends EmployeeEvent
case class HalfDayLeavesApplied(from: DateTime, to: DateTime) extends EmployeeEvent
