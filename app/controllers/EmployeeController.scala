package controllers

import javax.inject.{Inject, Singleton}

import akka.util.ByteString
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.libs.json.JsValue
import play.api.mvc._
import service.{EmployeeService, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
case class EmployeeController @Inject()(private val employeeService: EmployeeService) extends Controller {

  def registerEmployee: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val email = (request.body \ "email").as[String]
    val firstName = (request.body \ "firstName").as[String]
    val lastName = (request.body \ "lastName").as[String]

    val result: Future[Either[String, Success]] = employeeService.registerEmployee(email, firstName, lastName)
    result.map {
      case Left(reason) => BadRequest(reason)
      case Right(_) => Created
    }
  }

  def applyLeaves(email: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val from = DateTime.parse((request.body \ "from").as[String])
    val to = DateTime.parse((request.body \ "to").as[String])
    val isHalfDay = (request.body \ "isHalfDay").as[Boolean]

    val result = if(isHalfDay) employeeService.applyHalfDayLeaves(email, from, to) else employeeService.applyFullDayLeaves(email, from, to)
    result.map {
      case Left(reason) => BadRequest(reason)
      case Right(_) => Ok
    }
  }

  def creditLeaves(email: String): Action[JsValue] = Action.async(parse.json) {implicit request =>
    val creditedLeaves = (request.body \ "creditedLeaves").as[String].toFloat

    val result = employeeService.creditLeaves(email, creditedLeaves)
    result.map {
      case Left(reason) => BadRequest(reason)
      case Right(_) => Ok
    }
  }
}
