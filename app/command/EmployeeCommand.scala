package command

import org.joda.time.DateTime

sealed trait EmployeeCommand extends Command

case class RegisterEmployee(email: String, firstName: String, lastName: String) extends EmployeeCommand
case class CreditLeaves(email: String, creditedLeaves: Float) extends EmployeeCommand
case class ApplyFullDayLeaves(email: String, from: DateTime, to: DateTime) extends EmployeeCommand
case class ApplyHalfDayLeaves(email: String, from: DateTime, to: DateTime) extends EmployeeCommand
