package com.studo.katerbase

import com.fasterxml.jackson.core.JsonProcessingException
import com.mongodb.client.AggregateIterable
import com.mongodb.client.DistinctIterable
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.Sorts
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass

class DistinctCursor<Entry : Any>(val mongoIterable: DistinctIterable<Entry>, private val clazz: KClass<Entry>) :
    Iterable<Entry> {
  override fun iterator(): Iterator<Entry> = mongoIterable.iterator()
}

class AggregateCursor<Entry : Any>(val mongoIterable: AggregateIterable<Document>, private val clazz: KClass<Entry>) :
    Iterable<Entry> {
  override fun iterator() = iteratorForDocumentClass(mongoIterable, clazz)
}

class FindCursor<Entry : MongoMainEntry>(
    val mongoIterable: FindIterable<Document>,
    private val clazz: KClass<Entry>,
    private val collection: MongoDatabase.MongoCollection<Entry>
) : Iterable<Entry> {
  override fun iterator() = iteratorForDocumentClass(mongoIterable, clazz)

  private var limit = 0
  private var skip = 0
  private var projection = BsonDocument()
  private var sort: Bson? = null
  private var hint: Bson? = null
  private val mongoFilter by lazy { filterGetter.get(mongoIterable) as Bson }

  /* Limit number of returned objects */
  fun limit(limit: Int): FindCursor<Entry> = apply {
    mongoIterable.limit(limit)
    this.limit = limit
  }

  fun skip(skip: Int): FindCursor<Entry> = apply {
    mongoIterable.skip(skip)
    this.skip = skip
  }

  fun hint(indexName: String): FindCursor<Entry> =
      hint(
          collection.getIndex(indexName)
              ?: throw IllegalArgumentException("Index $indexName was not found in collection ${collection.name}")
      )

  fun hint(index: MongoDatabase.MongoCollection<Entry>.MongoIndex): FindCursor<Entry> = apply {
    mongoIterable.hint(index.bson)
    this.hint = index.bson
  }

  @Deprecated("Use only for hacks")
  fun projection(bson: Bson): FindCursor<Entry> = apply {
    mongoIterable.projection(bson)
  }

  fun <T> selectedFields(vararg fields: MongoEntryField<out T>): FindCursor<Entry> = apply {
    val bson = fields.includeBson()
    this.projection.combine(bson)
    mongoIterable.projection(this.projection)
  }

  @Deprecated(
      "Excluding fields is an anti-pattern and is not maintainable. Always use selected fields. " +
          "You can also structure your db-object into sub-objects so you only need to select one field."
  )
  fun <T> excludeFields(vararg fields: MongoEntryField<out T>): FindCursor<Entry> = apply {
    val bson = fields.excludeBson()
    this.projection.combine(bson)
    mongoIterable.projection(this.projection)
  }

  @Deprecated("Use only for hacks")
  fun sort(bson: Bson): FindCursor<Entry> = apply {
    mongoIterable.sort(bson)
    this.sort = bson
  }

  fun <T> sortByDescending(field: MongoEntryField<T>): FindCursor<Entry> = apply {
    val fieldName = field.toMongoField().name
    val bson = Sorts.descending(fieldName)
    mongoIterable.sort(bson)
    this.sort = bson
  }

  fun <T> sortBy(field: MongoEntryField<T>): FindCursor<Entry> = apply {
    val fieldName = field.toMongoField().name
    val bson = Sorts.ascending(fieldName)
    mongoIterable.sort(bson)
    this.sort = bson
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as FindCursor<*>

    if (clazz != other.clazz) return false
    if (limit != other.limit) return false
    if (skip != other.skip) return false
    if (projection != other.projection) return false
    if (sort != other.sort) return false
    if (hint != other.hint) return false
    if (mongoFilter != other.mongoFilter) return false

    return true
  }

  override fun hashCode(): Int {
    var result = clazz.hashCode()
    result = 31 * result + limit
    result = 31 * result + skip
    result = 31 * result + projection.hashCode()
    result = 31 * result + (sort?.hashCode() ?: 0)
    result = 31 * result + (hint?.hashCode() ?: 0)
    result = 31 * result + (mongoFilter.hashCode())

    return result
  }

  companion object {
    val filterGetter = Class.forName("com.mongodb.client.internal.FindIterableImpl").declaredFields
        .find { it.name == "filter" }!!
        .apply { isAccessible = true }
  }
}

private fun <Entry : Any> iteratorForDocumentClass(
    mongoIterable: MongoIterable<out Document>,
    clazz: KClass<Entry>
): Iterator<Entry> = object : Iterator<Entry> {
  val mongoIterator = mongoIterable.iterator()

  override fun hasNext(): Boolean = mongoIterator.hasNext()

  override fun next(): Entry {
    val document = mongoIterator.next()
    return try {
      JsonHandler.fromBson(document, clazz)
    } catch (e: JsonProcessingException) {
      throw IllegalArgumentException(
          "Could not deserialize mongo entry of type ${clazz.simpleName} with id ${document["_id"]}",
          e
      )
    }
  }
}

fun BsonDocument.combine(other: BsonDocument): BsonDocument {
  other.forEach { (key, value) ->
    this.remove(key)
    this[key] = value
  }
  return this
}

fun combineBsonWithValue(keys: List<String>, value: Int) = BsonDocument().apply {
  keys.forEach { key -> this.append(key, BsonInt32(value)) }
}

fun <T> Array<out MongoEntryField<out T>>.includeBson() =
    combineBsonWithValue(map { it.toMongoField().name }, value = 1)

fun <T> Array<out MongoEntryField<out T>>.excludeBson() =
    combineBsonWithValue(map { it.toMongoField().name }, value = 0)
