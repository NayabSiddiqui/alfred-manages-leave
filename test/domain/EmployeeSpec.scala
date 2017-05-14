package domain

import command._
import event._
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec

class EmployeeSpec extends PlaySpec {

  "Employee" should {
    "be able to register himself" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"

      implicit val employee = new Employee(email)

      val registerEmployee = RegisterEmployee(email, firstName, lastName)
      val result = Employee.handle(registerEmployee)

      result match {
        case Left(_) => fail
        case Right(tuple) => {
          tuple._1.email mustBe email
          tuple._1.firstName mustBe firstName
          tuple._1.lastName mustBe lastName
          tuple._2 match {
            case EmployeeRegistered(e, fName, lName) => {
              e mustBe email
              fName mustBe firstName
              lName mustBe lastName
            }
            case _ => fail
          }
        }
      }
    }

    "rehydrate from EmployeeRegistered event" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"

      val employeeRegistered = EmployeeRegistered(email, firstName, lastName)
      val historicalEvents = List(employeeRegistered)
      val employee = Employee.rehydrateFromEvents(historicalEvents)

      employee.email mustBe email
      employee.firstName mustBe firstName
      employee.lastName mustBe lastName
    }

    "get leaves credited in his account" in {
      def givenThatEmployeeWasRegistered(email: String, firstName: String, lastName: String): Employee = {
        val employeeRegistered = EmployeeRegistered(email, firstName, lastName)
        val historicalEvents = List(employeeRegistered)
        Employee.rehydrateFromEvents(historicalEvents)
      }

      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      implicit val employee = givenThatEmployeeWasRegistered(email, firstName, lastName)

      val creditLeaves = CreditLeaves(email, 12.5f)
      val result = Employee.handle(creditLeaves)

      result match {
        case Left(_) => fail
        case Right(tuple) => {
          tuple._1.leaveBalance mustBe 12.5f
          tuple._2 match {
            case LeavesCredited(e, creditedLeaves) => {
              e mustBe email
              creditedLeaves mustBe 12.5f
            }
            case _ => fail
          }
        }
      }
    }

    "rehydrate from LeavesCredited event" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 11.5f

      val employeeRegistered = EmployeeRegistered(email, firstName, lastName)
      val leavesCredited = LeavesCredited(email, creditedLeaves)
      val events = List(employeeRegistered, leavesCredited)
      val employee = Employee.rehydrateFromEvents(events)

      employee.leaveBalance mustBe creditedLeaves
    }

    "be able to apply for full day leaves" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 11.5f

      implicit val existingEmployee = givenThatLeavesWereCredited(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(2)
      val applyFullDayLeaves = ApplyFullDayLeaves(email, from, to)
      val result = Employee.handle(applyFullDayLeaves)

      result match {
        case Left(_) => fail
        case Right(tuple) => {
          tuple._1.leaveBalance mustBe 9.5f
          tuple._2 match {
            case FullDayLeavesApplied(email, fromDate, toDate) => {
              fromDate mustBe from
              toDate mustBe to
            }
            case _ => fail
          }
        }
      }
    }

    "rehydrate from FullDayLeavesApplied event" in {
      val email: String = "ironman@marvel.com"
      val employeeRegistered = EmployeeRegistered(email, "Tony", "Stark")
      val leavesCredited = LeavesCredited(email, 11.5f)
      val fullDayLeavesApplied = FullDayLeavesApplied(email, new DateTime(), new DateTime().plusDays(2))

      val historicalEvents = List(employeeRegistered, leavesCredited, fullDayLeavesApplied)
      val employee = Employee.rehydrateFromEvents(historicalEvents)

      employee.leaveBalance mustBe 9.5f
    }

    "not be able to apply full day leaves with insufficient balance" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 1.5f

      implicit val existingEmployee = givenThatLeavesWereCredited(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(2)
      val applyFullDayLeaves = ApplyFullDayLeaves(email, from, to)

      val result = Employee.handle(applyFullDayLeaves)

      result match {
        case Left(reason) => reason mustBe "insufficient leave balance"
        case Right(_) => fail
      }
    }

    "be able to apply for half day leaves" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 11.5f

      implicit val existingEmployee = givenThatLeavesWereCredited(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyHalfDayLeaves = ApplyHalfDayLeaves(email, from, to)
      val result = Employee.handle(applyHalfDayLeaves)

      result match {
        case Left(_) => fail
        case Right(tuple) => {
          tuple._1.leaveBalance mustBe 10f
          tuple._2 match {
            case HalfDayLeavesApplied(email, fromDate, toDate) => {
              fromDate mustBe from
              toDate mustBe to
            }
            case _ => fail
          }
        }
      }
    }

    "rehydrate from HalfDayLeavesApplied event" in {
      val email: String = "ironman@marvel.com"
      val employeeRegistered = EmployeeRegistered(email, "Tony", "Stark")
      val leavesCredited = LeavesCredited(email, 11.5f)
      val halfDayLeavesApplied = HalfDayLeavesApplied(email, new DateTime(), new DateTime().plusDays(3))

      val historicalEvents = List(employeeRegistered, leavesCredited, halfDayLeavesApplied)
      val employee = Employee.rehydrateFromEvents(historicalEvents)

      employee.leaveBalance mustBe 10f
    }

    "not be able to apply half day leaves with insufficient balance" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 1f

      implicit val existingEmployee = givenThatLeavesWereCredited(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyHalfDayLeaves = ApplyHalfDayLeaves(email, from, to)

      val result = Employee.handle(applyHalfDayLeaves)

      result match {
        case Left(reason) => reason mustBe "insufficient leave balance"
        case Right(_) => fail
      }
    }

    def givenThatLeavesWereCredited(email: String, firstName: String, lastName: String, creditedLeaves: Float)
    : Employee = {
      val employeeRegistered = EmployeeRegistered(email, firstName, lastName)
      val leavesCredited = LeavesCredited(email, creditedLeaves)
      val events = List(employeeRegistered, leavesCredited)
      Employee.rehydrateFromEvents(events)
    }
  }
}











