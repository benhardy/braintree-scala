package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class SearchNode[P <: SearchRequest](nodeName: String, parent: P) {

  protected def assembleCriteria(operation: String, value: String): P = {
    parent.addCriteria(this.nodeName, new SearchCriteria(operation, defaultWithEmptyString(value)))
    parent
  }

  protected def assembleMultiValueCriteria(items: List[_]): P = {
    parent.addMultipleValueCriteria(this.nodeName, new SearchCriteria(items))
    parent
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