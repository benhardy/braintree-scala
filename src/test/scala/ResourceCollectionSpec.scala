package net.bhardy.braintree.scala

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import net.bhardy.braintree.scala.util.NodeWrapperFactory
import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class ResourceCollectionSpec extends FunSpec with MustMatchers {
  describe("getFirst") {
    it("produces expected count of resources") {
      val resultXml = <search-results>
        <page-size>2</page-size>
        <ids type="array">
          <items>0</items> <items>1</items> <items>2</items> <items>3</items> <items>4</items>
        </ids>
      </search-results>

      val rootNode = NodeWrapperFactory.create(resultXml.toString)

      val resourceCollection = new ResourceCollection[String](new TestPager, rootNode)

      val items = resourceCollection.toList
      items.length must be === (values.length)

      items must be === (values.toList)
    }
  }

  private val values = Array("a", "b", "c", "d", "e")

  private[braintree] class TestPager extends Pager[String] {
    def getPage(ids: List[String]) = {
      ids.map { s => values(Integer.parseInt(s)) }
    }
  }

}