package domain

import org.joda.time.{DateTime, Days}

case class Employee private(id: String, email: String, givenName: String, leaveBalance: Float = 0) {
  require(!id.isEmpty, "Id cannot be empty")
  def this(id: String) = this(id, null, null, 0f)

  def register(email: String, givenName: String): Employee = copy(email = email, givenName = givenName)

  def creditLeaves(creditedLeaves: Float): Either[String, Employee] = {
    if(creditedLeaves < 0) Left("Cannot credit negative leaves")
    else Right(copy(leaveBalance = leaveBalance + creditedLeaves))
  }

  def applyFullDayLeaves(from: DateTime, to: DateTime): Either[String, Employee] = {
    val numberOfLeavesApplied = Days.daysBetween(from, to).getDays
    if (numberOfLeavesApplied > leaveBalance) Left("Insufficient leave balance")
    else Right(copy(leaveBalance = leaveBalance - numberOfLeavesApplied))
  }

  def applyHalfDayLeaves(from: DateTime, to: DateTime): Either[String, Employee] = {
    val numberOfLeavesApplied = Days.daysBetween(from, to).getDays * 0.5f
    if (numberOfLeavesApplied > leaveBalance) Left("Insufficient leave balance")
    else Right(copy(leaveBalance = leaveBalance - numberOfLeavesApplied))
  }
}