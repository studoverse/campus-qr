package webcore.extensions

import web.dom.Node
import web.dom.NodeList

fun NodeList<Node>.toList(): List<Node> {
  @Suppress("UNUSED_VARIABLE") // variable list is used in js block
  val list = this
  val array: Array<Node> = js("Array.prototype.slice.call(list)") as Array<Node>
  return array.toList()
}