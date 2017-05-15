package domain

import akka.actor.{ActorSystem, PoisonPill, Status}
import akka.testkit.{ImplicitSender, TestKit}
import command.{ApplyFullDayLeaves, ApplyHalfDayLeaves, CreditLeaves, RegisterEmployee}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class EmployeeActorSpec extends TestKit(ActorSystem("EmployeeActorSpec"))
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def givenActorWithRegisteredEmployee(email: String, firstName: String, lastName: String) = {
    val employeeActor = system.actorOf(EmployeeActor.props(email))

    val registerEmployee = RegisterEmployee(firstName, lastName)

    employeeActor ! registerEmployee
    expectMsg(new Employee(email).register(firstName, lastName))
    employeeActor
  }

  def givenActorWithCreditedLeaves(email: String, firstName: String, lastName: String, creditedLeaves: Float) = {
    val actor = givenActorWithRegisteredEmployee(email, firstName, lastName)
    val creditLeaves = CreditLeaves(creditedLeaves)
    actor ! creditLeaves
    expectMsg(new Employee(email).register(firstName, lastName).creditLeaves(creditedLeaves).right.get)
    actor
  }

  "Employee Actor" should {
    "handle RegisterEmployee command" in {
      val email = "ironman@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"

      val employeeActor = system.actorOf(EmployeeActor.props(email))

      val registerEmployee = RegisterEmployee(firstName, lastName)

      employeeActor ! registerEmployee

      expectMsg(new Employee(email).register(firstName, lastName))

      employeeActor ! PoisonPill

      val actor2 = system.actorOf(EmployeeActor.props(email))
      actor2 ! "getEmployee"
      expectMsg(new Employee(email).register(firstName, lastName))
    }

    "handle CreditLeaves command" in {
      val email = "ironman2@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val actor = givenActorWithRegisteredEmployee(email, firstName, lastName)

      val creditLeaves = CreditLeaves(11.5f)
      actor ! creditLeaves
      expectMsg(new Employee(email).register(firstName, lastName).creditLeaves(11.5f).right.get)

      actor ! PoisonPill

      val actor2 = system.actorOf(EmployeeActor.props(email))
      actor2 ! "getEmployee"
      expectMsg(new Employee(email).register(firstName, lastName).creditLeaves(11.5f).right.get)
    }

    "handle ApplyFullDayLeaves command" in {
      val email = "ironman3@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 11.5f
      val actor = givenActorWithCreditedLeaves(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyFullDayLeaves = ApplyFullDayLeaves(from, to)

      actor ! applyFullDayLeaves
      expectMsg(new Employee(email)
        .register(firstName, lastName)
        .creditLeaves(creditedLeaves).right.get
        .applyFullDayLeaves(from, to).right.get)

      actor ! PoisonPill

      val recoveredActor = system.actorOf(EmployeeActor.props(email))
      recoveredActor ! "getEmployee"
      expectMsg(new Employee(email)
        .register(firstName, lastName)
        .creditLeaves(creditedLeaves).right.get
        .applyFullDayLeaves(from, to).right.get)
    }

    "reject ApplyFullDayLeaves command when leave balance is insufficient" in {
      val email = "ironman4@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 2.5f
      val actor = givenActorWithCreditedLeaves(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyFullDayLeaves = ApplyFullDayLeaves(from, to)

      actor ! applyFullDayLeaves
      expectMsg(Status.Failure)

      actor ! PoisonPill

      val recoveredActor = system.actorOf(EmployeeActor.props(email))
      recoveredActor ! "getEmployee"
      expectMsg(new Employee(email)
        .register(firstName, lastName)
        .creditLeaves(creditedLeaves).right.get)
    }

    "handle ApplyHalfDayLeaves command" in {
      val email = "ironman5@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 11.5f
      val actor = givenActorWithCreditedLeaves(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyHalfDayLeaves = ApplyHalfDayLeaves(from, to)

      actor ! applyHalfDayLeaves
      expectMsg(new Employee(email)
        .register(firstName, lastName)
        .creditLeaves(creditedLeaves).right.get
        .applyHalfDayLeaves(from, to).right.get)

      actor ! PoisonPill

      val recoveredActor = system.actorOf(EmployeeActor.props(email))
      recoveredActor ! "getEmployee"
      expectMsg(new Employee(email)
        .register(firstName, lastName)
        .creditLeaves(creditedLeaves).right.get
        .applyHalfDayLeaves(from, to).right.get)
    }

    "reject ApplyHalfDayLeaves command when leave balance is insufficient" in {
      val email = "ironman6@marvel.com"
      val firstName = "Tony"
      val lastName = "Stark"
      val creditedLeaves = 1.0f
      val actor = givenActorWithCreditedLeaves(email, firstName, lastName, creditedLeaves)

      val from = new DateTime()
      val to = from.plusDays(3)
      val applyHalfDayLeaves = ApplyHalfDayLeaves(from, to)

      actor ! applyHalfDayLeaves
      expectMsg(Status.Failure)

      actor ! PoisonPill

      val recoveredActor = system.actorOf(EmployeeActor.props(email))
      recoveredActor ! "getEmployee"
      expectMsg(new Employee(email)
        .register(firstName, lastName)
        .creditLeaves(creditedLeaves).right.get)
    }
  }
}











