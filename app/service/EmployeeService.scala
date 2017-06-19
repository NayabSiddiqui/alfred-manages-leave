package service

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import command._
import domain.{DomainError, EmployeeActor, EventAppliedSuccessfully, LeaveSummary}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

case class Success()

case class EmployeeService @Inject()(private val teamCalendarService: TeamCalendarService)(
  implicit private val system: ActorSystem,
  ec: ExecutionContext = ExecutionContext.global) {


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
    val result: Future[Any] = actor ? ApplyFullDayLeaves(teamCalendarService.getWorkingDaysBetween(from, to))
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def applyHalfDayLeaves(id: String, from: DateTime, to: DateTime): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result: Future[Any] = actor ? ApplyHalfDayLeaves(teamCalendarService.getWorkingDaysBetween(from, to))
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def cancelLeaveApplication(id: String, applicationId: String): Future[Either[String, Success]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result = actor ? CancelLeaveApplication(applicationId)
    result.map {
      case DomainError(message) => Left(message)
      case EventAppliedSuccessfully() => Right(Success())
    }
  }

  def getLeaveBalance(id: String): Future[Either[String, Float]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result = actor ? GetLeaveBalance()
    result.map {
      case DomainError(message) => Left(message)
      case balance: Float => Right(balance)
    }
  }

  def getLeaveSummary(id: String): Future[Either[String, LeaveSummary]] = {
    val actor = system.actorOf(EmployeeActor.props(id))
    val result = actor ? GetLeaveSummary()
    result map {
      case DomainError(message) => Left(message)
      case summary: LeaveSummary => Right(summary)
    }
  }
}
