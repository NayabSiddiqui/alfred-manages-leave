package service

import org.joda.time.{DateTime, DateTimeConstants, Days}

case class TeamCalendarService(){
  def getWorkingDaysBetween(from: DateTime, to: DateTime): List[DateTime] = {
    val totalNumberOfDays = Days.daysBetween(from, to).getDays
    val allDates = for (day <- 0 to totalNumberOfDays) yield from.plusDays(day).withTimeAtStartOfDay()
    allDates.filter(date =>
      date.getDayOfWeek != DateTimeConstants.SATURDAY && date.getDayOfWeek != DateTimeConstants.SUNDAY)
      .toList
  }

}
