package service

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import command.{ApplyFullDayLeaves, ApplyHalfDayLeaves, CreditLeaves, RegisterEmployee}
import domain.{DomainError, EmployeeActor, EventAppliedSuccessfully}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

case class Success()

case class EmployeeService @Inject()(implicit private val system: ActorSystem, ec: ExecutionContext = ExecutionContext.global) {
  implicit val timeout = Timeout(20 seconds)

  def registerEmployee(email: String, firstName: String, lastName: String)
  : Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(email))
    val result: Future[Any] = actor ? RegisterEmployee(firstName, lastName)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def creditLeaves(email: String, creditedLeaves: Float): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(email))
    val result: Future[Any] = actor ? CreditLeaves(creditedLeaves)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def applyFullDayLeaves(email: String, from: DateTime, to: DateTime): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(email))
    val result: Future[Any] = actor ? ApplyFullDayLeaves(from, to)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def applyHalfDayLeaves(email: String, from: DateTime, to: DateTime): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(email))
    val result: Future[Any] = actor ? ApplyHalfDayLeaves(from, to)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }
}
