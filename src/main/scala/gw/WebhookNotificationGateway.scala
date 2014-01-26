package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.exceptions.InvalidSignatureException
import net.bhardy.braintree.scala.org.apache.commons.codec.binary.Base64
import net.bhardy.braintree.scala.util.Crypto
import net.bhardy.braintree.scala.util.NodeWrapperFactory
import net.bhardy.braintree.scala.WebhookNotification

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