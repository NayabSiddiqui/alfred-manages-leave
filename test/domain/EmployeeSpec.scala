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

  def givenEmployeeWithAppliedLeaves(creditedLeaves: Float, from: DateTime, to: DateTime) = {
    val employee = givenEmployeeWithCreditedLeaves(creditedLeaves)
    employee.applyFullDayLeaves(UUID.randomUUID().toString, from, to).right.get
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
      val to = new DateTime(2017, 6, 15, 0, 0)

      employee.applyFullDayLeaves("myApplication", from, to) match {
        case Left(_) => fail
        case Right(updatedEmployee) => {
          updatedEmployee.leaveBalance mustBe 8.5f
          val leaveApplications = updatedEmployee.leaveApplications
          leaveApplications.length mustBe 1
          val application = leaveApplications.head
          application.id mustBe "myApplication"
          application.days.length mustBe 4
        }
      }
    }

    "not be able to apply duplicate leaves" in {
      val from = new DateTime()
      val to = from.plusDays(6)
      val creditedLeaves: Float = 12.5f

      val employee = givenEmployeeWithAppliedLeaves(creditedLeaves, from, to)

      val newLeavesFrom = from.plusDays(2).plusHours(4)
      val newLeavesTill = from.plusDays(3)


      employee.applyFullDayLeaves(UUID.randomUUID().toString, newLeavesFrom, newLeavesTill) match {
        case Left(reason) => reason mustBe "Leaves for one or more dates have already been applied."
        case Right(_) => fail
      }
    }

    "be able to apply full day leaves excluding weekends" in {
      val employee = givenEmployeeWithCreditedLeaves(12.5f)

      val from = new DateTime(2017, 6, 11, 0, 0)
      val to = new DateTime(2017, 6, 20, 0, 0)

      employee.applyFullDayLeaves(from = from, to = to) match {
        case Left(_) => fail
        case Right(updatedEmployee) => {
          updatedEmployee.leaveBalance mustBe 5.5f
          val leaveApplications = updatedEmployee.leaveApplications
          leaveApplications.length mustBe 1
          val application = leaveApplications.head
          application.days.length mustBe 7
        }
      }
    }

    "not be able to apply full day leaves with insufficient balance" in {
      val employee = givenEmployeeWithCreditedLeaves(2.5f)

      val from = new DateTime(2017, 6, 12, 0, 0)
      val to = new DateTime(2017, 6, 15, 0, 0)

      employee.applyFullDayLeaves(from = from, to = to) match {
        case Left(reason) => reason mustBe "Insufficient leave balance"
        case Right(_) => fail
      }
    }

    "be able to apply half day leaves" in {
      val employee = givenEmployeeWithCreditedLeaves(2.5f)

      val from = new DateTime(2017, 6, 12, 0, 0)
      val to = new DateTime(2017, 6, 14, 0, 0)

      employee.applyHalfDayLeaves("myLeaveApplication", from = from, to = to) match {
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
      val to = new DateTime(2017, 6, 15, 0, 0)

      employee.applyHalfDayLeaves(from = from, to = to) match {
        case Left(reason) => reason mustBe "Insufficient leave balance"
        case Right(_) => fail
      }
    }
  }
}




