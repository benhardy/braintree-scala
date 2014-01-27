package net.bhardy.braintree.scala

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

  private var settlementDate: Option[Calendar] = None
  private var groupByCustomField: Option[String] = None

  def settlementDate(settlementDate: Calendar): SettlementBatchSummaryRequest = {
    this.settlementDate = Option(settlementDate)
    this
  }

  def groupByCustomField(groupByCustomField: String): SettlementBatchSummaryRequest = {
    this.groupByCustomField = Option(groupByCustomField)
    this
  }

  override val xmlName = "settlement-batch-summary"

  protected def buildRequest(root: String): RequestBuilder = {
    import SettlementBatchSummaryRequest.dateString

    new RequestBuilder(root).
        addElement("settlement-date", settlementDate map dateString).
        addElement("group-by-custom-field", groupByCustomField)
  }
}