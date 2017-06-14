package controllers

import domain.{EventAppliedSuccessfully, LeaveApplication, LeaveSummary}
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
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmployeeControllerSpec extends PlaySpec with Results with MockitoSugar with ScalaFutures {

  lazy val employeeService = mock[EmployeeService]
  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  "EmployeeController" should {
    "return CREATED after successfully registering a new Employee" in {
      val id = "coleson"
      val email = "coulson@shield.com"
      val givenName = "Phillip Coulson"
      when(employeeService.registerEmployee(id, email, givenName)).thenReturn({
        Future {
          Right(Success())
        }
      })

      val requestBody: JsObject = Json.obj(
        "id" -> id,
        "email" -> email,
        "givenName" -> givenName
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
      val id = "coleson"
      val email = "coulson@shield.com"
      val givenName = "Phillip Coulson"
      when(employeeService.registerEmployee(id, email, givenName)).thenReturn({
        Future {
          Left("Employee is already registered")
        }
      })

      val requestBody: JsObject = Json.obj(
        "id" -> id,
        "email" -> email,
        "givenName" -> givenName
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
      val unregisteredId = "coleson@shield.com"
      val from = new DateTime()
      val to = from.plusDays(2)
      val requestBody: JsObject = Json.obj(
        "from" -> from.toString,
        "to" -> to.toString,
        "isHalfDay" -> true
      )
      val request = FakeRequest("POST", s"/api/employees/${unregisteredId}/leaves")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      when(employeeService.applyHalfDayLeaves(any[String], any[DateTime], any[DateTime])).thenReturn({
        Future {
          Left("Employee is not registered")
        }
      })

      val controller = new EmployeeController(employeeService)

      val result = controller.applyLeaves(unregisteredId)(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe "Employee is not registered"
        r.header.status mustBe BAD_REQUEST
      }
    }

    "return OK when applying leaves employee" in {
      val unregisteredId = "coleson@shield.com"
      val from = new DateTime()
      val to = from.plusDays(2)
      val requestBody: JsObject = Json.obj(
        "from" -> from.toString,
        "to" -> to.toString,
        "isHalfDay" -> true
      )
      val request = FakeRequest("POST", s"/api/employees/${unregisteredId}/leaves")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      when(employeeService.applyHalfDayLeaves(any[String], any[DateTime], any[DateTime])).thenReturn({
        Future {
          Right(Success())
        }
      })

      val controller = new EmployeeController(employeeService)

      val result = controller.applyLeaves(unregisteredId)(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe empty
        r.header.status mustBe OK
      }
    }

    "return OK when crediting the leaves for an employee" in {
      val id = "coleson@shield.com"
      val creditedLeaves = 12.5f

      val requestBody: JsObject = Json.obj(
        "creditedLeaves" -> creditedLeaves.toString
      )
      val request = FakeRequest("POST", s"/api/employees/${id}/leave/balance")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")
        .withBody(requestBody)

      when(employeeService.creditLeaves(id, creditedLeaves))
        .thenReturn({
          Future {
            Right(Success())
          }
        })

      val controller = new EmployeeController(employeeService)

      val result = controller.creditLeaves(id)(request)
      whenReady(result) { r =>
        contentAsString(result) mustBe empty
        r.header.status mustBe OK
      }
    }

    "return OK with leave balance for a given employee" in {
      val id = "coleson@shield.com"
      val leaveBalance = 12.5f
      when(employeeService.getLeaveBalance(id))
        .thenReturn({
          Future {
            Right(leaveBalance)
          }
        })

      val controller = new EmployeeController(employeeService)

      val request = FakeRequest("GET", s"/api/employees/${id}/leave/balance")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")

      val result = controller.getLeaveBalance(id)(request)
      whenReady(result) { r =>
        contentAsString(result).toFloat mustBe 12.5f
        r.header.status mustBe OK
      }
    }

    "should return the leave summary of a given employee" in {
      val id = "vodoochild"
      val request = FakeRequest("GET", s"/api/employees/${id}/leaves")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")

      val today = new DateTime().withTimeAtStartOfDay()
      val applications = List[LeaveApplication](
        new LeaveApplication("myApplication1", List[DateTime](today, today plusDays 1)),
        new LeaveApplication("myApplication2", List[DateTime](today plusDays 5, today plusDays 6, today plusDays 7))
      )
      val leaveSummary = new LeaveSummary(applications, 9.5f)

      when(employeeService.getLeaveSummary(id))
        .thenReturn({
          Future {
            Right(leaveSummary)
          }
        })

      val controller = new EmployeeController(employeeService)
      val result = controller.getLeaveSummary(id)(request)
      whenReady(result) { r =>
        Json.parse(contentAsString(result)) mustBe Json.toJson(leaveSummary)
        r.header.status mustBe OK
      }
    }

    "should cancel a leave application" in {
      val id = "vodoochild"
      val applicationId = "vodooLeave"
      val request = FakeRequest("DELETE", s"/api/employees/${id}/leaves/${applicationId}")
        .withHeaders(HOST -> "localhost",
          CONTENT_TYPE -> "application/json")

      when(employeeService.cancelLeaveApplication(id, applicationId))
        .thenReturn({
          Future {
            Right(Success())
          }
        })

      val controller = new EmployeeController(employeeService)
      val result = controller.cancelLeaveApplication(id, applicationId)(request)
      whenReady(result) { r =>
        r.header.status mustBe OK
      }
    }
  }
}


