package command

import org.joda.time.DateTime

sealed trait EmployeeCommand extends Command

case class RegisterEmployee(email: String, givenName: String) extends EmployeeCommand
case class CreditLeaves(creditedLeaves: Float) extends EmployeeCommand
case class ApplyFullDayLeaves(from: DateTime, to: DateTime) extends EmployeeCommand
case class ApplyHalfDayLeaves(from: DateTime, to: DateTime) extends EmployeeCommand
case class GetLeaveBalance() extends EmployeeCommand
case class GetLeaveSummary() extends EmployeeCommand
