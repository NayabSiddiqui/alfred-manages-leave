package domain

import java.util.UUID

import akka.actor.Props
import akka.persistence.PersistentActor
import command.{ApplyFullDayLeaves, ApplyHalfDayLeaves, CreditLeaves, RegisterEmployee}
import event._

case class DomainError(message: String)

case class EventAppliedSuccessfully()

class EmployeeActor(id: String) extends PersistentActor {

  var employee = new Employee(id)

  override def persistenceId: String = s"employee-$id"

  def unregistered: Receive = {
    case RegisterEmployee(email, givenName) => {
      persist(EmployeeRegistered(email, givenName)) { event =>
        applyEvent(event) match {
          case Left(reason) => sender ! DomainError(reason)
          case Right(success) => sender ! success
        }
      }
    }
    case _ => sender ! DomainError(s"Employee with id $id is not registered. Cannot handle commands for unregistered Employee")
  }

  def registered: Receive = {
    case RegisterEmployee(email, givenName) => sender ! DomainError(s"Employee with id $id is already registered.")
    case CreditLeaves(creditedLeaves) => {
      persist(LeavesCredited(creditedLeaves)) { event => {
        applyEvent(event) match {
          case Left(reason) => sender ! DomainError(reason)
          case Right(success) => sender ! success
        }
      }
      }
    }
    case ApplyFullDayLeaves(from, to) => {
      val event: LeavesApplied = LeavesApplied(UUID.randomUUID().toString,from.withTimeAtStartOfDay,to.withTimeAtStartOfDay, isHalfDay = false)
      applyEvent(event) match {
        case Left(reason) => sender ! DomainError(reason)
        case Right(success) => {
          persist(event) { event =>
            sender ! success
          }
        }
      }
    }
    case ApplyHalfDayLeaves(from, to) => {
      val event: LeavesApplied = LeavesApplied(UUID.randomUUID().toString,from.withTimeAtStartOfDay,to.withTimeAtStartOfDay, isHalfDay = true)
      applyEvent(event) match {
        case Left(reason) => sender ! DomainError(reason)
        case Right(success) => {
          persist(event) { event =>
            sender ! success
          }
        }
      }
    }

    //TODO only for testing purpose. Maybe find a better way to do this
    case "getEmployee" => sender ! employee
  }

  override def receiveCommand: Receive = unregistered

  override def receiveRecover: Receive = {
    case event: EmployeeEvent => applyEvent(event)
  }

  def applyEvent(event: EmployeeEvent): Either[String, EventAppliedSuccessfully] = {
    event match {
      case EmployeeRegistered(email, givenName) => {
        employee = employee.register(email, givenName)
        context become registered
        Right(EventAppliedSuccessfully())
      }
      case LeavesCredited(creditedLeaves) => {
        employee.creditLeaves(creditedLeaves) match {
          case Left(reason) => Left(reason)
          case Right(updatedEmployee) => {
            employee = updatedEmployee
            Right(EventAppliedSuccessfully())
          }
        }
      }
      case LeavesApplied(applicationId,from, to, isHalfDay) => {
        isHalfDay match {
          case false => employee.applyFullDayLeaves(applicationId,from, to) match {
            case Left(reason) => Left(reason)
            case Right(updatedEmployee) => {
              employee = updatedEmployee
              Right(EventAppliedSuccessfully())
            }
          }
          case true => employee.applyHalfDayLeaves(applicationId, from, to) match {
            case Left(reason) => Left(reason)
            case Right(updatedEmployee) => {
              employee = updatedEmployee
              Right(EventAppliedSuccessfully())
            }
          }
        }
      }
    }
  }
}

object EmployeeActor {
  def props(id: String): Props = Props(new EmployeeActor(id))
}