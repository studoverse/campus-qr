package com.studo.katerbase

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.BsonField
import com.mongodb.client.model.Projections
import org.bson.Document
import org.bson.conversions.Bson
import java.util.*
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeParameter

abstract class MongoEntry {
  fun toBSONDocument(): Document = JsonHandler.toBsonDocument(this)
}

abstract class MongoSubEntry : MongoEntry()

abstract class MongoAggregationEntry : MongoEntry()

abstract class MongoMainEntry : MongoEntry() {
  @Suppress("PropertyName") // MongoDBs id field is named "_id", so allow it here too.
  var _id: String = ""

  override fun equals(other: Any?): Boolean = other === this || (other as? MongoMainEntry)?._id == this._id
  override fun hashCode(): Int = _id.hashCode()

  fun randomId(): String = MongoMainEntry.randomId()

  fun generateId(compoundValue: String, vararg compoundValues: String): String =
      MongoMainEntry.generateId(compoundValue, *compoundValues)

  companion object {
    private val random = Random()

    internal fun randomId(): String {
      val bytes = ByteArray(size = 16) // 16 * 8 = 256 -> full entropy for sha256
      random.nextBytes(bytes)
      return bytes.sha256().take(32)
    }

    internal fun generateId(compoundValue: String, vararg compoundValues: String): String {
      return compoundValues.joinToString(separator = "|", prefix = "$compoundValue|").sha256().take(32)
    }
  }
}

@Suppress("UNCHECKED_CAST")
private fun Any?.toBSONDocument(): Any? = when (this) {
  null -> this
  is String, is Int, is Long, is Double, is Float, is Date, is Boolean, is ByteArray -> this
  is Enum<*> -> this.name
  is Collection<*> -> this.map { it.toBSONDocument() }
  is Document -> this.map { (key, value) -> (key as String) to value.toBSONDocument() }.toMap(Document())
  is Map<*, *> -> this.map { (key, value) -> (key as String) to value.toBSONDocument() }.toMap(Document())
  is MongoEntry -> this.toBSONDocument()
  is SubDocumentListFilter -> Document("\$elemMatch", filter.toFilterDocument())
  else -> throw IllegalArgumentException("${this.javaClass.simpleName} is not BSON compatible, if you want to put this class in Mongo it should be a MongoEntry!")
}

fun Array<out MongoPair>.toFilterDocument(): Document {
  val bson = Document()
  // Iterate through every MongoPair and add their values
  this.forEach { (key, value) ->
    when (value) {
      is Document -> {
        // If value is a Document, merge it's keys and values with another Document from the same key to support multiple operators
        val innerBson = bson.getOrPut(key.name) { Document() } as? Document
            ?: throw IllegalStateException("${key.name} can't have 'equal' or 'has' operators if it has multiple operators")
        value.forEach { innerKey, innerValue -> innerBson[innerKey] = innerValue }
        bson[key.name] = innerBson // Add merged Document to outer Document
      }
      else -> bson[key.name] = value
    }
  }
  return bson
}

class SubDocumentListFilter(vararg val filter: FilterPair)

typealias MongoEntryField<T> = KMutableProperty1<out MongoEntry, T>
typealias NullableMongoEntryField<T> = KMutableProperty1<out MongoEntry, T?>

fun <Value> MongoEntryField<Value>.toMongoField() = MongoField(name)

/**
 * Use this if you want to access a subdocument's field
 */
fun <Class, Value> MongoEntryField<out Any>.child(property: KMutableProperty1<Class, Value>): MongoEntryField<Value> {
  return this.toMongoField().extend(property.name).toProperty()
}

@JvmName("childOnNullable")
fun <Class, Value> NullableMongoEntryField<out Any>.child(property: KMutableProperty1<Class, Value>): MongoEntryField<Value> {
  return this.toMongoField().extend(property.name).toProperty()
}

/**
 * Use this if you have an array of classes. This filter returns every document where in the array
 * every filter matches at least one subdocument
 */
fun MongoEntryField<out List<MongoSubEntry>>.any(vararg filter: FilterPair): FilterPair {
  return FilterPair(this, SubDocumentListFilter(*filter))
}

/**
 * Use this if you have an array of classes. This filter returns every document where in the array
 * none of the filter matches any subdocuments
 */
fun MongoEntryField<out List<MongoSubEntry>>.none(vararg filter: FilterPair): FilterPair {
  return FilterPair(this, Document("\$not", SubDocumentListFilter(*filter).toBSONDocument()))
}

// Represents a fake Kotlin class property
private class FakeProperty<T, R>(override val name: String) : KMutableProperty1<T, R> {
  private fun error(): Nothing = throw Exception("Operation not supported")

  override fun invoke(p1: T): R = error()

  override fun callBy(args: Map<KParameter, Any?>): R = error()
  override val isLateinit get() = false
  override val isAbstract get() = false
  override val isFinal get() = false
  override val isOpen get() = false
  override val parameters: List<KParameter> get() = emptyList()
  override val returnType get() = error()
  override val typeParameters: List<KTypeParameter> get() = emptyList()
  override val isSuspend: Boolean = false

  override val visibility get() = error()

  override fun call(vararg args: Any?) = error()
  override val isConst get() = false

  override val setter get() = error()

  override fun getDelegate(receiver: T): Any? = error()
  override val annotations: List<Annotation> get() = emptyList()

  override val getter get() = error()
  override fun get(receiver: T): R = error()
  override fun set(receiver: T, value: R) = error()

}

class MongoField(val name: String) {
  fun extend(name: String) = MongoField(this.name + '.' + name)
  fun extendWithCursor(name: String) = MongoField(this.name + ".$." + name)

  fun <Class, Type> toProperty(): KMutableProperty1<Class, Type> = FakeProperty(name)
  val fieldName: String get() = name.takeLastWhile { it != '.' }

  override fun equals(other: Any?): Boolean = (other as? MongoField)?.name == name
  override fun hashCode(): Int = name.hashCode()
}

abstract class MongoPair(val key: MongoField, val value: Any?) {
  operator fun component1() = key
  operator fun component2() = value
}

class FilterPair
@Deprecated("Use only for hacks") constructor(key: MongoField, value: Any?) : MongoPair(key, value.toBSONDocument()) {

  @Suppress("DEPRECATION")
  constructor(key: KMutableProperty1<out MongoEntry, out Any?>, value: Any?) : this(key.toMongoField(), value)
}

class MutatorPair<out Value>
@Deprecated("Use only for hacks") constructor(key: MongoField, value: Any?) : MongoPair(key, value.toBSONDocument()) {

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<out Value>, value: Value?) : this(key.toMongoField(), value)

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<out Value>, value: Document) : this(key.toMongoField(), value)

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<out Value>, value: List<Document>) : this(key.toMongoField(), value)

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<Map<String, Value>>, value: Pair<String, Value>) :
      this(key.toMongoField().extend(value.first), value.second)
}

class PushPair<Value>
@Deprecated("Use only for hacks") constructor(key: MongoField, value: Any?) : MongoPair(key, value.toBSONDocument()) {

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<List<Value>>, value: Value?) : this(key.toMongoField(), value)

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<List<Value>>, value: Document) : this(key.toMongoField(), value)
}

// See: https://docs.mongodb.com/manual/reference/operator/update/unset/
@Deprecated("Use only for hacks")
class UnsetPair(key: MongoField) : MongoPair(key, value = "") {

  @Suppress("DEPRECATION")
  constructor(key: MongoEntryField<out Any?>) : this(key.toMongoField())
}

// FilterPair

infix fun <Value> MongoEntryField<Value>.equal(value: Value?) = FilterPair(this, value)
infix fun <Value> MongoEntryField<Value>.notEqual(value: Value?) = FilterPair(this, Document("\$ne", value))

// THIS CALL CAN NOT BE INDEXED!!!
infix fun MongoEntryField<String>.contains(value: String) = contains(value, caseSensitive = true)

infix fun MongoEntryField<String>.containsCaseInsensitive(value: String) = contains(value, caseSensitive = false)
fun MongoEntryField<String>.contains(value: String, caseSensitive: Boolean = true) = FilterPair(this, Document().apply {
  append("\$regex", ".*" + Pattern.quote(value) + ".*") // Use Pattern.quote(..) to escape all regex reserved chars
  if (!caseSensitive) append("\$options", "i") // Case-insensitive
})

// THIS CALL CAN NOT BE INDEXED!!!
infix fun MongoEntryField<String>.startsWith(value: String) = startsWith(value, caseSensitive = true)

infix fun MongoEntryField<String>.startsWithCaseInsensitive(value: String) = startsWith(value, caseSensitive = false)

fun MongoEntryField<String>.startsWith(value: String, caseSensitive: Boolean = true) =
    FilterPair(this, Document().apply {
      append("\$regex", "^" + Pattern.quote(value) + ".*") // Use Pattern.quote(..) to escape all regex reserved chars
      if (!caseSensitive) append("\$options", "i") // Case-insensitive
    })

// THIS CALL CAN NOT BE INDEXED!!!
infix fun MongoEntryField<String>.endsWith(value: String) = endsWith(value, caseSensitive = true)

fun MongoEntryField<String>.endsWith(value: String, caseSensitive: Boolean = true) = FilterPair(this, Document().apply {
  append("\$regex", ".*" + Pattern.quote(value) + "\$") // Use Pattern.quote(..) to escape all regex reserved chars
  if (!caseSensitive) append("\$options", "i") // Case-insensitive
})

infix fun <Value> KMutableProperty1<out MongoEntry, out Collection<Value>>.has(value: Value) = FilterPair(this, value)

infix fun <Value> MongoEntryField<Value>.inArray(array: Collection<Value>): FilterPair {
  return FilterPair(this, Document("\$in", array))
}

infix fun <Value> KMutableProperty1<out MongoEntry, out Collection<Value>>.hasAnyInArray(array: Collection<Value>) =
    FilterPair(this, Document("\$in", array))

infix fun <Value> MongoEntryField<Value>.lower(value: Value) = FilterPair(this, Document("\$lt", value))
infix fun <Value> MongoEntryField<Value>.lowerEquals(value: Value) = FilterPair(this, Document("\$lte", value))

infix fun <Value> MongoEntryField<Value>.greater(value: Value) = FilterPair(this, Document("\$gt", value))
infix fun <Value> MongoEntryField<Value>.greaterEquals(value: Value) = FilterPair(this, Document("\$gte", value))

fun <Value> MongoEntryField<Value>.inRange(
    start: Value,
    end: Value,
    includeStart: Boolean = true,
    includeEnd: Boolean = false
) =
    FilterPair(this, Document().apply {
      if (includeStart) putAll(Document("\$gte", start)) else putAll(Document("\$gt", start))
      if (includeEnd) putAll(Document("\$lte", end)) else putAll(Document("\$lt", end))
    })

// Keep in mind that this query can't be indexed (unless using probably a space index)
fun <Value> MongoEntryField<Value>.exists(value: Boolean = true) = FilterPair(this, Document("\$exists", value))

// MutatorPair

infix fun <Value> MongoEntryField<Value>.valueDocument(value: Document) = MutatorPair(this, value)

// PushPair
infix fun <Value> MongoEntryField<List<Value>>.valueDocument(value: Document) = PushPair(this, value)

// Global operators
// These should look like this: find({$or:[{_id: "1"}, {_id: "2"}]})
// They can be combined like this: find({$or:[{_id: "1"}, {$and:[{name: "test"}, {_id: "2"}]}]})
// Bind these to MongoDatabase so they are "not so global"

// Logical operators
@Suppress("DEPRECATION", "unused")
fun MongoDatabase.or(vararg filter: FilterPair) =
    FilterPair(MongoField("\$or"), filter.map { arrayOf(it).toFilterDocument() })

@Suppress("DEPRECATION", "unused")
fun MongoDatabase.and(vararg filter: FilterPair) =
    FilterPair(MongoField("\$and"), filter.map { arrayOf(it).toFilterDocument() })

// This call requires a Text Index https://docs.mongodb.com/manual/text-search/#text-operator
// All fields that are defined as Text Index in this collection are searched.
@Suppress("DEPRECATION")
infix fun MongoDatabase.searchText(value: String) = FilterPair(MongoField("\$text"), Document("\$search", value))

// Aggregation
fun aggregationPipeline(block: AggregationPipeline.() -> Unit): AggregationPipeline =
    AggregationPipeline().apply { block() }

class AggregationPipeline {
  val bson: MutableList<Bson> = mutableListOf()

  fun match(vararg filter: FilterPair) {
    bson += Aggregates.match(filter.toFilterDocument())
  }

  fun group(field: MongoEntryField<*>, block: Accumulation.() -> Unit) {
    val accumulators = Accumulation().apply { block() }.accumulators
    bson += Aggregates.group("\$${field.name}", accumulators.map { it.bsonField })
  }

  // Use this when you want to simly just include some of the fields
  fun project(vararg selectedFields: MongoEntryField<*>) {
    bson += Aggregates.project(selectedFields.includeBson())
  }

  // Use this when you want to transform the document
  inline fun <reified InputType : MongoEntry, reified OutputType : MongoAggregationEntry> transform(noinline block: Transformation<InputType, OutputType>.() -> Unit) {
    transform(
        transformation = Transformation(InputType::class, OutputType::class),
        block = block
    )
  }

  fun <InputType : MongoEntry, OutputType : MongoAggregationEntry> transform(
      transformation: Transformation<InputType, OutputType>,
      block: Transformation<InputType, OutputType>.() -> Unit
  ) {
    bson += Aggregates.project(transformation.apply { block() }.bson)
  }

  fun sortBy(field: MongoEntryField<*>) {
    bson += Aggregates.sort(Document(field.name, 1))
  }

  fun sortByDescending(field: MongoEntryField<*>) {
    bson += Aggregates.sort(Document(field.name, -1))
  }

  class Accumulation {
    val accumulators: MutableList<Accumulator> = mutableListOf()

    class Accumulator(val bsonField: BsonField)

    fun sum(field: MongoEntryField<out Number>, value: MongoEntryField<out Number>) {
      accumulators += Accumulator(bsonField = Accumulators.sum(field.name, "\$${value.name}"))
    }

    fun average(field: MongoEntryField<out Number>, value: MongoEntryField<out Number>) {
      accumulators += Accumulator(bsonField = Accumulators.avg(field.name, "\$${value.name}"))
    }

    fun max(field: MongoEntryField<out Number>, value: MongoEntryField<out Number>) {
      accumulators += Accumulator(bsonField = Accumulators.max(field.name, "\$${value.name}"))
    }

    fun min(field: MongoEntryField<out Number>, value: MongoEntryField<out Number>) {
      accumulators += Accumulator(bsonField = Accumulators.min(field.name, "\$${value.name}"))
    }

    fun count(field: MongoEntryField<out Number>) {
      accumulators += Accumulator(bsonField = Accumulators.sum(field.name, 1))
    }

    @Deprecated("Use only for hacks")
    fun addAccumulator(bsonField: BsonField) {
      accumulators += Accumulator(bsonField)
    }
  }

  class Transformation<InputType : MongoEntry, OutputType : MongoAggregationEntry>(
      val inputClass: KClass<InputType>,
      val outputClass: KClass<OutputType>
  ) {
    private val projections: MutableList<Bson> = mutableListOf()
    val bson: Bson get() = Projections.fields(projections)

    fun include(field: KMutableProperty1<InputType, *>) {
      projections += Projections.include(field.name)
    }

    // We can't use InputType for inputField here because inputFied can also be a child document type of a MongoMainEntry,
    // just ensure the field values have the same type
    fun <FieldValueType> project(
        inputField: MongoEntryField<FieldValueType>,
        outputField: KMutableProperty1<OutputType, FieldValueType>
    ) {
      projections += Projections.computed(outputField.name, "\$${inputField.name}")
    }
  }
}