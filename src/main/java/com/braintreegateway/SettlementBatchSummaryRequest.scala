package com.braintreegateway

import java.text.SimpleDateFormat
import java.util.Calendar

object SettlementBatchSummaryRequest {
  def dateString(settlementDate: Calendar): String = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    dateFormat.setCalendar(settlementDate)
    dateFormat.format(settlementDate.getTime)
  }
}

class SettlementBatchSummaryRequest extends BaseRequest {

  import SettlementBatchSummaryRequest.dateString

  private var settlementDate: Calendar = null
  private var groupByCustomField: String = null

  def settlementDate(settlementDate: Calendar): SettlementBatchSummaryRequest = {
    this.settlementDate = settlementDate
    this
  }

  def groupByCustomField(groupByCustomField: String): SettlementBatchSummaryRequest = {
    this.groupByCustomField = groupByCustomField
    this
  }

  override def toXmlString: String = {
    buildRequest("settlement-batch-summary").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
        addElement("settlement-date", dateString(settlementDate)).
        addElementIf(groupByCustomField != null, "group-by-custom-field", groupByCustomField)
  }
}