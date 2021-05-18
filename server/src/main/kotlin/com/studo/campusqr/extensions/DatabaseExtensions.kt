package com.studo.campusqr.extensions

import com.studo.campusqr.database.MainDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This function executes the [action] in the [Dispatchers.IO] context, because mongo database calls
 * are blocking not suspending.
 */
suspend inline fun <R> runOnDb(crossinline action: suspend MainDatabase.() -> R): R {
  return withContext(Dispatchers.IO) { action.invoke(MainDatabase) }
}