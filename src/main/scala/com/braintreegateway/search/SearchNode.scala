package com.braintreegateway.search

class SearchNode[P <: SearchRequest[P]](nodeName: String, parent: P) {

  protected def assembleCriteria(operation: String, value: String): P = {
    parent.addCriteria(this.nodeName, new SearchCriteria(operation, defaultWithEmptyString(value)))
  }

  protected def assembleMultiValueCriteria(items: List[_]): P = {
    parent.addMultipleValueCriteria(this.nodeName, new SearchCriteria(items))
  }

  private def defaultWithEmptyString(value: String): String = {
    if (value == null) {
      ""
    }
    else {
      value
    }
  }
}