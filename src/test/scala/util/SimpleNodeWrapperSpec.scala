package net.bhardy.braintree.scala.util

import scala.math.BigDecimal
import java.util.Calendar
import java.util.TimeZone
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import net.bhardy.braintree.scala.testhelpers.CalendarHelper._


@RunWith(classOf[JUnitRunner])
class SimpleNodeWrapperSpec extends FunSpec with MustMatchers {

  val XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"

  implicit def textNode(string: String) = TextNode(string)

  describe("parse") {

    it("getsHashFromSimpleXML") {
      val xml = <parent>
        <child>value</child>
      </parent>
      val actual = SimpleNodeWrapper.parse(xml.toString)
      actual must be === SimpleNodeWrapper(name = "parent", content = List(
        SimpleNodeWrapper(name = "child", content = List("value"))
      ))
    }

    it("handleXmlCharactersCorrectly") {
      val xml = <credit-card>
        <bin>510510</bin>
        <cardholder-name>Special Chars &lt; &gt; &amp; &quot;'</cardholder-name>
      </credit-card>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findString("cardholder-name") must be === "Special Chars <>&\"'"
    }

    it("parsingSimpleEmptyElement") {
      val xml = <foo/>
      val node = SimpleNodeWrapper.parse(XML_HEADER + xml.toString)
      node must be === SimpleNodeWrapper(name = "foo")
    }
    it("parsingElementWithEmptyContent") {
      val xml = <foo></foo>
      val node = SimpleNodeWrapper.parse(XML_HEADER + xml.toString)
      node must be === SimpleNodeWrapper(name = "foo", content = Nil)
    }

    it("parsingSimpleNilAttributeElement") {
      val xml = <foo nil='true'/>
      val node = SimpleNodeWrapper.parse(XML_HEADER + xml.toString)
      node must be === SimpleNodeWrapper(name = "foo", attributes = Map("nil" -> "true"), content = Nil)
    }

    it("parsingFullXmlDoc") {
      val xml = <add-on>
        <amount>100.00</amount>
        <foo nil='true'></foo>
      </add-on>
      val node = SimpleNodeWrapper.parse(XML_HEADER + xml.toString)
      node must be === SimpleNodeWrapper(name = "add-on", content = List(
        SimpleNodeWrapper(name = "amount", content = List("100.00")),
        SimpleNodeWrapper(name = "foo", attributes = Map("nil" -> "true"), content = Nil)
      ))
    }

    it("parsingXmlWithListAtRoot") {
      val xml = <add-ons type="array">
        <add-on>
          <amount>100.00</amount>
        </add-on>
      </add-ons>

      val node = SimpleNodeWrapper.parse(XML_HEADER + xml.toString)

      node must be === SimpleNodeWrapper(name = "add-ons", attributes = Map("type" -> "array"), content = List(
        SimpleNodeWrapper(name = "add-on", content = List(
          SimpleNodeWrapper(name = "amount", content = List("100.00"))
        ))
      ))
    }

    it("parsingXmlWithNilValuesWithoutNilAttr") {
      val xml = <customer>
        <id>884969</id>
        <merchant-id>integration_merchant_id</merchant-id>
        <first-name nil="true"></first-name>
        <custom-fields>
        </custom-fields>
      </customer>
      val node = SimpleNodeWrapper.parse(XML_HEADER + xml.toString)
      node must be === SimpleNodeWrapper(name = "customer", content = List(
        SimpleNodeWrapper(name = "id", content = List("884969")),
        SimpleNodeWrapper(name = "merchant-id", content = List("integration_merchant_id")),
        SimpleNodeWrapper(name = "first-name", attributes = Map("nil" -> "true"), content = Nil),
        SimpleNodeWrapper(name = "custom-fields")
      ))
    }

    it("moreNestedXml") {
      val xml = <toplevel>
        <foo type='array'>
          <bar>
            <greeting>hi</greeting>
            <salutation>bye</salutation>
          </bar>
          <bar>
            <greeting>hello</greeting>
          </bar>
        </foo>
      </toplevel>

      val node = SimpleNodeWrapper.parse(xml.toString)

      node must be === SimpleNodeWrapper(name = "toplevel", content = List(
        SimpleNodeWrapper(name = "foo", attributes = Map("type" -> "array"), content = List(
          SimpleNodeWrapper(name = "bar", content = List(
            SimpleNodeWrapper(name = "greeting", content = List("hi")),
            SimpleNodeWrapper(name = "salutation", content = List("bye"))
          )),
          SimpleNodeWrapper(name = "bar", content = List(
            SimpleNodeWrapper(name = "greeting", content = List("hello"))
          ))
        ))
      ))
    }
  }

  describe("findString") {
    it("gets top level for dot") {
      val xml = <toplevel>bar</toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findString(".") must be === "bar"
    }

    it("finds strings") {
      val xml = <toplevel>
        <foo>bar</foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findString("foo") must be === "bar"
    }

    it("returns null for values with a 'nil' attribute set") {
      val xml = <toplevel>
        <foo nil='true'></foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findString("foo") must be === null
    }

    it("also returns null if item isn't found") {
      val xml = <toplevel>
        <foo>bar</foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findString("blah") must be === null
    }
  }

  describe("findDate") {
    it("finds dates") {
      val xml = <toplevel>
        <created-at type="date">2010-02-16</created-at>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      val expected = Calendar.getInstance
      expected.set(2010, 1, 16)
      val utc = TimeZone.getTimeZone("UTC")
      expected.setTimeZone(utc)
      val actual = node.findDate("created-at")
      actual.year must be === 2010
      actual.month must be === Calendar.FEBRUARY
      actual.day must be === 16
      actual.timeZone must be === utc
    }

    it("returns null if not found") {
      val xml = <toplevel>
        <foo>bar</foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findDate("created-at") must be === null
    }
  }

  describe("findDateTime") {
    it("returns dateTime types") {
      val xml = <toplevel>
        <created-at type="datetime">2010-02-16T16:32:07Z</created-at>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)

      val expected = Calendar.getInstance
      expected.setTimeZone(TimeZone.getTimeZone("UTC"))
      expected.set(2010, 1, 16, 16, 32, 7)
      expected.set(Calendar.MILLISECOND, 0)

      val actual = node.findDateTime("created-at")
      actual.year must be === 2010
      actual.month must be === Calendar.FEBRUARY
      actual.day must be === 16
      actual.hour must be === 16
      actual.minute must be === 32
      actual.second must be === 07
      actual.timeZone must be === TimeZone.getTimeZone("UTC")
    }

    it("returns null if not found") {
      val xml = <toplevel>
        <foo>bar</foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findDateTime("created-at") must be === null
    }
  }

  describe("findBigDecimal") {
    it("finds values by name") {
      val xml = <toplevel>
        <amount>12.59</amount>
      </toplevel>
      val response = SimpleNodeWrapper.parse(xml.toString)
      response.findBigDecimal("amount") must be === BigDecimal("12.59")
    }

    it("findBigDecimalWithNoMatchingElement") {
      val xml = <toplevel>
        <amount>12.59</amount>
      </toplevel>
      val response = SimpleNodeWrapper.parse(xml.toString)
      response.findBigDecimal("price") must be === null
    }
  }

  describe("findInteger") {
    it("returns found integers given a name") {
      val xml = <toplevel>
        <foo>4</foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findInteger("foo") must be === new Integer(4)
    }

    it("returns null if not found") {
      val xml = <toplevel>
        <foo>4</foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findInteger("blah") must be === null
    }
  }

  describe("findAll") {
    it("returns a list of matching items") {
      val xml = <toplevel>
        <foo type='array'>
          <bar>
            <greeting>hi</greeting>
          </bar>
          <bar>
            <greeting>hello</greeting>
          </bar>
        </foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      val nodes = node.findAll("foo/bar")
      nodes.size must be === 2
      nodes(0).findString("greeting") must be === "hi"
      nodes(1).findString("greeting") must be === "hello"
    }

    it("uses star for wildcard matching") {
      val xml = <toplevel>
        <foo type='array'>
          <bar>
            <greeting>hi</greeting>
          </bar>
          <bar>
            <greeting>hello</greeting>
          </bar>
        </foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      val nodes = node.findAll("foo/*")
      nodes.size must be === 2
      nodes(0).findString("greeting") must be === "hi"
      nodes(1).findString("greeting") must be === "hello"
    }

    it("returns empty list if nothing found") {
      val xml = <toplevel></toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString)
      node.findAll("foo/bar") must be('empty)
    }
  }

  describe("findFirst") {
    it("returns found items") {
      val xml = <toplevel>
        <foo type='array'>
          <bar>
            <greeting>hi</greeting>
          </bar>
          <bar>
            <greeting>hello</greeting>
          </bar>
        </foo>
      </toplevel>
      val node = SimpleNodeWrapper.parse(xml.toString).findFirst("foo/bar")
      node.findString("greeting") must be === "hi"
    }

    it("returns null if nothing found") {
      val xml = <toplevel></toplevel>
      SimpleNodeWrapper.parse(xml.toString).findFirst("foo/bar") must be === null
    }
  }

  describe("findFirstOpt") {
    it("returns found items in a Some") {
      val xml = <toplevel>
        <foo type='array'>
          <bar>
            <greeting>hi</greeting>
          </bar>
          <bar>
            <greeting>hello</greeting>
          </bar>
        </foo>
      </toplevel>
      val doc = SimpleNodeWrapper.parse(xml.toString)
      val res = for {
        node <- doc.findFirstOpt("foo/bar")
        greet <- node.findStringOpt("greeting")
      } yield greet
      res must be === Some("hi")
    }

    it("returns None if nothing found") {
      val xml = <toplevel></toplevel>
      val doc = SimpleNodeWrapper.parse(xml.toString)
      doc.findFirstOpt("foo/bar") must be === None
    }
  }

  it("parameters") {
    val xml = <api-error-response>
      <params>
        <payment-method-token>99s6</payment-method-token>
        <id>invalid id</id>
        <plan-id>integration_trialless_plan</plan-id>
      </params>
    </api-error-response>
    val node = SimpleNodeWrapper.parse(xml.toString)
    val map = node.findFirst("params").getFormParameters
    map must be === Map("id" -> "invalid id", "payment_method_token" -> "99s6", "plan_id" -> "integration_trialless_plan")
  }

  it("nestsParameters") {
    val xml = <api-error-response>
      <ps>
        <child>
          <grandchild>sonny</grandchild>
        </child>
        <id>invalid id</id>
      </ps>
    </api-error-response>
    val node = SimpleNodeWrapper.parse(xml.toString)
    val map = node.findFirst("ps").getFormParameters
    map must be === Map("child[grandchild]" -> "sonny", "id" -> "invalid id")
  }
}