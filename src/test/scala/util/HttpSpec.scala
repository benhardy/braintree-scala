package net.bhardy.braintree.scala.util

import net.bhardy.braintree.scala.Environment
import java.io.File
import java.io.FileInputStream
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.junit.JUnitRunner
import net.bhardy.braintree.scala.gw.BraintreeGateway
import net.bhardy.braintree.scala.it.GatewayIntegrationSpec

@RunWith(classOf[JUnitRunner])
class HttpSpec extends GatewayIntegrationSpec with FunSpec with MustMatchers {

  describe("ssl Bad Certificate") {
    onGatewayIt("includes message about certificate problems in exception") {
      gateway =>
        try {
          startSSLServer
          intercept[Exception] {
            val http = new Http(gateway.authorizationHeader, "https://localhost:9443",
              Environment.SANDBOX.certificateFilenames, BraintreeGateway.VERSION)
            http.get("/")
          }.getMessage must include("Cert")
        }
        finally {
          stopSSLServer
        }
    }
  }

  private def startSSLServer {
    val fileName = StringUtils.getFullPathOfFile("script/httpsd.rb")
    new File(fileName).setExecutable(true)
    new ProcessBuilder(fileName, "/tmp/httpsd.pid").start.waitFor
  }

  private def stopSSLServer {
    val pid = StringUtils.inputStreamToString(new FileInputStream("/tmp/httpsd.pid"))
    new ProcessBuilder("kill", "-9", pid).start
  }
}