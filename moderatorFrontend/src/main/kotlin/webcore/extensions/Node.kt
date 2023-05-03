package webcore.extensions

import web.dom.Node

// Traverses the DOM tree upwards. Returns first node where predicate returns true else null
fun Node.findParent(pred: (Node) -> Boolean): Node? {
  var current: Node? = this
  while (current != null) {
    if (pred(current)) {
      return current
    }
    current = current.parentElement
  }
  return null
}