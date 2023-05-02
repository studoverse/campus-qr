package webcore.extensions

import web.html.HTMLInputElement
import web.html.HTMLTextAreaElement
import web.events.Event

val Event.inputValue: String
  get() = (target as? HTMLInputElement)?.value ?: (target as? HTMLTextAreaElement)?.value ?: ""