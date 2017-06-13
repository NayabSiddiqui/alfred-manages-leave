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

  def applyFullDayLeaves(leaveApplicationId: String = UUID.randomUUID().toString, from: DateTime, to: DateTime): Either[String, Employee] = {
    if (leaveAlreadyAppliedForGivenDates(from, to)) {
      Left("Leaves for one or more dates have already been applied.")
    }
    else {
      val leaveDays = getWorkingDayLeaves(from, to)
      if (leaveDays.length > leaveBalance) Left("Insufficient leave balance")
      else {
        val application = new LeaveApplication(leaveApplicationId, leaveDays)
        Right(copy(leaveBalance = leaveBalance - leaveDays.length,
          leaveApplications = application :: leaveApplications))
      }
    }
  }

  def applyHalfDayLeaves(leaveApplicationId: String = UUID.randomUUID().toString, from: DateTime, to: DateTime): Either[String, Employee] = {
    if (leaveAlreadyAppliedForGivenDates(from, to)) {
      Left("Leaves for one or more dates have already been applied.")
    }
    else {
      val leaveDays = getWorkingDayLeaves(from, to)
      val numberOfLeavesApplied = leaveDays.length * 0.5
      if (numberOfLeavesApplied > leaveBalance) Left("Insufficient leave balance")
      else {
        val application = new LeaveApplication(leaveApplicationId, leaveDays)
        Right(copy(leaveBalance = leaveBalance - numberOfLeavesApplied.toFloat,
          leaveApplications = application :: leaveApplications))
      }
    }
  }

  private def getWorkingDayLeaves(from: DateTime, to: DateTime): List[DateTime] = {
    val totalNumberOfDays = Days.daysBetween(from, to).getDays
    val allDates = for (day <- 0 to totalNumberOfDays) yield from.plusDays(day).withTimeAtStartOfDay()
    allDates.filter(date =>
      date.getDayOfWeek != DateTimeConstants.SATURDAY && date.getDayOfWeek != DateTimeConstants.SUNDAY)
      .toList
  }

  private def leaveAlreadyAppliedForGivenDates(from: DateTime, to: DateTime): Boolean = {
    val allAppliedLeaves = leaveApplications.flatMap(application => application.days)
    val appliedLeaveDays = getWorkingDayLeaves(from, to)
    allAppliedLeaves.intersect(appliedLeaveDays).nonEmpty
  }
}

