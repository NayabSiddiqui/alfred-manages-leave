package command

import org.joda.time.DateTime

sealed trait EmployeeCommand extends Command

case class RegisterEmployee(email: String, givenName: String) extends EmployeeCommand

case class CreditLeaves(creditedLeaves: Float) extends EmployeeCommand

case class ApplyFullDayLeaves(days: List[DateTime]) extends EmployeeCommand

case class ApplyHalfDayLeaves(days: List[DateTime]) extends EmployeeCommand

case class CancelLeaveApplication(applicationId: String) extends EmployeeCommand

case class GetLeaveBalance() extends EmployeeCommand

case class GetLeaveSummary() extends EmployeeCommand
