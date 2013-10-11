package com.braintreegateway.gw

import com.braintreegateway.SettlementBatchSummary
import com.braintreegateway.SettlementBatchSummaryRequest
import com.braintreegateway.util.Http
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