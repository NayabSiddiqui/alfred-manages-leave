package domain

import org.joda.time.{DateTime, Days}
import org.scalatestplus.play.PlaySpec

class EmployeeSpec extends PlaySpec {

  val email = "ironman@marvel.com"
  val firstName = "Tony"
  val lastName = "Stark"

  def givenRegisteredEmployee = {
    val employee = new Employee(email)
    employee.register(firstName, lastName)
  }

  def givenEmployeeWithCreditedLeaves(creditedLeaves: Float): Employee = {
    val employee = givenRegisteredEmployee
    employee.creditLeaves(creditedLeaves).right.get
  }

  "Employee" should {
    "be able to register himself" in {

      val employee = new Employee(email).register(firstName, lastName)

      employee.email mustBe email
      employee.firstName mustBe firstName
      employee.lastName mustBe lastName
      employee.leaveBalance mustBe 0
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

      val from = new DateTime()
      val to = from.plusDays(3)

      employee.applyFullDayLeaves(from, to) match {
        case Left(_) => fail
        case Right(updatedEmployee) => updatedEmployee.leaveBalance mustBe 9.5f
      }
    }

    "not be able to apply full day leaves with insufficient balance" in {
      val employee = givenEmployeeWithCreditedLeaves(2.5f)

      val from = new DateTime()
      val to = from.plusDays(3)

      employee.applyFullDayLeaves(from, to) match {
        case Left(reason) => reason mustBe "Insufficient leave balance"
        case Right(_) => fail
      }
    }

    "be able to apply half day leaves" in {
      val employee = givenEmployeeWithCreditedLeaves(2.5f)

      val from = new DateTime()
      val to = from.plusDays(3)

      employee.applyHalfDayLeaves(from, to) match {
        case Left(_) => fail
        case Right(updatedEmployee) => updatedEmployee.leaveBalance mustBe 1f
      }
    }

    "not be able to apply half day leaves with insufficient balance" in {
      val employee = givenEmployeeWithCreditedLeaves(1f)

      val from = new DateTime()
      val to = from.plusDays(3)

      employee.applyHalfDayLeaves(from, to) match {
        case Left(reason) => reason mustBe "Insufficient leave balance"
        case Right(_) => fail
      }
    }
  }
}




