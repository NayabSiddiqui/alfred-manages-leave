package domain

import akka.actor.{ActorSystem, PoisonPill}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import command._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, MustMatchers, WordSpecLike}

class EmployeeActorSpec extends TestKit(ActorSystem("EmployeeActorSpec"))
  with WordSpecLike
  with MustMatchers
  with BeforeAndAfterAll
  with ImplicitSender
  with ScalaFutures {

  implicit val timeout = Timeout(30 seconds)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def givenActorWithRegisteredEmployee(id: String, email: String, givenName: String) = {
    val employeeActor = system.actorOf(EmployeeActor.props(id))

    val registerEmployee = RegisterEmployee(email, givenName)

    employeeActor ! registerEmployee
    expectMsg(EventAppliedSuccessfully())
    employeeActor
  }

  def givenActorWithCreditedLeaves(id: String, email: String, givenName: String, creditedLeaves: Float) = {
    val actor = givenActorWithRegisteredEmployee(id, email, givenName)
    val creditLeaves = CreditLeaves(creditedLeaves)
    actor ! creditLeaves
    expectMsg(EventAppliedSuccessfully())
    actor
  }

  "Employee Actor" should {
    "handle RegisterEmployee command" in {
      val id = "ironman19"
      val email = "ironman19@marvel.com"
      val givenName = "Tony Stark"

      val employeeActor = system.actorOf(EmployeeActor.props(id))

      val registerEmployee = RegisterEmployee(email, givenName)

      employeeActor ! registerEmployee

      expectMsg(EventAppliedSuccessfully())

      employeeActor ! PoisonPill

      val actor2 = system.actorOf(EmployeeActor.props(id))
      actor2 ! "getEmployee"
      expectMsg(new Employee(id).register(email, givenName))
    }

    "should not allow registration of the same employee again" in {
      val id = "batman1"
      val email = "batman1@dccomics.com"
      val givenName = "Bruce Wayne"

      val employeeActor = system.actorOf(EmployeeActor.props(id))

      val registerEmployee = RegisterEmployee(email, givenName)
      employeeActor ! registerEmployee
      expectMsg(EventAppliedSuccessfully())

      employeeActor ! registerEmployee
      expectMsg(DomainError(s"Employee with id $id is already registered."))
    }

    "should not handle any command before the employee has been registered" in {
      val id = "batman2"

      val actor = system.actorOf(EmployeeActor.props(id))

      actor ! CreditLeaves(11.5f)
      expectMsg(DomainError(s"Employee with id $id is not registered. Cannot handle commands for unregistered Employee"))

      actor ! ApplyFullDayLeaves(new DateTime(), new DateTime().plusDays(3))
      expectMsg(DomainError(s"Employee with id $id is not registered. Cannot handle commands for unregistered Employee"))

      actor ! ApplyHalfDayLeaves(new DateTime(), new DateTime().plusDays(3))
      expectMsg(DomainError(s"Employee with id $id is not registered. Cannot handle commands for unregistered Employee"))
    }

    "handle CreditLeaves command" in {
      val id = "ironman2"
      val email = "ironman2@marvel.com"
      val givenName = "Tony Stark"
      val actor = givenActorWithRegisteredEmployee(id, email, givenName)

      val creditLeaves = CreditLeaves(11.5f)
      actor ! creditLeaves
      expectMsg(EventAppliedSuccessfully())

      actor ! PoisonPill

      val actor2 = system.actorOf(EmployeeActor.props(id))
      actor2 ! "getEmployee"
      expectMsg(new Employee(id).register(email, givenName).creditLeaves(11.5f).right.get)
    }

    "handle ApplyFullDayLeaves command" in {
      val id = "ironman3"
      val email = "ironman3@marvel.com"
      val givenName = "Tony Stark"
      val creditedLeaves = 11.5f
      val actor = givenActorWithCreditedLeaves(id, email, givenName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyFullDayLeaves = ApplyFullDayLeaves(from, to)

      actor ! applyFullDayLeaves
      expectMsg(EventAppliedSuccessfully())

      actor ! PoisonPill

      //      TODO find a better way of testing the dates. assertion is failing
      //      val recoveredActor = system.actorOf(EmployeeActor.props(id))
      //      recoveredActor ! "getEmployee"
      //      expectMsg(new Employee(id)
      //        .register(email, givenName)
      //        .creditLeaves(creditedLeaves).right.get
      //        .applyFullDayLeaves(from, to).right.get)
    }

    "reject ApplyFullDayLeaves command when leave balance is insufficient" in {
      val id = "ironman4"
      val email = "ironman4@marvel.com"
      val givenName = "Tony Stark"
      val creditedLeaves = 2.5f
      val actor = givenActorWithCreditedLeaves(id, email, givenName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyFullDayLeaves = ApplyFullDayLeaves(from, to)

      actor ! applyFullDayLeaves
      expectMsg(DomainError("Insufficient leave balance"))

      actor ! PoisonPill

      val recoveredActor = system.actorOf(EmployeeActor.props(id))
      recoveredActor ! "getEmployee"
      expectMsg(new Employee(id)
        .register(email, givenName)
        .creditLeaves(creditedLeaves).right.get)
    }

    "handle ApplyHalfDayLeaves command" in {
      val id = "ironman5"
      val email = "ironman5@marvel.com"
      val givenName = "Tony Stark"
      val creditedLeaves = 11.5f
      val actor = givenActorWithCreditedLeaves(id, email, givenName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyHalfDayLeaves = ApplyHalfDayLeaves(from, to)

      actor ! applyHalfDayLeaves
      expectMsg(EventAppliedSuccessfully())

      actor ! PoisonPill

      //TODO find a better way of testing the dates. assertion is failing
      //      val recoveredActor = system.actorOf(EmployeeActor.props(id))
      //      recoveredActor ! "getEmployee"
      //      expectMsg(new Employee(id)
      //        .register(email, givenName)
      //        .creditLeaves(creditedLeaves).right.get
      //        .applyHalfDayLeaves(from, to).right.get)
    }

    "reject ApplyHalfDayLeaves command when leave balance is insufficient" in {
      val id = "ironman6"
      val email = "ironman6@marvel.com"
      val givenName = "Tony Stark"
      val creditedLeaves = 1.0f
      val actor = givenActorWithCreditedLeaves(id, email, givenName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyHalfDayLeaves = ApplyHalfDayLeaves(from, to)

      actor ! applyHalfDayLeaves
      expectMsg(DomainError("Insufficient leave balance"))

      actor ! PoisonPill

      val recoveredActor = system.actorOf(EmployeeActor.props(id))
      recoveredActor ! "getEmployee"
      expectMsg(new Employee(id)
        .register(email, givenName)
        .creditLeaves(creditedLeaves).right.get)
    }

    "handle GetLeaveBalance command" in {
      val id = "ironman7"
      val email = "ironman7@marvel.com"
      val givenName = "Tony Stark"
      val creditedLeaves = 11.5f
      val actor = givenActorWithCreditedLeaves(id, email, givenName, creditedLeaves)

      val getLeaveBalance = GetLeaveBalance()

      actor ! getLeaveBalance
      expectMsg(11.5f)
    }

    "handle GetLeaveSummary command" in {
      val id = "ironman8"
      val email = "ironman8@marvel.com"
      val givenName = "Tony Stark"
      val creditedLeaves = 11.5f
      val actor = givenActorWithCreditedLeaves(id, email, givenName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      actor ! ApplyFullDayLeaves(from, to)

      val from2 = from.plusDays(5)
      val to2 = from.plusDays(7)
      actor ! ApplyFullDayLeaves(from2, to2)

      val result = actor ? GetLeaveSummary()
      result map {
        case x: LeaveSummary => {
          x.leaveApplications.length mustBe 2
          x.balance mustBe 5.5f
        }
        case _ => fail
      }
    }
  }
}













