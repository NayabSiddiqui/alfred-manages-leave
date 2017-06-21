package domain

import java.util.UUID

import org.joda.time.{DateTime, DateTimeConstants, Days, DateTimeComparator}

case class Employee private(id: String, email: String, givenName: String, leaveBalance: Float = 0,
                            leaveApplications: List[LeaveApplication]) {

  require(!id.isEmpty, "Id cannot be empty")

  def this(id: String) = this(id, null, null, 0f, List.empty[LeaveApplication])

  def register(email: String, givenName: String): Employee = copy(email = email, givenName = givenName)

  def creditLeaves(creditedLeaves: Float): Either[String, Employee] = {
    if (creditedLeaves < 0) Left("Cannot credit negative leaves")
    else Right(copy(leaveBalance = leaveBalance + creditedLeaves))
  }

  def applyFullDayLeaves(leaveApplicationId: String = UUID.randomUUID().toString, days: List[DateTime]): Either[String, Employee] = {
    if (isLeaveAlreadyAppliedForGivenDates(days)) {
      Left("Leaves for one or more dates have already been applied.")
    }
    else {
      if (days.length > leaveBalance) Left("Insufficient leave balance")
      else {
        val application = new LeaveApplication(leaveApplicationId, days, false)
        Right(copy(leaveBalance = leaveBalance - days.length,
          leaveApplications = application :: leaveApplications))
      }
    }
  }

  def applyHalfDayLeaves(leaveApplicationId: String = UUID.randomUUID().toString, days: List[DateTime]): Either[String, Employee] = {
    if (isLeaveAlreadyAppliedForGivenDates(days)) {
      Left("Leaves for one or more dates have already been applied.")
    }
    else {
      val numberOfLeavesApplied = days.length * 0.5
      if (numberOfLeavesApplied > leaveBalance) Left("Insufficient leave balance")
      else {
        val application = new LeaveApplication(leaveApplicationId, days, true)
        Right(copy(leaveBalance = leaveBalance - numberOfLeavesApplied.toFloat,
          leaveApplications = application :: leaveApplications))
      }
    }
  }

  def cancelLeaveApplication(id: String): Either[Nothing, Employee] = {
    val leaveApplicationToBeDeleted = leaveApplications.filter(application => application.id == id).head
    val numberOfDaysToBeCredited = leaveApplicationToBeDeleted.days.length * (if (leaveApplicationToBeDeleted.halfDayLeaves) 0.5f else 1)
    Right(copy(leaveBalance = leaveBalance + numberOfDaysToBeCredited,
      leaveApplications = leaveApplications.filter(application => application.id != id)
    ))
  }

  private def isLeaveAlreadyAppliedForGivenDates(days: List[DateTime]): Boolean = {
    val allAppliedLeaves = leaveApplications.flatMap(application => application.days)
    allAppliedLeaves.intersect(days).nonEmpty
  }
}

