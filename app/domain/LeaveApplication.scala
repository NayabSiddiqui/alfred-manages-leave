package domain

import org.joda.time.DateTime

case class LeaveApplication(id: String, days: List[DateTime], halfDayLeaves: Boolean)
