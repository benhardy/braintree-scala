package com.braintreegateway.testhelpers

import java.util.{TimeZone, Calendar}
import com.braintreegateway.util.NodeWrapper
import java.text.SimpleDateFormat

/**
 * Helpers to decrease clutter on Calendar operations.
 */
object CalendarHelper {

  def now = Calendar.getInstance


  class DateDelta(unitCount:Int) {
    def days = TimeDelta(Calendar.DAY_OF_MONTH, unitCount)
    def hours = TimeDelta(Calendar.HOUR_OF_DAY, unitCount)
    def months = TimeDelta(Calendar.MONTH, unitCount)
  }

  implicit def dateDelta(num:Int) = new DateDelta(num)

  implicit def dateDelta(num:Integer) = new DateDelta(num)

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
    def year = calendar.get(Calendar.YEAR)
    def month = calendar.get(Calendar.MONTH)
    def day = calendar.get(Calendar.DAY_OF_MONTH)
    def hour = calendar.get(Calendar.HOUR_OF_DAY)
    def minute = calendar.get(Calendar.MINUTE)
    def second = calendar.get(Calendar.SECOND)
    def timeZone = calendar.getTimeZone
    def in(timeZone:TimeZone): Calendar = {
      val newCal = copyOf(calendar)
      newCal.setTimeZone(timeZone)
      newCal
    }
  }

  def copyOf(calendar: Calendar): Calendar = {
    calendar.clone.asInstanceOf[Calendar]
  }

  implicit def calendarOperations(c:Calendar) = CalendarOperations(c)

  case class TimeDelta(calendarUnit:Int, quantity: Int) {
    def before(when: Calendar): Calendar = when - this
    def after(when: Calendar): Calendar = when + this
  }

  val UTC = "UTC"

  def date(dateString: String) = {
    getCalendar(dateString, NodeWrapper.DATE_FORMAT, UTC)
  }

  def date(year:Int, month:Int, day:Int) = {
    val calendar = Calendar.getInstance
    calendar.set(year, month, day)
    calendar
  }

  def dateTime(dateString: String, timeZoneName: String = UTC) = {
    getCalendar(dateString, NodeWrapper.DATE_TIME_FORMAT, timeZoneName)
  }

  def getCalendar(dateString: String,  dateTimeFormat: String, timeZoneName: String) = {
    val dateFormat = new SimpleDateFormat(dateTimeFormat)
    dateFormat.setTimeZone(TimeZone.getTimeZone(timeZoneName))
    val disbursementCalendar = Calendar.getInstance(TimeZone.getTimeZone(timeZoneName))
    disbursementCalendar.setTime(dateFormat.parse(dateString))
    disbursementCalendar
  }
}
