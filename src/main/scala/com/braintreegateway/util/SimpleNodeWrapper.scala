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

object SimpleNodeWrapper {
  def parse(xml: String): SimpleNodeWrapper = {
    try {
      val source: InputSource = new InputSource(new StringReader(xml))
      val parser: SAXParser = saxParserFactory.newSAXParser
      val handler: SimpleNodeWrapper.MapNodeHandler = new SimpleNodeWrapper.MapNodeHandler
      parser.parse(source, handler)
      return handler.root
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
    return nodes
  }

  private def findAll(tokens: LinkedList[String], nodes: List[NodeWrapper]): Unit = {
    if (tokens.isEmpty) nodes.add(this)
    else {
      val first: String = tokens.getFirst
      if ("." == first) findAll(restOf(tokens), nodes)
      import scala.collection.JavaConversions._
      for (node <- childNodes) {
        if (("*" == first) || (first == node.name)) node.findAll(restOf(tokens), nodes)
      }
    }
  }

  private def find(tokens: LinkedList[String]): SimpleNodeWrapper = {
    if (tokens.isEmpty) return this
    else {
      val first: String = tokens.getFirst
      if ("." == first) return find(restOf(tokens))
      import scala.collection.JavaConversions._
      for (node <- childNodes) {
        if (("*" == first) || (first == node.name)) return node.find(restOf(tokens))
      }
      return null
    }
  }

  private def find(expression: String): SimpleNodeWrapper = {
    val paths: Array[String] = expression.split("/")
    val tokens: LinkedList[String] = new LinkedList[String](paths.toList)
    return find(tokens)
  }

  private def restOf(tokens: LinkedList[String]): LinkedList[String] = {
    val newTokens: LinkedList[String] = new LinkedList[String](tokens)
    newTokens.removeFirst
    return newTokens
  }

  def findFirst(expression: String): NodeWrapper = {
    return find(expression)
  }

  def findString(expression: String): String = {
    val node: SimpleNodeWrapper = find(expression)
    if (node == null) return null
    else return node.stringValue
  }

  private def stringValue: String = {
    if (content.size == 1 && content.get(0) == null) return null
    val value: StringBuilder = new StringBuilder
    import scala.collection.JavaConversions._
    for (o <- content) {
      value.append(o.toString)
    }
    return value.toString.trim
  }

  def getElementName: String = {
    return name
  }

  private def childNodes: List[SimpleNodeWrapper] = {
    val nodes: List[SimpleNodeWrapper] = new LinkedList[SimpleNodeWrapper]
    import scala.collection.JavaConversions._
    for (o <- content) {
      if (o.isInstanceOf[SimpleNodeWrapper]) nodes.add(o.asInstanceOf[SimpleNodeWrapper])
    }
    return nodes
  }

  def getFormParameters: Map[String, String] = {
    val params: Map[String, String] = new HashMap[String, String]
    import scala.collection.JavaConversions._
    for (node <- childNodes) {
      node.buildParams("", params)
    }
    return params
  }

  private def buildParams(prefix: String, params: Map[String, String]) {
    //val childNodes: List[SimpleNodeWrapper] = childNodes
    val newPrefix: String = if (("" == prefix)) StringUtils.underscore(name) else prefix + "[" + StringUtils.underscore(name) + "]"
    if (childNodes.isEmpty) params.put(newPrefix, stringValue)
    else {
      import scala.collection.JavaConversions._
      for (childNode <- childNodes) childNode.buildParams(newPrefix, params)
    }
  }

  override def toString: String = {
    return "<" + name + (if (attributes.isEmpty) "" else " attributes=" + StringUtils.toString(attributes)) + " content=" + StringUtils.toString(content) + ">"
  }
}