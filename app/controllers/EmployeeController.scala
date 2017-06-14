package controllers

import javax.inject.{Inject, Singleton}

import akka.util.ByteString
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import service.{EmployeeService, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
case class EmployeeController @Inject()(private val employeeService: EmployeeService) extends Controller {

//  import domain.LeaveSummary._

  def getLeaveBalance(id: String) = Action.async { implicit request =>
    val result = employeeService.getLeaveBalance(id)
    result map {
      case Left(reason) => BadRequest(reason)
      case Right(leaveBalance) => Ok(leaveBalance.toString)
    }
  }

  def getLeaveSummary(id: String) = Action.async { implicit request =>
    val result = employeeService.getLeaveSummary(id)
    result map {
      case Left(reason) => BadRequest(reason)
      case Right(summary) => Ok(Json.toJson(summary))
    }
  }

  def registerEmployee: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val id = (request.body \ "id").as[String]
    val email = (request.body \ "email").as[String]
    val givenName = (request.body \ "givenName").as[String]

    val result: Future[Either[String, Success]] = employeeService.registerEmployee(id, email, givenName)
    result.map {
      case Left(reason) => BadRequest(reason)
      case Right(_) => Created
    }
  }

  def applyLeaves(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val from = DateTime.parse((request.body \ "from").as[String])
    val to = DateTime.parse((request.body \ "to").as[String])
    val isHalfDay = (request.body \ "isHalfDay").as[Boolean]

    val result = if (isHalfDay) employeeService.applyHalfDayLeaves(id, from, to) else employeeService.applyFullDayLeaves(id, from, to)
    result.map {
      case Left(reason) => BadRequest(reason)
      case Right(_) => Ok
    }
  }

  def creditLeaves(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val creditedLeaves = (request.body \ "creditedLeaves").as[String].toFloat

    val result = employeeService.creditLeaves(id, creditedLeaves)
    result.map {
      case Left(reason) => BadRequest(reason)
      case Right(_) => Ok
    }
  }
}
