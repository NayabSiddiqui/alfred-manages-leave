package service

import org.joda.time.{DateTime, DateTimeConstants, Days}
import org.scalatestplus.play.PlaySpec

class TeamCalendarServiceSpec extends PlaySpec {

  val calendarService = new TeamCalendarService()

  "Team Calendar Service" should {
    "return working days between two given dates" in {
      val from = new DateTime(2017, 6, 12, 0, 0)
      val to = new DateTime(2017, 6, 15, 0, 0)

      val workingDays = calendarService.getWorkingDaysBetween(from, to)

      workingDays.length mustBe 4
      workingDays(0) mustBe from
      workingDays(1) mustBe from.plusDays(1)
      workingDays(2) mustBe from.plusDays(2)
      workingDays(3) mustBe from.plusDays(3)
    }

    "return working days between two given dates excluding weekends" in {
      val from = new DateTime(2017, 6, 15, 0, 0)
      val to = new DateTime(2017, 6, 21, 0, 0)

      val workingDays = calendarService.getWorkingDaysBetween(from, to)

      workingDays.length mustBe 5
      workingDays(0) mustBe from
      workingDays(1) mustBe from.plusDays(1)
      workingDays(2) mustBe from.plusDays(4)
      workingDays(3) mustBe from.plusDays(5)
      workingDays(4) mustBe from.plusDays(6)
    }
  }
}


