package service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import domain.LeaveSummary
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

class EmployeeServiceSpec extends TestKit(ActorSystem("EmployeeServiceSpec"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll
  with ImplicitSender with ScalaFutures {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  "Employee Service" should {
    "register a new employee" in {
      val id = "batman"
      val email = "batman@gotham.com"
      val givenName = "Bruce Wayne"

      val service = new EmployeeService()
      val futureResult = service.registerEmployee(id, email, givenName)

      whenReady(futureResult) {
        result => result.right.getOrElse(fail)
      }
    }

    "not register same employee again" in {
      val id = "batman"
      val email = "batman@gotham.com"
      val givenName = "Bruce Wayne"

      val service = new EmployeeService()
      val result = service.registerEmployee(id, email, givenName).futureValue

      result.left.getOrElse(fail) mustBe s"Employee with id $id is already registered."
    }

    "credit leaves to employee" in {
      val id = "batman1"
      val email = "batman@gotham.com"
      val givenName = "Bruce Wayne"
      val service = new EmployeeService()
      var result = service.registerEmployee(id, email, givenName).futureValue
      result.right.getOrElse(fail)

      result = service.creditLeaves(id, 14.5f).futureValue
      result.right.getOrElse(fail)
    }

    "get leave balance" in {
      val id = "batman1"
      val service = new EmployeeService()
      val futureResult = service.getLeaveBalance(id)

      whenReady(futureResult) {
        result => {
          val balance = result.right.getOrElse(fail)
          balance mustBe 14.5f
        }
      }
    }

    "apply for full day leaves" in {
      val id = "batman1"
      val service = new EmployeeService()
      val from = new DateTime(2017, 6, 11, 0, 0)
      val to = new DateTime(2017, 6, 15, 0, 0)
      val result = service.applyFullDayLeaves(id, from, to).futureValue
      result.right.getOrElse(fail)
    }

    "apply for half day leaves" in {
      val id = "batman1"
      val service = new EmployeeService()
      val from = new DateTime(2017, 6, 19, 0, 0)
      val to = new DateTime(2017, 6, 21, 0, 0)
      val result = service.applyHalfDayLeaves(id, from, to).futureValue
      result.right.getOrElse(fail)
    }

    "get leave summary of the employee" in {
      val id = "batman1"
      val service = new EmployeeService()
      val futureResult = service.getLeaveSummary(id)
      whenReady(futureResult) { result => {
        val summary = result.right.getOrElse(fail)
        summary.leaveApplications.length mustBe 2
        summary.balance mustBe 9f
      }

      }
    }

    "cancel leave application of the employee" in {
      val id = "batman1"
      val service = new EmployeeService()
      val leaveSummary = service.getLeaveSummary(id).futureValue.right.getOrElse(fail)

      val applicationIdToBeCancelled = leaveSummary.leaveApplications.head.id
      val result = service.cancelLeaveApplication(id,applicationIdToBeCancelled).futureValue.right.getOrElse(fail)
      result mustBe Success()

      val summary = service.getLeaveSummary(id).futureValue.right.getOrElse(fail)
      summary.leaveApplications.filter(application => application.id == applicationIdToBeCancelled) mustBe empty
    }
  }
}
