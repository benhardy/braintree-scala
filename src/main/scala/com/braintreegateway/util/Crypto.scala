package com.braintreegateway.util

import com.braintreegateway.org.apache.commons.codec.binary.Hex

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

class Crypto {
  def hmacHash(privateKey: String, content: String): String = {
    try {
      val signingKey = new SecretKeySpec(sha1Bytes(privateKey), "SHA1")
      val mac = Mac.getInstance("HmacSHA1")
      mac.init(signingKey)
      val rawMac = mac.doFinal(content.getBytes("UTF-8"))
      val hexBytes = new Hex().encode(rawMac)
      val hash = new String(hexBytes, "ISO-8859-1")
      hash
    }
    catch {
      case e: Exception => {
        throw new RuntimeException(e)
      }
    }
  }

  def secureCompare(left: String, right: String): Boolean = {
    if (left == null || right == null || (left.length != right.length)) {
      false
    } else {
      val leftBytes = left.getBytes
      val rightBytes = right.getBytes
      var result = 0
      0 until left.length foreach { i =>
        result = result | leftBytes(i) ^ rightBytes(i)
      }
      result == 0
    }
  }

  def sha1Bytes(string: String): Array[Byte] = {
    try {
      val md = MessageDigest.getInstance("SHA1")
      md.update(string.getBytes("UTF-8"))
      md.digest
    }
    catch {
      case e: Exception => {
        throw new RuntimeException(e)
      }
    }
  }
}