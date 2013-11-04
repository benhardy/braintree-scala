package com.braintreegateway

import com.braintreegateway.util.NodeWrapper
import scala.collection.JavaConversions._

import java.util.Iterator
import java.lang.Iterable
/**
 * A collection used to page through query or search results.
 *
 * @tparam T
 *         type of object being paged, e.g. { @link Transaction} or
 *                                                  { @link Customer}.
 */
class ResourceCollection[T](val pager: Pager[T], response: NodeWrapper) extends Iterable[T] {

  val pageSize = response.findInteger("page-size")
  val ids = response.findAllStrings("ids/*").toIndexedSeq

  /**
   * Returns the approximate total size of the collection.
   *
   * @return Approximate size of collection
   */
  def getMaximumSize =  ids.size

  def iterator: Iterator[T] = new PagedIterator[T](this)

  def getFirst: T = pager.getPage(asScalaIterable(ids.subList(0, 1)).toList).head


  private class PagedIterator[E](resourceCollection: ResourceCollection[E]) extends Iterator[E] {

    private var index: Int = 0
    private var nextIndexToFetch: Int = 0
    private var items: List[E] = Nil

    private def nextBatchOfIds: IndexedSeq[String] = {
      var lastIdIndex: Int = nextIndexToFetch + pageSize
      if (lastIdIndex > ids.size) {
        lastIdIndex = ids.size
      }
      val nextIds = ids.slice(nextIndexToFetch, lastIdIndex)
      nextIndexToFetch = lastIdIndex
      nextIds
    }

    def hasNext: Boolean = {
      if (nextIndexToFetch < ids.size && index == items.size) {
        this.items = resourceCollection.pager.getPage(nextBatchOfIds.toList)
        this.index = 0
      }
      (index < items.size)
    }

    def next: E = {
      val item: E = items(index)
      index += 1
      item
    }

    def remove {
      throw new UnsupportedOperationException
    }

  }

}