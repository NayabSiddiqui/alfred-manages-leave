package domain

import java.util.UUID

import org.joda.time.{DateTime, Days}
import org.scalatestplus.play.PlaySpec

class EmployeeSpec extends PlaySpec {

  val id = "ironman"
  val email = "ironman@marvel.com"
  val givenName = "Tony Stark"

  def givenRegisteredEmployee = {
    val employee = new Employee(id)
    employee.register(email, givenName)
  }

  def givenEmployeeWithCreditedLeaves(creditedLeaves: Float): Employee = {
    val employee = givenRegisteredEmployee
    employee.creditLeaves(creditedLeaves).right.get
  }

  def givenEmployeeWithAppliedLeaves(creditedLeaves: Float, leaveDays: List[DateTime]) = {
    val employee = givenEmployeeWithCreditedLeaves(creditedLeaves)
    employee.applyFullDayLeaves(UUID.randomUUID().toString, leaveDays).right.get
  }

  "Employee" should {
    "be able to register himself" in {

      val employee = new Employee(id).register(email, givenName)

      employee.id mustBe id
      employee.email mustBe email
      employee.givenName mustBe givenName
      employee.leaveBalance mustBe 0
      employee.leaveApplications mustBe empty
    }

    "get leaves credited" in {
      val existingEmployee = givenRegisteredEmployee

      existingEmployee.creditLeaves(12f) match {
        case Left(_) => fail
        case Right(employee) => employee.leaveBalance mustBe 12f
      }
    }

    "not accept negative credited leaves" in {
      val existingEmployee = givenRegisteredEmployee

      existingEmployee.creditLeaves(-12f) match {
        case Left(reason) => reason mustBe "Cannot credit negative leaves"
        case Right(_) => fail
      }
    }

    "be able to apply full day leaves" in {
      val employee = givenEmployeeWithCreditedLeaves(12.5f)

      val from = new DateTime(2017, 6, 12, 0, 0)
      val leaveDays = List[DateTime](from, from plusDays 1, from plusDays 2, from plusDays 3)

      employee.applyFullDayLeaves("myApplication", leaveDays) match {
        case Left(_) => fail
        case Right(updatedEmployee) => {
          updatedEmployee.leaveBalance mustBe 8.5f
          val leaveApplications = updatedEmployee.leaveApplications
          leaveApplications.length mustBe 1
          val application = leaveApplications.head
          application.id mustBe "myApplication"
          application.days.length mustBe 4
          application.days mustBe leaveDays
        }
      }
    }

    "not be able to apply duplicate leaves" in {
      val from = new DateTime(2017, 6, 12, 0, 0)
      val leaveDays = List[DateTime](from, from plusDays 1, from plusDays 2, from plusDays 3)
      val creditedLeaves: Float = 12.5f

      val employee = givenEmployeeWithAppliedLeaves(creditedLeaves, leaveDays)

      employee.applyFullDayLeaves(UUID.randomUUID().toString,
        List[DateTime](from plusDays 3, from plusDays 4)) match {
        case Left(reason) => reason mustBe "Leaves for one or more dates have already been applied."
        case Right(_) => fail
      }
    }

    "not be able to apply full day leaves with insufficient balance" in {
      val employee = givenEmployeeWithCreditedLeaves(2.5f)

      val from = new DateTime(2017, 6, 12, 0, 0)
      val leaveDays = List[DateTime](from, from plusDays 1, from plusDays 2, from plusDays 3)

      employee.applyFullDayLeaves(UUID.randomUUID().toString,leaveDays) match {
        case Left(reason) => reason mustBe "Insufficient leave balance"
        case Right(_) => fail
      }
    }

    "be able to apply half day leaves" in {
      val employee = givenEmployeeWithCreditedLeaves(2.5f)

      val from = new DateTime(2017, 6, 12, 0, 0)
      val leaveDays = List[DateTime](from, from plusDays 1, from plusDays 2)

      employee.applyHalfDayLeaves("myLeaveApplication", leaveDays) match {
        case Left(_) => fail
        case Right(updatedEmployee) => {
          updatedEmployee.leaveBalance mustBe 1f
          updatedEmployee.leaveApplications.length mustBe 1

          val application = updatedEmployee.leaveApplications.head
          application.id mustBe "myLeaveApplication"
          application.days.length mustBe 3
        }
      }
    }

    "not be able to apply half day leaves with insufficient balance" in {
      val employee = givenEmployeeWithCreditedLeaves(1f)

      val from = new DateTime(2017, 6, 12, 0, 0)
      val leaveDays = List[DateTime](from, from plusDays 1, from plusDays 2)

      employee.applyHalfDayLeaves("myLeaveApplication", leaveDays) match {
        case Left(reason) => reason mustBe "Insufficient leave balance"
        case Right(_) => fail
      }
    }

    "should be able to delete an already submitted leave application" in {
      val from = new DateTime(2017, 6, 12, 0, 0)
      val leaveDays = List[DateTime](from, from plusDays 1, from plusDays 2, from plusDays 3)
      val creditedLeaves: Float = 12.5f

      val employee = givenEmployeeWithAppliedLeaves(creditedLeaves, leaveDays)
      employee.leaveApplications.length mustBe 1

      val updatedEmployee = employee.cancelLeaveApplication(employee.leaveApplications.head.id).right.get
      updatedEmployee.leaveApplications mustBe empty
    }
  }
}




