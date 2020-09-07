package com.studo.campusqr.database

import com.studo.katerbase.MongoDatabase
import com.studo.katerbase.equal
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
        index(BackendAccess::dateRanges.ascending())
        index(BackendAccess::allowedEmails.ascending())
    }
    collection<Configuration>("configurations")
    collection<SessionToken>("sessionTokens")
    collection<CheckIn>("checkIns") {
        index(CheckIn::email.ascending(), CheckIn::date.descending())
        index(CheckIn::locationId.ascending(), CheckIn::date.descending())
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