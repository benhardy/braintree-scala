package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.SettlementBatchSummary
import net.bhardy.braintree.scala.SettlementBatchSummaryRequest
import net.bhardy.braintree.scala.util.Http
import java.util.Calendar

class SettlementBatchSummaryGateway(http: Http) {

  def generate(settlementDate: Calendar): Result[SettlementBatchSummary] = {
    val request = new SettlementBatchSummaryRequest
    request.settlementDate(settlementDate)
    doGenerate(request)
  }

  def generate(settlementDate: Calendar, groupByCustomField: String): Result[SettlementBatchSummary] = {
    val request = new SettlementBatchSummaryRequest
    request.settlementDate(settlementDate)
    request.groupByCustomField(groupByCustomField)
    doGenerate(request)
  }

  private def doGenerate(request: SettlementBatchSummaryRequest): Result[SettlementBatchSummary] = {
    val node = http.post("/settlement_batch_summary", request)
    Result.settlementBatchSummary(node)
  }
}