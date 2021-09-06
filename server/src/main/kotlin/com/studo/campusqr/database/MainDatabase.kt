package com.studo.campusqr.database

import com.moshbit.katerbase.MongoDatabase
import com.moshbit.katerbase.equal
import kotlin.reflect.KClass

private val mongoUri = System.getenv("MONGO_URI") ?: "mongodb://localhost:27017/campus-qr"


/**
 * This is the main mongo database.
 * Define all collections and indexes here.
 * For more info on the katerbase wrapper: https://github.com/studo-app/katerbase
 */
object MainDatabase : MongoDatabase(mongoUri, collections = {
  collection<BackendUser>("users") {
    index(BackendUser::email.ascending())
  }
  collection<BackendLocation>("locations")
  collection<BackendAccess>("accesses") {
    index(BackendAccess::locationId.ascending(), BackendAccess::allowedEmails.ascending())
    index(BackendAccess::backendDateRanges.ascending())
  }
  collection<Configuration>("configurations")
  collection<SessionToken>("sessionTokens")
  collection<CheckIn>("checkIns") {
    index(
      CheckIn::email.ascending(),
      CheckIn::checkOutDate.descending()
    ) // Checkout api & list of current check-ins
    index(CheckIn::locationId.ascending(), CheckIn::date.descending()) // Contact tracing

    // Automatic check-out
    // Compound index of 2 dates makes sense here because the index is partial, the first date is always null.
    index(
      CheckIn::checkOutDate.descending(), CheckIn::date.descending(),
      partialIndex = arrayOf(CheckIn::checkOutDate equal null)
    )

    // Guest check in / check out
    index(CheckIn::checkedInBy.ascending(), CheckIn::checkOutDate.descending())

    // Live check-ins
    index(CheckIn::locationId.ascending(), CheckIn::checkOutDate.descending())
  }
  collection<BackendSeatFilter>("seatFilters") {
    index(BackendSeatFilter::locationId.ascending(), BackendSeatFilter::seat.ascending())
  }
}) {
  fun <T : Any> getConfig(id: String, valueType: KClass<T>): T {
    val configEntry = getCollection<Configuration>().findOne(Configuration::_id equal id)
      ?: throw IllegalArgumentException("No config found for id $id")

    @Suppress("UNCHECKED_CAST") // KClass<T> -> type of T so it is OK to cast here
    return when (valueType) {
      Int::class -> configEntry.intValue!!
      Boolean::class -> configEntry.intValue!! == 1 // 1 -> true, 0 -> false
      String::class -> configEntry.stringValue!!
      else -> throw IllegalArgumentException("Unsupported type ${valueType.simpleName}")
    } as T
  }

  inline fun <reified T : Any> getConfig(id: String): T = getConfig(id, T::class)
}