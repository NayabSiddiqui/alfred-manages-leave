package service

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
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

      whenReady(futureResult){
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

    "apply for full day leaves" in {
      val id = "batman1"
      val service = new EmployeeService()
      val result = service.applyFullDayLeaves(id, new DateTime(), new DateTime()).futureValue
      result.right.getOrElse(fail)
    }

    "apply for half day leaves" in {
      val id = "batman1"
      val service = new EmployeeService()
      val result = service.applyHalfDayLeaves(id, new DateTime(), new DateTime()).futureValue
      result.right.getOrElse(fail)
    }
  }
}
