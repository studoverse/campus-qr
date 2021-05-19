package webcore.extensions

import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun NodeList.toList(): List<Node> {
  @Suppress("UNUSED_VARIABLE") // variable list is used in js block
  val list = this
  val array: Array<Node> = js("Array.prototype.slice.call(list)") as Array<Node>
  return array.toList()
}