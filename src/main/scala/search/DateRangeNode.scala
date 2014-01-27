package net.bhardy.braintree.scala.search

import java.util.Calendar

class DateRangeNode[P <: SearchRequest[P]](nodeName: String, parent: P) extends SearchNode[P](nodeName, parent) {

  def between(min: Calendar, max: Calendar): P = {
    this greaterThanOrEqualTo min
    this lessThanOrEqualTo max
    parent
  }

  // TODO see if it makes sense to use nice operators here
  def greaterThanOrEqualTo (min: Calendar): P = {
    parent.addRangeCriteria(nodeName, new SearchCriteria("min", min))
    parent
  }

  def lessThanOrEqualTo (max: Calendar): P = {
    parent.addRangeCriteria(nodeName, new SearchCriteria("max", max))
    parent
  }
}