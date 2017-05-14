package domain

import command._
import event._
import org.joda.time.{DateTime, Days}

case class Employee private(email: String, firstName: String, lastName: String, leaveBalance: Float) extends AggregateRoot {

  def this(email: String) = {
    this(email, null, null, 0f)
  }

  override val id = email

  def apply(event: EmployeeEvent): Employee = {
    event match {
      case EmployeeRegistered(email, firstName, lastName) => {
        copy(firstName = firstName, lastName = lastName)
      }
      case LeavesCredited(email, creditedLeaves) => {
        copy(leaveBalance = leaveBalance + creditedLeaves)
      }
      case FullDayLeavesApplied(email, from, to) => {
        val numberOfLeavesApplied = Days.daysBetween(from, to).getDays
        copy(leaveBalance = leaveBalance - numberOfLeavesApplied)
      }
      case HalfDayLeavesApplied(email, from, to) => {
        val numberOfLeavesApplied = Days.daysBetween(from, to).getDays * 0.5f
        copy(leaveBalance = leaveBalance - numberOfLeavesApplied)
      }
    }
  }

  def register(firstName: String, lastName: String): Employee = {
    copy(firstName = firstName, lastName = lastName)
  }

  def creditLeaves(creditedLeaves: Float): Employee = {
    copy(leaveBalance = creditedLeaves)
  }

  def applyFullDayLeaves(from: DateTime, to: DateTime): Either[String, Employee] = {
    val numberOfLeavesApplied = Days.daysBetween(from, to).getDays
    if (leaveBalance < numberOfLeavesApplied) Left("insufficient leave balance")
    else Right(copy(leaveBalance = leaveBalance - numberOfLeavesApplied))
  }

  def applyHalfDayLeaves(from: DateTime, to: DateTime): Either[String, Employee] = {
    val numberOfLeavesApplied = Days.daysBetween(from, to).getDays * 0.5f
    if (leaveBalance < numberOfLeavesApplied) Left("insufficient leave balance")
    else Right(copy(leaveBalance = leaveBalance - numberOfLeavesApplied))
  }
}

object Employee {

  def rehydrateFromEvents(historicalEvents: List[EmployeeEvent]): Employee = {
    historicalEvents.foldLeft(new Employee(historicalEvents(0).sourceId))(_ apply _)
  }

  def handle(command: EmployeeCommand)(implicit employee: Employee): Either[String, (Employee, EmployeeEvent)] = {
    command match {
      case RegisterEmployee(email, firstName, lastName) => {
        val newEmployee = employee.register(firstName, lastName)
        val event = EmployeeRegistered(email, firstName, lastName)
        Right(newEmployee, event)
      }

      case CreditLeaves(email, creditedLeaves) => {
        val newEmployee = employee.creditLeaves(creditedLeaves)
        val event = LeavesCredited(email, creditedLeaves)
        Right(newEmployee, event)
      }

      case ApplyFullDayLeaves(email, from, to) => {
        val result = employee.applyFullDayLeaves(from, to)
        result match {
          case Left(reason) => Left(reason)
          case Right(newEmployee) => {
            val event = FullDayLeavesApplied(email, from, to)
            Right(newEmployee, event)
          }
        }
      }

      case ApplyHalfDayLeaves(email, from, to) => {
        val result = employee.applyHalfDayLeaves(from, to)
        result match {
          case Left(reason) => Left(reason)
          case Right(newEmployee) => {
            val event = HalfDayLeavesApplied(email, from, to)
            Right(newEmployee, event)
          }
        }
      }
    }
  }
}