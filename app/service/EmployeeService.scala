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
  implicit val timeout = Timeout(30 seconds)

  def registerEmployee(id: String, email: String, givenName: String)
  : Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result: Future[Any] = actor ? RegisterEmployee(email, givenName)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def creditLeaves(id: String, creditedLeaves: Float): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result: Future[Any] = actor ? CreditLeaves(creditedLeaves)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def applyFullDayLeaves(id: String, from: DateTime, to: DateTime): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result: Future[Any] = actor ? ApplyFullDayLeaves(from, to)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def applyHalfDayLeaves(id: String, from: DateTime, to: DateTime): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result: Future[Any] = actor ? ApplyHalfDayLeaves(from, to)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }
}
