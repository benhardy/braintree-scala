package net.bhardy.braintree.scala.search

import net.bhardy.braintree.scala.{Request, RequestBuilder}
import xml.{TopScope, Null, Elem}

class SearchCriteria private(val xml: Elem) extends Request {
  def this(listName:String, items: List[_]) = {
    this(new Elem(null, listName, Null, TopScope, true, SearchCriteria.createChildItems(items): _*))
  }

  def this(searchType: String, value: AnyRef) = {
    this(RequestBuilder.buildXmlElement(searchType, value).get)
  }

  override def toXml = Some(xml)

  override def toQueryString(parent: String): String = {
    throw new UnsupportedOperationException
  }

  override def toQueryString: String = {
    throw new UnsupportedOperationException
  }
}

object SearchCriteria {


  def createChildItems(items: List[Any]): Seq[Elem] = {
    (for {
      item:Any <- items
      elem <- RequestBuilder.buildXmlElement("item", item)
    } yield elem).toSeq
  }

}