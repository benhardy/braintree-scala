package net.bhardy.braintree.scala.util

import xml._
import xml.{Null => NoAttributes}

/**
 * DSL for legibly constructing XML Elems with variable tag name
 * for use internally in this library. (As opposed to using XML
 * literals when tag names are fixed).
 *
 * example
 *
 * import XmlUtil
 *
 * val child = XmlUtil tag "name" content "barry"
 * val parent = XmlUtil tag "person" content child
 * val list = XmlUtil tag "people" withType "array" content child
 */
object XmlUtil {

  abstract class ContentSpec {
    private[util] def maker: (Seq[Node]) => Elem

    def apply(nodes: Iterable[Node]): Elem = maker(nodes.toSeq)

    def apply(nodes: Node*): Elem = maker(nodes)

    def apply(content: String): Elem = maker(new Text(content))
  }

  trait ToContent {
    def content: ContentSpec
  }

  trait AfterTag extends ToContent {
    def withType(typeString: String): ToContent
  }

  def tag(tag: String): AfterTag = new AfterTag {
    def content = new ContentSpec {
      def maker = wrapXmlInXmlTag(tag)
    }

    def withType(typeString: String) = new ToContent {
      def content = new ContentSpec {
        def maker = wrapXmlInXmlTag(tag, Some(typeString))
      }
    }

    def wrapXmlInXmlTag(tagName: String, tagType: Option[String] = None)(content: Node*): Elem = {
      val attributes = tagType.map {
        typeString =>
          new UnprefixedAttribute("type", typeString, NoAttributes)
      } getOrElse NoAttributes

      new Elem(null, xmlEscape(tagName), attributes, TopScope, true, content: _*)
    }

    // TODO this needs to begone.
    private def xmlEscape(input: String): String = {
      input.replaceAll("&", "&amp;").replaceAll("<", "&lt;").
        replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;")
    }
  }
}
