package domain

import akka.actor.Status.Status
import akka.actor.{Props, Status}
import akka.persistence.PersistentActor
import command.{ApplyFullDayLeaves, ApplyHalfDayLeaves, CreditLeaves, RegisterEmployee}
import event._

class EmployeeActor(email: String) extends PersistentActor {

  var employee = new Employee(email)

  override def persistenceId: String = s"order-$email"

  override def receiveCommand: Receive = {
    case RegisterEmployee(firstName, lastName) => {
      persist(EmployeeRegistered(firstName, lastName)) { event =>
        applyEvent(event) match {
          case Left(reason) => sender ! Status.Failure
          case Right(updatedEmployee) => sender ! updatedEmployee
        }
      }
    }
    case CreditLeaves(creditedLeaves) => {
      persist(LeavesCredited(creditedLeaves)) { event => {
        applyEvent(event) match {
          case Left(reason) => sender ! Status.Failure
          case Right(updatedEmployee) => sender ! updatedEmployee
        }
      }
      }
    }
    case ApplyFullDayLeaves(from, to) => {
      val event: FullDayLeavesApplied = FullDayLeavesApplied(from, to)
      applyEvent(event) match {
        case Left(reason) => sender ! Status.Failure
        case Right(updatedEmployee) => {
          persist(event) { event =>
            sender ! updatedEmployee
          }
        }
      }
    }
    case ApplyHalfDayLeaves(from, to) => {
      val event: HalfDayLeavesApplied = HalfDayLeavesApplied(from, to)
      applyEvent(event) match {
        case Left(reason) => sender ! Status.Failure
        case Right(updatedEmployee) => {
          persist(event) { event =>
            sender ! updatedEmployee
          }
        }
      }
    }

    //TODO only for testing purpose. Maybe find a better way to do this
    case "getEmployee" => sender ! employee
  }

  override def receiveRecover: Receive = {
    case event: EmployeeEvent => applyEvent(event)
  }

  def applyEvent(event: EmployeeEvent): Either[String, Employee] = {
    event match {
      case EmployeeRegistered(firstName, lastName) => {
        employee = employee.register(firstName, lastName)
        Right(employee)
      }
      case LeavesCredited(creditedLeaves) => {
        employee.creditLeaves(creditedLeaves) match {
          case Left(reason) => Left(reason)
          case Right(updatedEmployee) => {
            employee = updatedEmployee
            Right(employee)
          }
        }
      }
      case FullDayLeavesApplied(from, to) => {
        employee.applyFullDayLeaves(from, to) match {
          case Left(reason) => Left(reason)
          case Right(updatedEmployee) => {
            employee = updatedEmployee
            Right(employee)
          }
        }
      }
      case HalfDayLeavesApplied(from, to) => {
        employee.applyHalfDayLeaves(from, to) match {
          case Left(reason) => Left(reason)
          case Right(updatedEmployee) => {
            employee = updatedEmployee
            Right(employee)
          }
        }
      }
    }
  }
}

object EmployeeActor {
  def props(email: String): Props = Props(new EmployeeActor(email))
}