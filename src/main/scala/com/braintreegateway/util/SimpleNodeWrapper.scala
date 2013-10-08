package com.braintreegateway.util

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory
import java.io.StringReader
import java.util._
import java.util.regex.Matcher
import java.util.regex.Pattern
import scala.collection.JavaConversions._
import java.util

object SimpleNodeWrapper {
  def parse(xml: String): SimpleNodeWrapper = {
    try {
      val source = new InputSource(new StringReader(xml))
      val parser = saxParserFactory.newSAXParser
      val handler = new SimpleNodeWrapper.MapNodeHandler
      parser.parse(source, handler)
      handler.root
    }
    catch {
      case e: Exception => {
        throw new IllegalArgumentException(e.getMessage, e)
      }
    }
  }

  private val saxParserFactory: SAXParserFactory = SAXParserFactory.newInstance

  private object MapNodeHandler {
    private val NON_WHITE_SPACE: Pattern = Pattern.compile("\\S")
  }

  private class MapNodeHandler extends DefaultHandler {
    override def startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
      val node: SimpleNodeWrapper = new SimpleNodeWrapper(qName)

      for (i <- 0 until attributes.getLength) {
        node.attributes.put(attributes.getQName(i), attributes.getValue(i))
      }
      if ("true" == node.attributes.get("nil")) node.content.add(null)
      if (!stack.isEmpty) stack.peek.content.add(node)
      stack.push(node)
    }

    override def endElement(uri: String, localName: String, qName: String) {
      val pop: SimpleNodeWrapper = stack.pop
      if (stack.isEmpty) root = pop
    }

    override def characters(chars: Array[Char], start: Int, length: Int) {
      val value: String = new String(chars, start, length)
      val matcher: Matcher = MapNodeHandler.NON_WHITE_SPACE.matcher(value)
      if (value.length > 0 && matcher.find) {
        stack.peek.content.add(value)
      }
    }

    private val stack: Stack[SimpleNodeWrapper] = new Stack[SimpleNodeWrapper]
    var root: SimpleNodeWrapper = null
  }

}

class SimpleNodeWrapper(val name:String) extends NodeWrapper {

  private val attributes: Map[String, String] = new HashMap[String, String]
  private val content: List[AnyRef] = new LinkedList[AnyRef]

  def findAll(expression: String): List[NodeWrapper] = {
    val paths: Array[String] = expression.split("/")
    val tokens: LinkedList[String] = new LinkedList[String](paths.toList)
    val nodes: List[NodeWrapper] = new LinkedList[NodeWrapper]
    findAll(tokens, nodes)
    nodes
  }

  private def findAll(tokens: LinkedList[String], nodes: List[NodeWrapper]): Unit = {
    if (tokens.isEmpty) nodes.add(this)
    else {
      val first: String = tokens.getFirst
      if ("." == first) findAll(restOf(tokens), nodes)
      for (node <- childNodes) {
        if (("*" == first) || (first == node.name)) node.findAll(restOf(tokens), nodes)
      }
    }
  }

  private def findOpt(tokens: scala.collection.immutable.List[String]): Option[SimpleNodeWrapper] = {
    tokens match {
      case Nil => Some(this)
      case first :: rest => {
        if ("." == first) {
          findOpt(rest)
        } else {
          childNodes.
            find(node => (("*" == first) || (first == node.name))).
            flatMap(node => node.findOpt(rest))
        }
      }
    }
  }

  @deprecated // old behavior was null-based
  private def find(tokens: LinkedList[String]): SimpleNodeWrapper = {
    findOpt(tokens.toList).getOrElse(null)
  }

  @deprecated
  private def find(expression: String): SimpleNodeWrapper = {
    val paths: Array[String] = expression.split("/")
    val tokens: LinkedList[String] = new LinkedList[String](paths.toList)
    find(tokens)
  }

  private def restOf(tokens: LinkedList[String]): LinkedList[String] = {
    val newTokens: LinkedList[String] = new LinkedList[String](tokens)
    newTokens.removeFirst
    newTokens
  }

  @deprecated
  def findFirst(expression: String): NodeWrapper = {
    find(expression)
  }

  def findFirstOpt(expression: String): Option[NodeWrapper] = {
    Option(find(expression))
  }

  @deprecated
  def findString(expression: String): String = {
    val node: SimpleNodeWrapper = find(expression)
    if (node == null) null
    else node.stringValue
  }

  def findStringOpt(expression: String): Option[String] = {
    val node: SimpleNodeWrapper = find(expression)
    if (node == null) None
    else Some(node.stringValue)
  }

  private def stringValue: String = {
    if (content.size == 1 && content.get(0) == null) { null }
    else {
      val value: StringBuilder = new StringBuilder
      for (o <- content) {
        value.append(o.toString)
      }
      value.toString.trim
    }
  }

  def getElementName: String = {
    name
  }

  private def childNodes: List[SimpleNodeWrapper] = {
    val nodes: List[SimpleNodeWrapper] = new LinkedList[SimpleNodeWrapper]
    import scala.collection.JavaConversions._
    for (o <- content) {
      if (o.isInstanceOf[SimpleNodeWrapper]) {
        nodes.add(o.asInstanceOf[SimpleNodeWrapper])
      }
    }
    nodes
  }

  def getFormParameters: Map[String, String] = {
    val params: Map[String, String] = new HashMap[String, String]
    for (node <- childNodes) {
      node.buildParams("", params)
    }
    params
  }

  private def buildParams(prefix: String, params: Map[String, String]) {
    val newPrefix = if ("" == prefix)
      StringUtils.underscore(name)
    else
      prefix + "[" + StringUtils.underscore(name) + "]"
    if (childNodes.isEmpty) {
      params.put(newPrefix, stringValue)
    }
    else {
      for (childNode <- childNodes) {
        childNode.buildParams(newPrefix, params)
      }
    }
  }

  override def toString: String = {
    return "<" + name + (if (attributes.isEmpty) "" else " attributes=" + StringUtils.toString(attributes)) + " content=" + StringUtils.toString(content) + ">"
  }
}