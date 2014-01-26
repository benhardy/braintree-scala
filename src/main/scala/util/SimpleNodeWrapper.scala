package net.bhardy.braintree.scala.util

import java.io.InputStream
import scala.collection.JavaConversions._

import collection.mutable.Stack
import collection.mutable.ListBuffer
import xml.MetaData
import io.Source
import scala.xml.pull._
import scala.Some
import scala.xml.parsing.XhtmlEntities

sealed trait NodeType {}

case class TextNode(value: String) extends NodeType {
  override def toString = value
}

trait Element extends NodeType {
  def name: String

  def attributes: Map[String, String]

  def content: List[NodeType]
}

object SimpleNodeWrapper {

  def parse(string: String): SimpleNodeWrapper = parse(Source.fromString(string))

  def parse(stream: InputStream): SimpleNodeWrapper = parse(Source.fromInputStream(stream))

  def parse(source: Source): SimpleNodeWrapper = parse(new XMLEventReader(source))

  def metadataMap(metaData: MetaData): Map[String, String] = {
    metaData.map {
      inner => inner.key -> inner.value.last.toString()
    }.toMap
  }

  def parse(reader: XMLEventReader): SimpleNodeWrapper = {
    var root: Option[SimpleNodeWrapper] = None

    case class NodeBuilder(name: String, metadata: MetaData, content: ListBuffer[NodeType] = new ListBuffer[NodeType])

    val stack = new Stack[NodeBuilder]

    def handleElementEnd(name: String): Option[SimpleNodeWrapper] = {
      val top = stack.pop
      if (name != top.name) throw new IllegalArgumentException("unbalanced")
      val built = SimpleNodeWrapper(top.name, metadataMap(top.metadata), top.content.toList)
      if (stack.isEmpty) {
        root = Some(built)
      } else {
        stack.top.content += built
      }
      root
    }

    def handleXmlEvent(next: XMLEvent) {
      next match {
        case EvElemStart(_, name, metadata, _) => {
          stack.push(NodeBuilder(name, metadata))
        }
        case EvText(text) => {
          val trimmed = text.trim
          if (trimmed.length > 0) {
            stack.top.content += TextNode(text)
          }
        }
        case EvElemEnd(_, name) => {
          handleElementEnd(name)
        }
        case er: EvEntityRef => {
          val entityMaybe = XhtmlEntities.entMap.get(er.entity).map {
            ch => TextNode("" + ch)
          }
          stack.top.content ++= entityMaybe
        }
      }
    }

    while (reader.hasNext) {

      val next = reader.next
      handleXmlEvent(next)
    }

    root getOrElse {
      throw new IllegalArgumentException("no root found")
    }
  }

}

case class SimpleNodeWrapper(
                              name: String,
                              attributes: Map[String, String] = Map.empty,
                              content: List[NodeType] = Nil
                              ) extends NodeWrapper with Element with NodeType {

  def findAll(expression: String): List[NodeWrapper] = {
    val paths = expression.split("/")
    val tokens = new ListBuffer[String]
    tokens ++= paths
    val nodes = new ListBuffer[NodeWrapper]
    findAll(tokens, nodes)
    nodes.toList
  }

  private def findAll(tokens: ListBuffer[String], nodes: ListBuffer[NodeWrapper]): Unit = {
    if (tokens.isEmpty) nodes.add(this)
    else {
      val first: String = tokens.head
      if ("." == first) findAll(tokens.tail, nodes)
      for (node <- childNodes) {
        if (("*" == first) || (first == node.name)) node.findAll(tokens.tail, nodes)
      }
    }
  }

  private def findOpt(tokens: List[String]): Option[SimpleNodeWrapper] = {
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
  private def find(tokens: List[String]): SimpleNodeWrapper = {
    findOpt(tokens.toList).getOrElse(null)
  }

  @deprecated
  private def find(expression: String): SimpleNodeWrapper = {
    val paths: Array[String] = expression.split("/")
    val tokens = paths.toList
    find(tokens)
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
    else Option(node.stringValue)
  }

  private def stringValue: String = {
    if (attributes.get("nil") == Some("true")) {
      null // legacy - TODO eventually remove
    }
    else {
      content.foldLeft(new StringBuilder) {
        (buf, item) =>
          buf.append(item.toString)
      }.toString.trim
    }
  }

  def getElementName = name

  private def childNodes: List[SimpleNodeWrapper] = {
    content.flatMap {
      case x: SimpleNodeWrapper => Some(x)
      case _ => None
    }
  }

  def getFormParameters: Map[String, String] = {
    val pairs = for {
      node <- childNodes
      things <- node.buildParams("")
    } yield things
    pairs.toMap
  }

  def buildParams(prefix: String): List[(String, String)] = {
    val newPrefix = prefixWithName(prefix)
    if (childNodes.isEmpty) {
      List((newPrefix, stringValue))
    }
    else {
      childNodeParamPairs(newPrefix)
    }
  }

  def childNodeParamPairs(newPrefix: String): List[(String, String)] = {
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

}