package controllers

import java.util.Date

import org.joda.time.DateTime
import org.json4s.DefaultFormats
import org.json4s.ext.JodaTimeSerializers
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import service.{EmployeeService, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmployeeControllerSpec extends PlaySpec with Results with MockitoSugar with ScalaFutures {

  lazy val employeeService = mock[EmployeeService]
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  "EmployeeController" should {
    "return CREATED after successfully registering a new Employee" in {
      val email = "coleson@shield.com"
      val firstName = "Phillip"
      val lastName = "Coulson"
      when(employeeService.registerEmployee(email, firstName, lastName)).thenReturn({
        Future {
          Right(Success())
        }
      })

      val requestBody: JsObject = Json.obj(
        "email" -> email,
        "firstName" -> firstName,
        "lastName" -> lastName
      )

      val request = FakeRequest("POST", "/api/employees")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      val controller = new EmployeeController(employeeService)

      val result = controller.registerEmployee()(request)
      whenReady(result) {
        r => contentAsString(result) mustBe empty
          r.header.status mustBe CREATED
      }
    }

    "return bad request when trying to register an already registered employee" in {
      val email = "coleson@shield.com"
      val firstName = "Phillip"
      val lastName = "Coulson"
      when(employeeService.registerEmployee(email, firstName, lastName)).thenReturn({
        Future {
          Left("Employee is already registered")
        }
      })

      val requestBody: JsObject = Json.obj(
        "email" -> email,
        "firstName" -> firstName,
        "lastName" -> lastName
      )

      val request = FakeRequest("POST", "/api/employees")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      val controller = new EmployeeController(employeeService)

      val result = controller.registerEmployee()(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe "Employee is already registered"
        r.header.status mustBe BAD_REQUEST
      }
    }

    "return bad request when trying to apply leaves for a non-registered employee" in {
      val unregisteredEmail = "coleson@shield.com"
      val from = new DateTime()
      val to = from.plusDays(2)
      val requestBody: JsObject = Json.obj(
        "from" -> from.toString,
        "to" -> to.toString,
        "isHalfDay" -> true
      )
      val request = FakeRequest("POST", s"/api/employees/${unregisteredEmail}/leaves")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      when(employeeService.applyHalfDayLeaves(any[String], any[DateTime], any[DateTime])).thenReturn({
        Future {
          Left("Employee is not registered")
        }
      })

      val controller = new EmployeeController(employeeService)

      val result = controller.applyLeaves(unregisteredEmail)(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe "Employee is not registered"
        r.header.status mustBe BAD_REQUEST
      }
    }

    "return OK when applying leaves employee" in {
      val unregisteredEmail = "coleson@shield.com"
      val from = new DateTime()
      val to = from.plusDays(2)
      val requestBody: JsObject = Json.obj(
        "from" -> from.toString,
        "to" -> to.toString,
        "isHalfDay" -> true
      )
      val request = FakeRequest("POST", s"/api/employees/${unregisteredEmail}/leaves")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      when(employeeService.applyHalfDayLeaves(any[String], any[DateTime], any[DateTime])).thenReturn({
        Future {
          Right(Success())
        }
      })

      val controller = new EmployeeController(employeeService)

      val result = controller.applyLeaves(unregisteredEmail)(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe empty
        r.header.status mustBe OK
      }
    }

    "return OK when crediting the leaves for an employee" in {
      val email = "coleson@shield.com"
      val creditedLeaves = 12.5f

      val requestBody: JsObject = Json.obj(
        "creditedLeaves" -> creditedLeaves.toString
      )
      val request = FakeRequest("POST", s"/api/employees/${email}/leave-balance")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      when(employeeService.creditLeaves(email,creditedLeaves))
        .thenReturn({
          Future{
            Right(Success())
          }
        })

      val controller = new EmployeeController(employeeService)

      val result = controller.creditLeaves(email)(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe empty
        r.header.status mustBe OK
      }
    }
  }
}


