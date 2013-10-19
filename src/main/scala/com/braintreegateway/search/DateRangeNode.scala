package com.braintreegateway.search

import java.util.Calendar
import com.braintreegateway.SearchRequest

class DateRangeNode[P <: SearchRequest](nodeName: String, parent: P) extends SearchNode[P](nodeName, parent) {

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