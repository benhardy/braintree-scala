package com.braintreegateway.testhelpers

import java.util.Calendar

/**
 * Helpers to decrease clutter on Calendar operations.
 */
trait CalendarHelper {

  case class TimeDelta(calendarUnit:Int, quantity: Int)

  class DateDelta(unitCount:Int) {
    def days = TimeDelta(Calendar.DAY_OF_MONTH, unitCount)
    def hours = TimeDelta(Calendar.HOUR_OF_DAY, unitCount)
  }

  implicit def dateDelta(num:Int) = new DateDelta(num)

  case class CalendarOperations(calendar:Calendar) {
    def + (delta: TimeDelta): Calendar = {
      val newCal = copyOf(calendar)
      newCal.add(delta.calendarUnit, delta.quantity)
      newCal
    }
    def - (delta: TimeDelta): Calendar = {
      val newCal = copyOf(calendar)
      newCal.add(delta.calendarUnit, - delta.quantity)
      newCal
    }
  }

  def copyOf(calendar: Calendar): Calendar = {
    calendar.clone.asInstanceOf[Calendar]
  }

  implicit def calendarOperations(c:Calendar) = CalendarOperations(c)

}
