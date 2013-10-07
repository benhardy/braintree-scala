package com.braintreegateway.gw

import com.braintreegateway.exceptions.InvalidSignatureException
import com.braintreegateway.org.apache.commons.codec.binary.Base64
import com.braintreegateway.util.Crypto
import com.braintreegateway.util.NodeWrapperFactory
import com.braintreegateway.WebhookNotification

class WebhookNotificationGateway(configuration: Configuration) {

  def parse(signature: String, payload: String): WebhookNotification = {
    validateSignature(signature, payload)
    val xmlPayload = new String(Base64.decodeBase64(payload))
    val node = NodeWrapperFactory.create(xmlPayload)
    new WebhookNotification(node)
  }

  private def validateSignature(signature: String, payload: String) {
    val matchingSignature: String = signature.split("&").toStream.
      map { _.split("\\|") }.
      find { parts =>
        parts.length >= 2 && this.configuration.publicKey == parts(0)
      }.
      map { _(1) }.
      getOrElse("")

    val crypto = new Crypto
    val computedSignature = crypto.hmacHash(configuration.privateKey, payload)
    if (!crypto.secureCompare(computedSignature, matchingSignature)) {
      throw new InvalidSignatureException
    }
  }

  def verify(challenge: String): String = {
    publicKeySignaturePair(challenge)
  }

  private def publicKeySignaturePair(stringToSign: String): String = {
    String.format("%s|%s", configuration.publicKey, new Crypto().hmacHash(configuration.privateKey, stringToSign))
  }
}