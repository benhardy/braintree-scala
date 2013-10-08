package com.braintreegateway.util

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory
import java.io.StringReader
import java.util.regex.Matcher
import java.util.regex.Pattern
import scala.collection.JavaConversions._
import java.util.{Calendar, TimeZone}
import java.util.{Map=>JMap, HashMap=>JHashMap}
import java.util.{List=>JList, ArrayList=>JArrayList, LinkedList=>JLinkedList}
import java.util.{Stack=>JStack}
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

    private val stack = new JStack[SimpleNodeWrapper]
    var root: SimpleNodeWrapper = null
  }

}

class SimpleNodeWrapper(val name:String) extends NodeWrapper {

  private val attributes: JMap[String, String] = new JHashMap[String, String]
  private val content: JList[AnyRef] = new JLinkedList[AnyRef]

  def findAll(expression: String): JList[NodeWrapper] = {
    val paths: Array[String] = expression.split("/")
    val tokens = new JLinkedList[String](paths.toList)
    val nodes: JList[NodeWrapper] = new JLinkedList[NodeWrapper]
    findAll(tokens, nodes)
    nodes
  }

  private def findAll(tokens: JLinkedList[String], nodes: JList[NodeWrapper]): Unit = {
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
  private def find(tokens: JLinkedList[String]): SimpleNodeWrapper = {
    findOpt(tokens.toList).getOrElse(null)
  }

  @deprecated
  private def find(expression: String): SimpleNodeWrapper = {
    val paths: Array[String] = expression.split("/")
    val tokens = new JLinkedList[String](paths.toList)
    find(tokens)
  }

  private def restOf(tokens: JLinkedList[String]): JLinkedList[String] = {
    val newTokens = new JLinkedList[String](tokens)
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
      val value = new StringBuilder
      for (o <- content) {
        value.append(o.toString)
      }
      value.toString.trim
    }
  }

  def getElementName: String = {
    name
  }

  private def childNodes: JList[SimpleNodeWrapper] = {
    val nodes: JList[SimpleNodeWrapper] = new JLinkedList[SimpleNodeWrapper]
    import scala.collection.JavaConversions._
    for (o <- content) {
      if (o.isInstanceOf[SimpleNodeWrapper]) {
        nodes.add(o.asInstanceOf[SimpleNodeWrapper])
      }
    }
    nodes
  }

  def getFormParameters: JMap[String, String] = {
    val pairs:util.List[(String,String)] = for {
      node <- childNodes;
      things <- node.buildParams("")
    } yield things
    val m = new util.HashMap[String,String]()
    pairs.foreach{p => m.put(p._1, p._2)}
    m
  }

  def buildParams(prefix: String): List[(String,String)] = {
    val newPrefix = prefixWithName(prefix)
    if (childNodes.isEmpty) {
      List((newPrefix, stringValue))
    }
    else {
      childNodeParamPairs(newPrefix)
    }
  }

  def childNodeParamPairs(newPrefix:String): List[(String,String)] = {
    val bits = for {
      childNode <- childNodes
      childParams <- childNode.buildParams(newPrefix)
    } yield childParams
    bits.toList
  }

  def prefixWithName(prefix: String): String = {
    if ("" == prefix)
      StringUtils.underscore(name)
    else
      prefix + "[" + StringUtils.underscore(name) + "]"
  }

  override def toString: String = {
    return "<" + name + (if (attributes.isEmpty) "" else " attributes=" + StringUtils.toString(attributes)) + " content=" + StringUtils.toString(content) + ">"
  }
}