package net.bhardy.braintree.scala.search

import scala.math.BigDecimal

class RangeNode[T <: SearchRequest[T]](nodeName: String, parent: T) extends SearchNode[T](nodeName, parent) {

  def between(min: BigDecimal, max: BigDecimal): T = {
    between(min.toString, max.toString)
  }

  def between(min: Int, max: Int): T = {
    between(String.valueOf(min), String.valueOf(max))
  }

  def between(min: String, max: String): T = {
    greaterThanOrEqualTo(min)
    lessThanOrEqualTo(max)
  }

  def greaterThanOrEqualTo(min: BigDecimal): T = {
    greaterThanOrEqualTo(min.toString)
  }

  def greaterThanOrEqualTo(min: Int): T = {
    greaterThanOrEqualTo(String.valueOf(min))
  }

  def greaterThanOrEqualTo(min: String): T = {
    parent.addRangeCriteria(nodeName, new SearchCriteria("min", min))
  }

  def lessThanOrEqualTo(max: BigDecimal): T = {
    lessThanOrEqualTo(max.toString)
  }

  def lessThanOrEqualTo(max: Int): T = {
    lessThanOrEqualTo(String.valueOf(max))
  }

  def lessThanOrEqualTo(max: String): T = {
    parent.addRangeCriteria(nodeName, new SearchCriteria("max", max))
  }

  def is(value: BigDecimal): T = {
    is(value.toString)
  }

  def is(value: Int): T = {
    is(String.valueOf(value))
  }

  def is(value: String): T = {
    assembleCriteria("is", value)
  }
}