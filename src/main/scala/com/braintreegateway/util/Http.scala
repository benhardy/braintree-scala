package com.braintreegateway.util

import com.braintreegateway.gw.Configuration
import com.braintreegateway.Request
import com.braintreegateway.exceptions._
import javax.net.ssl._
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.util.zip.GZIPInputStream
import com.braintreegateway.util.Http.RequestMethod
import xml.Elem

object Http {
  def throwExceptionIfErrorStatusCode(statusCode: Int, message: Option[String] = None) {

    if (isErrorCode(statusCode)) {
      statusCode match {
        case 401 =>
          throw new AuthenticationException
        case 403 =>
          throw new AuthorizationException(message.map {QueryString.decode}.getOrElse(""))
        case 404 =>
          throw new NotFoundException
        case 426 =>
          throw new UpgradeRequiredException
        case 500 =>
          throw new ServerException
        case 503 =>
          throw new DownForMaintenanceException
        case _ =>
          throw new UnexpectedException("Unexpected HTTP_RESPONSE " + statusCode)
      }
    }
  }

  private def isErrorCode(responseCode: Int): Boolean = {
    responseCode != 200 && responseCode != 201 && responseCode != 422
  }

  sealed trait RequestMethod

  object RequestMethod {
    case object DELETE extends RequestMethod
    case object GET extends RequestMethod
    case object POST extends RequestMethod
    case object PUT extends RequestMethod
  }

  val DummyNode = new SimpleNodeWrapper( <none/>.toString)
}

class Http(authorizationHeader: String, baseMerchantURL: String, certificateFilenames: List[String], version: String) {

  def delete(url: String) {
    httpRequest(RequestMethod.DELETE, url)
  }

  def get(url: String): NodeWrapper = {
    httpRequest(RequestMethod.GET, url)
  }

  def post(url: String): NodeWrapper = {
    httpRequest(RequestMethod.POST, url)
  }

  def post(url: String, request: Request): NodeWrapper = {
    httpRequest(RequestMethod.POST, url, request.toXml)
  }

  def put(url: String): NodeWrapper = {
    httpRequest(RequestMethod.PUT, url)
  }

  def put(url: String, request: Request): NodeWrapper = {
    httpRequest(RequestMethod.PUT, url, request.toXml)
  }

  private def httpRequest(requestMethod: Http.RequestMethod, url: String, postBody: Option[Elem] = None): NodeWrapper = {
    try {
      val connection = connectionSetup(requestMethod, url)
      postBody.map { writePostBody(connection) }

      Http.throwExceptionIfErrorStatusCode(connection.getResponseCode)
      val response = fetchResponse(connection)

      if (requestMethod == RequestMethod.DELETE) {
        Http.DummyNode
      } else {
        NodeWrapperFactory.create(response)
      }
   }
    catch {
      case e: IOException => {
        throw new UnexpectedException(e.getMessage, e)
      }
    }
  }

  def connectionSetup(requestMethod: Http.RequestMethod, url: String): HttpURLConnection = {
    buildConnection(requestMethod, url) match {
      case secureConnection: HttpsURLConnection => {
        secureConnection.setSSLSocketFactory(getSSLSocketFactory)
        secureConnection
      }
      case other => other
    }
  }

  def writePostBody(connection: HttpURLConnection)(body: Elem) {
    connection.getOutputStream.write(body.toString.getBytes("UTF-8"))
    connection.getOutputStream.close
  }

  def fetchResponse(connection: HttpURLConnection): String = {
    val isFailed = connection.getResponseCode == 422
    val streamSource = if (isFailed) connection.getErrorStream else connection.getInputStream

    val isCompressed = "gzip".equalsIgnoreCase(connection.getContentEncoding)
    val responseStream = if (isCompressed) new GZIPInputStream(streamSource) else streamSource

    val xml = StringUtils.inputStreamToString(responseStream)
    responseStream.close
    xml
  }

  private def getSSLSocketFactory: SSLSocketFactory = {
    try {
      val keyStore = KeyStore.getInstance(KeyStore.getDefaultType)
      keyStore.load(null)
      for (certificateFilename <- certificateFilenames) {
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        val certStream: InputStream = classOf[Http].getClassLoader.getResourceAsStream(certificateFilename)
        val cert: Certificate = cf.generateCertificate(certStream)
        keyStore.setCertificateEntry(certificateFilename, cert)
      }
      val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
      kmf.init(keyStore, null)
      val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
      tmf.init(keyStore)
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(kmf.getKeyManagers, tmf.getTrustManagers, SecureRandom.getInstance("SHA1PRNG"))
      sslContext.getSocketFactory
    }
    catch {
      case e: Exception => {
        throw new UnexpectedException(e.getMessage, e)
      }
    }
  }

  private def buildConnection(requestMethod: Http.RequestMethod, urlString: String): HttpURLConnection = {
    val url = new URL(baseMerchantURL + urlString)
    val connection = url.openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod(requestMethod.toString)
    connection.addRequestProperty("Accept", "application/xml")
    connection.addRequestProperty("User-Agent", "Braintree Java " + version)
    connection.addRequestProperty("X-ApiVersion", Configuration.apiVersion)
    connection.addRequestProperty("Authorization", authorizationHeader)
    connection.addRequestProperty("Accept-Encoding", "gzip")
    connection.addRequestProperty("Content-Type", "application/xml")
    connection.setDoOutput(true)
    connection.setReadTimeout(60000)
    connection
  }
}