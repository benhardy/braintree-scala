package com.braintreegateway.testhelpers

import java.util.Calendar

/**
 * Helpers to decrease clutter on Calendar operations.
 */
trait CalendarHelper {

  case class DaysDelta(days:Int)

  class DateDelta(unitCount:Int) {
    def days = DaysDelta(unitCount)
  }

  implicit def dateDelta(num:Int) = new DateDelta(num)

  case class CalendarOperations(calendar:Calendar) {
    def + (delta: DaysDelta): Calendar = {
      val newCal = calendar.clone.asInstanceOf[Calendar] // TODO find less horrendous way to do this
      newCal.add(Calendar.DAY_OF_MONTH, delta.days)
      newCal
    }
    def - (delta: DaysDelta): Calendar = this + ( - delta.days).days
  }

  implicit def calendarOperations(c:Calendar) = CalendarOperations(c)

}
