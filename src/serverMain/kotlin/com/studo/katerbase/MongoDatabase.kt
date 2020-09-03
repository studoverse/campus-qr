package com.studo.katerbase

import ch.qos.logback.classic.Level
import com.mongodb.*
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.model.*
import com.mongodb.client.model.changestream.FullDocument
import com.mongodb.client.model.changestream.OperationType
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.*
import org.bson.conversions.Bson
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties

/**
 * How to set-up a local mongodb development environment (on OSX)
 * 1) Open a terminal ;)
 * 2) Install homebrew https://brew.sh/index_de
 * 3) Remove any existing mongodb installation from your machine
 * 4) Add the new official mongo brew repository, type "brew tap mongodb/brew"
 * 5) Install mongodb, type "brew install mongodb-community"
 * 6) Start the service, type "brew services start mongodb-community"
 * 7) Check if it worked using Studio 3t or just type "mongo" to open the mongo shell.
 */

open class MongoDatabase(
        uri: String,
        allowReadFromSecondaries: Boolean = false,
        private val supportChangeStreams: Boolean = false,
        autoManageCollectionsAndIndexes: Boolean = true,
        clientSettings: (MongoClientSettings.Builder.() -> Unit)? = null,
        collections: MongoDatabaseDefinition.() -> Unit
) {
    protected val client: MongoClient
    protected val internalDatabase: com.mongodb.client.MongoDatabase
    protected val mongoCollections: Map<KClass<out MongoMainEntry>, MongoCollection<out MongoMainEntry>>
    protected val changeStreamClient: MongoClient?
    protected val changeStreamCollections: Map<KClass<out MongoMainEntry>, MongoCollection<out MongoMainEntry>>

    open fun <T : MongoMainEntry> getCollection(entryClass: KClass<T>): MongoCollection<T> {
        @Suppress("UNCHECKED_CAST")
        return mongoCollections[entryClass] as? MongoCollection<T>
                ?: throw IllegalArgumentException("No collection exists for ${entryClass.simpleName}")
    }

    inline fun <reified T : MongoMainEntry> getCollection() = getCollection(entryClass = T::class)

    class DuplicateKeyException(key: String) : IllegalStateException("Duplicate key: $key was already in collection.")

    init {
        // Disable mongo driver logging
        setLogLevel("org.mongodb", Level.ERROR)

        val connectionString = ConnectionString(uri)

        client = createMongoClientFromUri(
                connectionString, allowReadFromSecondaries = allowReadFromSecondaries,
                clientSettings = clientSettings
        )

        changeStreamClient = if (supportChangeStreams) {
            createMongoClientFromUri(connectionString, allowReadFromSecondaries = false, clientSettings = {
                readPreference(ReadPreference.primaryPreferred())

                // ChangeStreams work until MongoDB 4.2 only with ReadConcern.MAJORITY, see https://docs.mongodb.com/manual/changeStreams/
                readConcern(ReadConcern.MAJORITY)

                clientSettings?.invoke(this)
            })
        } else {
            null
        }

        internalDatabase = client.getDatabase(connectionString.database!!)

        val databaseDefinition = MongoDatabaseDefinition().apply { collections() }

        mongoCollections = databaseDefinition.collections.associateBy(
                keySelector = { it.modelClass },
                valueTransform = {
                    MongoCollection(
                            internalDatabase.getCollection(it.collectionName),
                            it.modelClass,
                            it.indexes
                    )
                }
        )

        changeStreamCollections = if (supportChangeStreams) {
            val changeStreamClientDatabase = changeStreamClient!!.getDatabase(connectionString.database!!)
            databaseDefinition.collections.associateBy(
                    keySelector = { it.modelClass },
                    valueTransform = {
                        MongoCollection(
                                changeStreamClientDatabase.getCollection(it.collectionName),
                                it.modelClass,
                                it.indexes
                        )
                    }
            )
        } else emptyMap()

        if (autoManageCollectionsAndIndexes) {
            // Create collections which don't exist
            val newCollections = databaseDefinition.collections
                    .filter { it.collectionName !in internalDatabase.listCollectionNames() }

            if (newCollections.isNotEmpty()) {
                println("Creating ${newCollections.size} new collections:")
                newCollections.forEach { newCollection ->
                    if (newCollection.collectionSizeCap == null) {
                        internalDatabase.createCollection(newCollection.collectionName)
                    } else {
                        internalDatabase.createCollection(
                                newCollection.collectionName,
                                CreateCollectionOptions().capped(true).sizeInBytes(newCollection.collectionSizeCap)
                        )
                    }

                    println("Successfully created collection ${newCollection.collectionName}")
                }
            }

            // Create and delete indexes in MongoDB
            mongoCollections.forEach { (_, collection) ->
                val existingIndexes = collection.internalCollection.listIndexes().toList().map { it["name"] as String }

                // Drop indexes which do not exist in the codebase anymore
                existingIndexes
                        .filter { indexName -> indexName != "_id_" } // Never drop _id
                        .filter { indexName -> collection.indexes.none { index -> index.indexName == indexName } }
                        .forEach { indexName ->
                            collection.internalCollection.dropIndex(indexName)
                            println("Successfully dropped index $indexName")
                        }

                // Make sure all indices are dropped first before creating new indexes so in case we change a textIndex we don't throw because
                // only one text index per collection is allowed.

                // Create new indexes which doesn't exist locally
                collection.indexes
                        .filter { index -> index.indexName !in existingIndexes }
                        .forEach { index ->
                            thread {
                                // Don't wait for this, application can be started without the indexes
                                println("Creating index ${index.indexName} ...")
                                index.createIndex()
                                println("Successfully created index ${index.indexName}")
                            }
                        }
            }
        }

        // Validation for Jackson to avoid serialization/deserialization issues
        mongoCollections.keys.forEach { mongoEntryClass ->
            mongoEntryClass.java.declaredFields.forEach { field ->
                fun errorMessage(msg: String) = "Field error in ${mongoEntryClass.simpleName} -> ${field.name}: $msg"
                require(!field.name.startsWith("is")) { errorMessage("Can't start with 'is'") }
                require(!field.name.startsWith("set")) { errorMessage("Can't start with 'set'") }
                require(!field.name.startsWith("get")) { errorMessage("Can't start with 'get'") }
            }
        }
    }

    data class PayloadChange<Entry : MongoMainEntry>(
            val _id: String,
            val payload: Entry?, // Payload is not available when operationType is DELETED
            val operationType: OperationType
    )

    // Mongo collection wrapper for Kotlin
    inner class MongoCollection<Entry : MongoMainEntry>(
            val internalCollection: com.mongodb.client.MongoCollection<Document>,
            private val entryClass: KClass<Entry>,
            indexes: List<MongoDatabaseDefinition.Collection.Index>
    ) {

        val name: String get() = internalCollection.namespace.collectionName

        internal val indexes: List<MongoIndex> = indexes.map { MongoIndex(it) }

        /**
         * This only works if MongoDB is a replica set
         * To test this set up a local replica set (follow the README in local-development > local-mongo-replica-set)
         * Use [ignoredFields] to exclude a set of fields, if any change occurs to these fields it will be ignored
         */
        fun watch(ignoredFields: List<MongoEntryField<*>> = emptyList(), action: (PayloadChange<Entry>) -> Unit) {
            require(supportChangeStreams) { "supportChangeStreams must be true for the watch() operation" }

            // Add aggregation pipeline to stream
            // https://stackoverflow.com/questions/49621939/how-to-watch-for-changes-to-specific-fields-in-mongodb-change-stream
            val pipeline = Document().apply {
                this["\$match"] = Document().apply {
                    this["\$or"] = listOf(
                            Document("operationType", "insert"),
                            Document("operationType", "replace"),
                            Document("operationType", "delete"),
                            Document().apply {
                                val filter = mutableListOf<Document>()
                                val ignoredMongoFields = ignoredFields.map { it.toMongoField() }.toSet()

                                // Get all the fields where we should listen for changes
                                val nonIgnoredMongoFields = entryClass.memberProperties
                                        .mapNotNull { it as? MongoEntryField<*> }
                                        .map { it.toMongoField() }
                                        .filter { it !in ignoredMongoFields }

                                nonIgnoredMongoFields.forEach { field ->
                                    // Format must look like this -> { "updateDescription.updatedFields.SomeFieldA": { $exists: true } }
                                    filter += Document(
                                            "updateDescription.updatedFields.${field.name}",
                                            Document("\$exists", true)
                                    )
                                }

                                this["\$and"] = listOf(
                                        Document("operationType", "update"),
                                        Document("\$or", filter)
                                )
                            }
                    )
                }
            }

            val internalCollection = changeStreamCollections.getValue(entryClass).internalCollection

            thread {
                try {
                    internalCollection.watch(listOf(pipeline))
                            .apply { fullDocument(FullDocument.UPDATE_LOOKUP) }
                            .forEach { document ->
                                val change = PayloadChange(
                                        _id = (document.documentKey!!["_id"] as BsonString).value,
                                        payload = document.fullDocument?.let { JsonHandler.fromBson(it, entryClass) },
                                        operationType = document.operationType
                                )
                                try {
                                    action(change)
                                } catch (e: Exception) {
                                    // If action fails, handle the exception but do not close the changeStream
                                    Thread.getDefaultUncaughtExceptionHandler()
                                            ?.uncaughtException(Thread.currentThread(), e) ?: thread { throw e }
                                }
                            }
                } catch (e: MongoCommandException) {
                    if (e.code == 40573) {
                        // The $changeStream stage is only supported on replica sets
                        throw IllegalStateException("watch() can only be used in a replica set", e)
                    } else {
                        thread { throw e } // Not sure what just happened. Log the error and restart the watch() operation
                    }
                } catch (e: java.lang.Exception) {
                    thread { throw e } // Not sure what just happened. Log the error and restart the watch() operation
                }
                watch(ignoredFields, action)
            }
        }

        // The index name is based on bson and partialIndex. Therefore when changing the bson or the partialIndex, the old index
        // will get deleted and a new index is created.
        // Keep in mind that changing indexOptions do not create a new index, so you need to manually delete the index or modify the index
        // appropriately in the database.
        inner class MongoIndex(indexDefinition: MongoDatabaseDefinition.Collection.Index) {
            val bson: Bson = indexDefinition.index
            val partialIndex: Document? = indexDefinition.partialIndex?.toFilterDocument()
            val indexOptions: (IndexOptions.() -> Unit)? = indexDefinition.indexOptions
            val indexName: String

            init {
                fun BsonValue.toIndexValue() = when (this) {
                    is BsonNumber -> this.intValue()
                    is BsonString -> this.value
                    else -> throw IllegalArgumentException("Invalid index value")
                }

                val baseName = bson
                        .toBsonDocument(BsonDocument::class.java, null)
                        .toList()
                        .joinToString(separator = "_", transform = { "${it.first}_${it.second.toIndexValue()}" })

                val partialSuffix = partialIndex
                        ?.map { (key, value) -> key to value }
                        ?.joinToString(separator = "_", transform = { (operator, value) -> "${operator}_$value" })
                        ?.let { suffix -> "_$suffix" } ?: ""

                indexName = baseName + partialSuffix
            }

            fun createIndex(): String? = internalCollection.createIndex(bson, IndexOptions()
                    .background(true)
                    .apply {
                        if (partialIndex != null) {
                            partialFilterExpression(partialIndex)
                        }
                    }
                    .apply { indexOptions?.invoke(this) }
                    .name(indexName) // Set name after indexOptions invoke, as our index management relies on that name
            )
        }

        fun getIndex(indexName: String): MongoIndex? = indexes.singleOrNull { it.indexName == indexName }

        // Used to create indexes for childFields
        fun <Class, Value> MongoEntryField<out Any>.child(property: KMutableProperty1<Class, Value>): MongoEntryField<Value> {
            return this.toMongoField().extend(property.name).toProperty()
        }

        fun drop() {
            internalCollection.drop()
        }

        fun clear(): DeleteResult {
            return deleteMany()
        }

        fun count(vararg filter: FilterPair): Long {
            if (logAllQueries) println("count: ${filter.toFilterDocument().asJsonString()}")
            return if (filter.isEmpty()) internalCollection.estimatedDocumentCount() else internalCollection.countDocuments(
                    filter.toFilterDocument()
            )
        }

        fun bulkWrite(
                options: BulkWriteOptions = BulkWriteOptions(),
                action: BulkOperation.() -> Unit
        ): BulkWriteResult {
            val models = BulkOperation().apply { action(this) }.models
            if (models.isEmpty()) return BulkWriteResult.acknowledged(
                    0,
                    0,
                    0,
                    0,
                    emptyList()
            ) // Acknowledge empty bulk write
            return internalCollection.bulkWrite(models, options)
        }

        private fun Document.toClass() = JsonHandler.fromBson(this, entryClass)
        private fun FindIterable<Document>.toClasses() = FindCursor(this, entryClass, this@MongoCollection)

        // Returns a MongoDocument as a list of mutators. Useful if you want to set all values in an update block.
        // In case _id should be included in the mutator, set withId to true.
        private fun Entry.asMutator(withId: Boolean): List<MutatorPair<Any>> =
                this.toBSONDocument().map { (key, value) ->
                    @Suppress("DEPRECATION")
                    MutatorPair<Any>(MongoField(key), value)
                }.let { mutator -> if (withId) mutator else mutator.filter { it.key.name != "_id" } }

        // Single operators
        @Deprecated("Use only for hacks", ReplaceWith("find"))
        fun findDocuments(vararg filter: FilterPair): FindIterable<Document> {
            return internalCollection.find(filter.toFilterDocument())
        }

        fun <T : MongoEntry> aggregate(pipeline: AggregationPipeline, entryClass: KClass<T>): AggregateCursor<T> {
            return AggregateCursor(
                    mongoIterable = internalCollection.aggregate(
                            /*pipeline = */ pipeline.bson,
                            /*resultClass = */ Document::class.java
                    ),
                    clazz = entryClass
            )
        }

        inline fun <reified T : MongoEntry> aggregate(noinline pipeline: AggregationPipeline.() -> Unit): AggregateCursor<T> {
            return aggregate(
                    pipeline = aggregationPipeline(pipeline),
                    entryClass = T::class
            )
        }

        fun find(vararg filter: FilterPair): FindCursor<Entry> {
            if (logAllQueries) println(
                    "find: ${
                        filter.toFilterDocument().asJsonString()
                    } (pipeline: ${filter.getExecutionPipeline()})"
            )
            return internalCollection.find(filter.toFilterDocument()).toClasses()
        }

        fun findOne(vararg filter: FilterPair): Entry? {
            require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }
            return find(*filter).limit(1).firstOrNull()
        }

        // Returns a document or inserts the document and then returns it.
        // This works atomically, so newEntry may be called even if the document exists
        fun findOneOrInsert(vararg filter: FilterPair, newEntry: () -> Entry): Entry {
            require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }

            // This is a performance optimization, when using updateOneAndFind the document is locked
            return findOne(*filter) ?: updateOneAndFind(*filter, upsert = true) {

                val useNewEntryId = filter.none { it.key.name == "_id" }
                val mutator = newEntry().asMutator(withId = useNewEntryId)
                if (useNewEntryId && mutator.none { it.key.name == "_id" && it.value != "" }) {
                    throw IllegalArgumentException("_id must either be in filter or must be set in newEntry()")
                }

                mutator.forEach { updateMutator("setOnInsert", it) }
            }!!
        }

        /**
         * Use this if you need a set of distinct specific value of a document
         * More info: https://docs.mongodb.com/manual/reference/method/db.collection.distinct/
         */
        fun <T : Any> distinct(
                distinctField: MongoEntryField<T>,
                entryClass: KClass<T>,
                vararg filter: FilterPair
        ): DistinctCursor<T> {
            return DistinctCursor(
                    mongoIterable = internalCollection.distinct(
                            /*fieldName = */ distinctField.name,
                            /*filter = */ filter.toFilterDocument(),
                            /*resultClass = */ entryClass.java
                    ),
                    clazz = entryClass
            )
        }

        inline fun <reified T : Any> distinct(
                distinctField: MongoEntryField<T>,
                vararg filter: FilterPair
        ): DistinctCursor<T> {
            return distinct(distinctField, T::class, *filter)
        }

        fun updateOne(vararg filter: FilterPair, update: UpdateOperation.() -> Unit): UpdateResult {
            require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }
            val mutator = UpdateOperation().apply { update(this) }.mutator
            if (mutator.isEmpty()) return UpdateResult.acknowledged(
                    0,
                    0,
                    null
            ) // Due to if branches, we might end up with an "empty" update
            if (logAllQueries) println(buildString {
                append("updateOne:\n")
                append("   filter: ${filter.toFilterDocument().asJsonString()}\n")
                append("   mutator: ${mutator.asJsonString()}\n")
                append("   pipeline: ${filter.getExecutionPipeline()}\n")
            })
            return internalCollection.updateOne(filter.toFilterDocument(), mutator, UpdateOptions().upsert(false))
        }

        fun updateOneOrInsert(filter: FilterPair, update: UpdateOperation.() -> Unit): UpdateResult {
            require(filter.key.name == "_id") {
                "An _id filter must be provided when interacting with only one object and no other filters are allowed to mitigate a DuplicateKeyException on update."
            }
            val mutator = UpdateOperation().apply { update(this) }.mutator

            if (logAllQueries) println(buildString {
                append("updateOneOrInsert:\n")
                append("   filter: ${arrayOf(filter).toFilterDocument().asJsonString()}\n")
                append("   mutator: ${mutator.asJsonString()}\n")
                append("   pipeline: ${arrayOf(filter).getExecutionPipeline()}\n")
            })

            return retryMongoOperationOnDuplicateKeyError {
                internalCollection.updateOne(arrayOf(filter).toFilterDocument(), mutator, UpdateOptions().upsert(true))
            }
        }

        fun updateOneAndFind(
                vararg filter: FilterPair,
                upsert: Boolean = false,
                update: UpdateOperation.() -> Unit
        ): Entry? {
            require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }
            val mutator = UpdateOperation().apply { update(this) }.mutator
            val options = FindOneAndUpdateOptions().apply {
                upsert(upsert)
                returnDocument(ReturnDocument.AFTER)
            }
            if (logAllQueries) println(buildString {
                append("updateOneAndFind:\n")
                append("   filter: ${filter.toFilterDocument().asJsonString()}\n")
                append("   mutator: ${mutator.asJsonString()}\n")
                append("   pipeline: ${filter.getExecutionPipeline()}\n")
            })

            return retryMongoOperationOnDuplicateKeyError {
                internalCollection.findOneAndUpdate(filter.toFilterDocument(), mutator, options)?.toClass()
            }
        }

        fun updateMany(vararg filter: FilterPair, update: UpdateOperation.() -> Unit): UpdateResult {
            val mutator = UpdateOperation().apply { update(this) }.mutator
            if (mutator.isEmpty()) return UpdateResult.acknowledged(
                    0,
                    0,
                    null
            ) // Due to if branches, we might end up with an "empty" update
            if (logAllQueries) println(buildString {
                append("updateMany:\n")
                append("   filter: ${filter.toFilterDocument().asJsonString()}\n")
                append("   mutator: ${mutator.asJsonString()}\n")
                append("   pipeline: ${filter.getExecutionPipeline()}\n")
            })
            return internalCollection.updateMany(filter.toFilterDocument(), mutator)
        }

        // TODO when updating to mongo-java-driver 4.0 return an InsertOneResult instead of Unit
        /** Throws on duplicate key when upsert=false */
        fun insertOne(document: Entry, upsert: Boolean) {
            if (upsert) {
                retryMongoOperationOnDuplicateKeyError {
                    internalCollection.replaceOne(
                            Document().apply { put("_id", document._id) },
                            document.toBSONDocument(),
                            ReplaceOptions().upsert(true)
                    )
                }
            } else {
                insertOne(document, onDuplicateKey = { throw DuplicateKeyException(key = document._id) })
            }
        }

        // TODO when updating to mongo-java-driver 4.0 return an InsertOneResult instead of Unit
        fun insertOne(document: Entry, onDuplicateKey: (() -> Unit)) {
            try {
                internalCollection.insertOne(document.toBSONDocument())
            } catch (e: MongoServerException) {
                if (e.code == 11000 && e.message?.matches(Regex(".*E11000 duplicate key error collection: .* index: _id_ dup key:.*")) == true) {
                    onDuplicateKey.invoke()
                } else {
                    throw e // Every other exception than duplicate key
                }
            }
        }

        fun deleteOne(vararg filter: FilterPair): DeleteResult {
            require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }
            if (logAllQueries) println("deleteOne: ${filter.toFilterDocument().asJsonString()}")
            return internalCollection.deleteOne(filter.toFilterDocument())
        }

        fun deleteMany(vararg filter: FilterPair): DeleteResult {
            if (logAllQueries) println("deleteMany: ${filter.toFilterDocument().asJsonString()}")
            return internalCollection.deleteMany(filter.toFilterDocument())
        }

        /**
         * This is an ongoing mongo issue
         * Some of this is mitigated in MongoDB 4.2 however not every operation can be retried, so this function retries every operation
         * that failed because of a *duplicate key error*.
         *
         * See https://jira.mongodb.org/browse/SERVER-14322 for more information on the specific supported and unsupported
         * operations
         */
        private fun <T> retryMongoOperationOnDuplicateKeyError(tries: Int = 2, operation: () -> T): T {
            return try {
                operation()
            } catch (e: MongoServerException) {
                if (e.code == 11000 &&
                        e.message?.matches(Regex(".*E11000 duplicate key error collection: .* index: _id_ dup key:.*")) == true &&
                        tries > 0
                ) {
                    retryMongoOperationOnDuplicateKeyError(tries = tries - 1, operation = operation)
                } else {
                    throw e // Every other exception than duplicate key
                }
            }
        }

        // Update operators
        inner class UpdateOperation {
            private val mutatorPairs: MutableMap<String, MutableList<MongoPair>> = LinkedHashMap()

            val mutator: Document
                get() = mutatorPairs.map { (op, values) -> "$$op" to values.toTypedArray().toFilterDocument() }
                        .toMap(Document())

            internal fun updateMutator(operator: String, mutator: MongoPair) {
                mutatorPairs.getOrPut(operator) { mutableListOf() }.add(mutator)
            }

            fun randomId() = MongoMainEntry.randomId()

            fun generateId(compoundValue: String, vararg compoundValues: String): String =
                    MongoMainEntry.generateId(compoundValue, *compoundValues)

            /**
             * Use this if you want to modify a subdocument's field
             */
            fun <Class, Value> MongoEntryField<out Any>.child(property: KMutableProperty1<Class, Value>): MongoEntryField<Value> {
                return this.toMongoField().extend(property.name).toProperty()
            }

            @JvmName("childOnNullableProperty")
            fun <Class, Value> NullableMongoEntryField<out Any>.child(property: KMutableProperty1<Class, Value>): MongoEntryField<Value> {
                return this.toMongoField().extend(property.name).toProperty()
            }

            /**
             * Use this if you want to modify a map's field
             */
            fun <Value> MongoEntryField<Map<String, Value>>.child(key: String): MongoEntryField<Value> {
                return this.toMongoField().extend(key).toProperty()
            }

            @JvmName("childOnNullableKey")
            fun <Value> NullableMongoEntryField<Map<String, Value>>.child(key: String): MongoEntryField<Value> {
                return this.toMongoField().extend(key).toProperty()
            }

            fun <Key : Enum<*>, Value> MongoEntryField<Map<Key, Value>>.child(key: Key): MongoEntryField<Value> {
                return this.toMongoField().extend(key.name).toProperty()
            }

            /**
             * Use this if you want to update a subdocument in an array.
             * More info: https://docs.mongodb.com/manual/reference/operator/update/positional/#update-documents-in-an-array
             */
            fun <Class, Value> MongoEntryField<out Any>.childWithCursor(property: KMutableProperty1<Class, Value>): MongoEntryField<Value> {
                return this.toMongoField().extendWithCursor(property.name).toProperty()
            }

            /**
             * Use this if you want to modify a field
             * More info: https://docs.mongodb.com/manual/reference/operator/update/set/
             */
            infix fun <Value> MongoEntryField<Value>.setTo(value: Value) {
                updateMutator(operator = "set", mutator = MutatorPair(this, value))
            }

            /**
             * Use this if you want to remove a field
             * More info: https://docs.mongodb.com/manual/reference/operator/update/unset/
             */
            fun <T> MongoEntryField<T>.unset() {
                @Suppress("DEPRECATION")
                updateMutator(operator = "unset", mutator = UnsetPair(this))
            }

            /**
             * Use this in combination with [updateOneOrInsert] if you want to set specific fields if a new document is created
             * More info: https://docs.mongodb.com/manual/reference/operator/update/setOnInsert/
             */
            infix fun <Value> MongoEntryField<Value>.setToOnInsert(value: Value) {
                updateMutator(operator = "setOnInsert", mutator = MutatorPair(this, value))
            }

            /**
             * Use this if you want to increment or decrement a numeric field
             * More info: https://docs.mongodb.com/manual/reference/operator/update/inc/
             */
            infix fun <Value : Number> MongoEntryField<Value>.incrementBy(value: Value) {
                updateMutator(operator = "inc", mutator = MutatorPair(this, value))
            }

            /**
             * Use this if you want to multiply or divide a numeric field
             * More info: https://docs.mongodb.com/manual/reference/operator/update/mul/
             */
            infix fun <Value : Number> MongoEntryField<Value>.multiplyBy(value: Value) {
                updateMutator(operator = "mul", mutator = MutatorPair(this, value))
            }

            /**
             * Use this if you want to change value if the specified value is lower than the current value of the field
             * More info: https://docs.mongodb.com/manual/reference/operator/update/min/
             */
            infix fun <Value : Number> MongoEntryField<Value>.min(value: Value) {
                updateMutator(operator = "min", mutator = MutatorPair(this, value))
            }

            /**
             * Use this if you want to change value if the specified value is greater than the current value of the field
             * More info: https://docs.mongodb.com/manual/reference/operator/update/max/
             */
            infix fun <Value : Number> MongoEntryField<Value>.max(value: Value) {
                updateMutator(operator = "max", mutator = MutatorPair(this, value))
            }

            /**
             * Use this if you want to push a new item to a list
             * More info: https://docs.mongodb.com/manual/reference/operator/update/push/
             */
            infix fun <Value> MongoEntryField<List<Value>>.push(value: Value) {
                updateMutator(operator = "push", mutator = PushPair(this, value))
            }

            /**
             * Use this if you want to push a new item to a set
             * This only works with primitives!!!!
             * More info: https://docs.mongodb.com/manual/reference/operator/update/addToSet/
             */
            @JvmName("pushToSet")
            infix fun <Value> MongoEntryField<Set<Value>>.push(value: Value) {
                @Suppress("DEPRECATION") // Use the hack version because List != Set
                updateMutator(operator = "addToSet", mutator = PushPair<Value>(this.toMongoField(), value))
            }

            /**
             * Use this if you want to remove an item from a list or set
             * More info: https://docs.mongodb.com/manual/reference/operator/update/pull/
             */
            infix fun <Value> MongoEntryField<List<Value>>.pull(value: Value) {
                updateMutator(operator = "pull", mutator = PushPair(this, value))
            }

            /**
             * Use this if you want to remove an item from a list or set
             * More info: https://docs.mongodb.com/manual/reference/operator/update/pull/
             */
            @JvmName("pullFromSet")
            infix fun <Value> MongoEntryField<Set<Value>>.pull(value: Value) {
                @Suppress("DEPRECATION")
                updateMutator(operator = "pull", mutator = PushPair<Value>(this.toMongoField(), value))
            }

            /**
             * Use this if you want to remove an subdocument with specific criteria from a list or set
             * More info: https://docs.mongodb.com/manual/reference/operator/update/pull/
             */
            fun <Value> MongoEntryField<List<Value>>.pullWhere(vararg filter: FilterPair) {
                updateMutator(
                        operator = "pull",
                        mutator = PushPair(this, Document(filter.map { it.key.fieldName to it.value }.toMap()))
                )
            }
        }

        // Bulk write operators
        inner class BulkOperation {
            val models = mutableListOf<WriteModel<Document>>()

            fun updateOne(vararg filter: FilterPair, update: UpdateOperation.() -> Unit) {
                require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }
                val mutator = UpdateOperation().apply { update(this) }.mutator
                if (mutator.isNotEmpty()) {
                    models.add(UpdateOneModel(filter.toFilterDocument(), mutator))
                }
            }

            fun updateMany(vararg filter: FilterPair, update: UpdateOperation.() -> Unit) {
                val mutator = UpdateOperation().apply { update(this) }.mutator
                if (mutator.isNotEmpty()) {
                    models.add(UpdateManyModel(filter.toFilterDocument(), mutator))
                }
            }

            fun insertOne(document: Entry, upsert: Boolean) {
                if (upsert) {
                    models.add(
                            ReplaceOneModel(
                                    Document().apply { put("_id", document._id) },
                                    document.toBSONDocument(),
                                    ReplaceOptions().upsert(true)
                            )
                    )
                } else {
                    models.add(InsertOneModel(document.toBSONDocument()))
                }
            }

            fun deleteOne(vararg filter: FilterPair): Boolean {
                require(filter.isNotEmpty()) { "A filter must be provided when interacting with only one object." }
                return models.add(DeleteOneModel(filter.toFilterDocument()))
            }

            fun deleteMany(vararg filter: FilterPair): Unit {
                models.add(DeleteManyModel(filter.toFilterDocument()))
            }
        }

        fun Array<out FilterPair>.getExecutionPipeline(): String {
            val explainCommand = Document().apply {
                put("explain", Document().apply {
                    put("find", name)
                    put("filter", this@getExecutionPipeline.toFilterDocument())
                })
                put("verbosity", "queryPlanner")
            }
            val res = internalDatabase.runCommand(explainCommand)
            val winningPlan = (res.getValue("queryPlanner") as Document).getValue("winningPlan") as Document
            fun getPipeline(stage: Document): List<String> {
                return listOf(stage.getValue("stage") as String) + ((stage["inputStage"] as? Document)?.let { inputStage ->
                    getPipeline(
                            inputStage
                    )
                }
                        ?: emptyList())
            }

            val pipeline = getPipeline(winningPlan)
            return pipeline.joinToString(separator = " < ")
        }
    }

    companion object {
        var logAllQueries = false // Set this to true if you want to debug your database queries
        private fun Document.asJsonString() = JsonHandler.toJsonString(this)

        fun createMongoClientFromUri(
                connectionString: ConnectionString,
                allowReadFromSecondaries: Boolean,
                clientSettings: (MongoClientSettings.Builder.() -> Unit)?
        ): MongoClient {
            return MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .apply {
                        when {
                            allowReadFromSecondaries -> {
                                // Set maxStalenessSeconds, see https://docs.mongodb.com/manual/core/read-preference/#maxstalenessseconds
                                readPreference(ReadPreference.secondaryPreferred(90L, TimeUnit.SECONDS))
                                readConcern(ReadConcern.MAJORITY)
                                writeConcern(WriteConcern.MAJORITY)
                            }
                            else -> {
                                readPreference(ReadPreference.primaryPreferred())
                                readConcern(ReadConcern.LOCAL)
                                writeConcern(WriteConcern.W1)
                            }
                        }

                        clientSettings?.invoke(this)
                    }
                    .build()
                    .let { MongoClients.create(it) }
        }
    }
}