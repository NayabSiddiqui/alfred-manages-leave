package event

import org.joda.time.DateTime

sealed trait EmployeeEvent extends Event

case class EmployeeRegistered(email: String, firstName: String, lastName: String) extends EmployeeEvent {
  override val sourceId: String = email
}
case class LeavesCredited(email: String, creditedLeaves: Float) extends EmployeeEvent{
  override val sourceId: String = email
}

case class FullDayLeavesApplied(email: String, from: DateTime, to: DateTime) extends EmployeeEvent {
  override val sourceId: String = email
}

case class HalfDayLeavesApplied(email: String, from: DateTime, to: DateTime) extends EmployeeEvent {
  override val sourceId: String = email
}
