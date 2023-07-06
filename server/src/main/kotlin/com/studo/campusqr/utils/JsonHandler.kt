package com.studo.campusqr.utils

 import com.fasterxml.jackson.core.json.JsonReadFeature
 import com.fasterxml.jackson.core.type.TypeReference
 import com.fasterxml.jackson.databind.DeserializationFeature
 import com.fasterxml.jackson.databind.MapperFeature
 import com.fasterxml.jackson.databind.ObjectMapper
 import com.fasterxml.jackson.databind.json.JsonMapper
 import com.fasterxml.jackson.module.kotlin.KotlinModule
 import kotlinx.serialization.InternalSerializationApi
 import kotlinx.serialization.KSerializer
 import kotlinx.serialization.Serializable
 import kotlinx.serialization.builtins.ListSerializer
 import kotlinx.serialization.builtins.MapSerializer
 import kotlinx.serialization.json.*
 import kotlinx.serialization.serializer
 import java.lang.reflect.Type
 import java.text.SimpleDateFormat
 import kotlin.reflect.KClass
 import kotlin.reflect.KType
 import kotlin.reflect.full.allSuperclasses
 import kotlin.reflect.full.createType
 import kotlin.reflect.full.superclasses
 import kotlin.reflect.jvm.javaType
 import kotlin.reflect.typeOf

object JsonHandler {
  private val jacksonMapper: ObjectMapper = JsonMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // Don't fail when JSON has new/additional values
    .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER) // Jackson doesn't ignore Transient members, so make it ignore them
    .enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS) // Used for Double.INFINITY
    .addModule(KotlinModule.Builder().build())
    .defaultDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"))!! // BaseSettings.java: Threadsafe, because "The configured [date] format object will be cloned once per deserialization process"
    .build()

  private val kotlinxSerializer = Json {
    classDiscriminator = "@class"
    encodeDefaults = true
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    isLenient = true // Allow to convert "true" String (e.g. used in URL) for true Boolean
  }

  /**
   * Returns the parent sealed class (as [KClass]) or null if there are no sealed classes in the class hierarchy.
   *
   * If there are multiple sealed classes in the hierarchy, return the closest one.
   * Example: ParentSealedClass1 -> ParentSealedClass2 -> CurrentClass. In this case we want to use ParentSealedClass2's serializer
   * because that is the closest one in the hierarchy.
   */
  private val KClass<*>.baseSealedClass: KClass<*>?
    get() = when {
      isSealed -> this
      else -> superclasses.find { !it.java.isInterface }?.baseSealedClass
    }

  private fun KClass<*>.hasClassInHierarchy(clazz: KClass<*>): Boolean = clazz in (allSuperclasses + this)

  private val KType.shouldSerializeToJsonList: Boolean
    get() = (classifier as? KClass<*>)?.hasClassInHierarchy(Iterable::class) ?: false

  private val KType.shouldSerializeToJsonMap: Boolean
    get() = (classifier as? KClass<*>)?.hasClassInHierarchy(Map::class) ?: false

  private val KType.hasSerializableAnnotation: Boolean
    get() = (classifier as? KClass<*>)?.java?.getAnnotation(Serializable::class.java) != null

  @Suppress("UNCHECKED_CAST")
  val KType.useKotlinxSerialization: Boolean
    get() = when {
      hasSerializableAnnotation -> {
        // Class has @Serializable annotation so always use kotlinx
        true
      }

      shouldSerializeToJsonList -> {
        val genericType = this.arguments.singleOrNull()
        if (genericType == null) {
          // KType is Iterable but doesn't have a generic type like EmptyList or other class that implements Iterable, without generics.
          // In this case it is impossible to determine the KType of the Iterable, so we just use Jackson.
          false
        } else {
          // KType is Iterable, so check the Iterable entry type to determine if we should use kotlinx.
          genericType.type!!.useKotlinxSerialization
        }
      }

      shouldSerializeToJsonMap -> {
        // KType is Map, so check key or value type to determine if we should use kotlinx.
        this.arguments.any { it.type?.useKotlinxSerialization == true }
      }

      !hasSerializableAnnotation -> {
        // Class doesn't have the @Serializable annotation or type is not denotable in Kotlin so always use jackson.
        false
      }

      else -> throw IllegalStateException("Invalid state for $this")
    }

  @OptIn(InternalSerializationApi::class)
  @Suppress("UNCHECKED_CAST")
  private fun <T : Any> KClass<T>.getSerializer(): KSerializer<T> {
    // If receiver class hase a parent sealed class then use the sealed classes serializer to support inheritance,
    // else use the receivers class serializer.
    // WARNING: This behaviour is only on JVM, on JS we cannot determine if the KClass has a baseSealedClass. This is why we cannot
    // serialize or deserialize sealed classes directly on JS, only when they are wrapped in another payload class.
    return (this.baseSealedClass ?: this).serializer() as KSerializer<T>
  }

  @Suppress("UNCHECKED_CAST")
  private fun <T : Any> KType.getSerializer(): KSerializer<T> = when {
    hasSerializableAnnotation -> (classifier as KClass<T>).getSerializer()
    shouldSerializeToJsonList -> ListSerializer(this.arguments.first().type!!.getSerializer()) as KSerializer<T>
    shouldSerializeToJsonMap -> MapSerializer(
      keySerializer = this.arguments[0].type!!.getSerializer(),
      valueSerializer = this.arguments[1].type!!.getSerializer()
    ) as KSerializer<T>

    classifier is KClass<*> -> (classifier as KClass<T>).getSerializer()
    else -> throw IllegalStateException("$this can't be serialized")
  }

  inline fun <reified T : Any> toJsonString(payload: T): String {
    return toJsonString(
      payload = payload,
      type = try {
        /**
         * Try to create a KType of [payload]'s class without any arguments.
         * This is not the same as creating a star projected type, because:
         *    1) it will work if class has no generic type parameters
         *    2) it will not work (throw) when class has any number of generic type parameters (like List<1> or Map<1,2>)
         * We explicitly want to avoid star projecting here because in case of a List<T> as [payload], we
         * would get a KType of List<*>, but we need T to decide if we want to serialize [payload] with kotlinx or jackson.
         */
        payload::class.createType()
      } catch (e: Exception) {
        /**
         * In case [payload]'s class has any number of generic type parameters fall back to using [typeOf].
         * The reason we are not using [typeOf] all the time because the [payload]'s type can be already erased. In this case [typeOf]
         * returns the KType of "Any?". As an example, see KotlinxJsonTests::typeTest()
         */
        typeOf<T>()
      }
    )
  }

  fun <T : Any> toJsonString(payload: T, type: KType): String {
    return if (type.useKotlinxSerialization) {
      kotlinxSerializer.encodeToString(type.getSerializer(), payload)
    } else {
      jacksonMapper.writeValueAsString(payload)
    }
  }

  fun <T : Any> fromJson(json: String, clazz: KClass<T>): T = fromJson(json, clazz.createType())

  fun <T : Any> fromJson(json: String, type: KType): T {
    return if (type.useKotlinxSerialization) {
      kotlinxSerializer.decodeFromString(type.getSerializer(), json)
    } else {
      // Use TypeReference here, so we support generics of fields in T with jackson.
      jacksonMapper.readValue(json, type.toTypeReference())!!
    }
  }

  inline fun <reified T : Any> fromJsonOrNull(json: String): T? {
    return try {
      fromJson(json, typeOf<T>())
    } catch (e: Exception) {
      null
    }
  }

  inline fun <reified T : Any> fromJson(json: Map<String, Any?>): T = fromJson(json, typeOf<T>())

  fun <T : Any> fromJson(json: Map<String, Any?>, clazz: KClass<T>): T = fromJson(json, clazz.createType())

  fun <T : Any> fromJson(json: Map<String, Any?>, type: KType): T {
    return if (type.useKotlinxSerialization) {
      kotlinxSerializer.decodeFromJsonElement(type.getSerializer(), json.toJsonElement())
    } else {
      // Use TypeReference here, so we support generics of fields in T with jackson.
      jacksonMapper.convertValue(json, type.toTypeReference())!!
    }
  }

  inline fun <reified T : Any> toJsonMap(payload: T): Map<String, Any?> = toJsonMap(payload, typeOf<T>())

  @Suppress("UNCHECKED_CAST")
  fun <T : Any> toJsonMap(payload: T, type: KType): Map<String, Any?> {
    return if (type.useKotlinxSerialization) {
      kotlinxSerializer.encodeToJsonElement(type.getSerializer(), payload).fromJsonElement() as Map<String, Any?>
    } else {
      jacksonMapper.convertValue(payload, Map::class.java) as Map<String, Any?>
    }
  }

  private fun Any?.toJsonElement(): JsonElement {
    fun Array<*>.toJsonArray() = JsonArray(content = this.map { it.toJsonElement() })
    fun List<*>.toJsonArray() = JsonArray(content = this.map { it.toJsonElement() })
    fun Map<*, *>.toJsonObject() = JsonObject(content = this.entries.associate { it.key.toString() to it.value.toJsonElement() })

    return when (this) {
      null -> JsonNull
      is Number -> JsonPrimitive(this)
      is Boolean -> JsonPrimitive(this)
      is String -> JsonPrimitive(this)
      is Enum<*> -> JsonPrimitive(this.name)
      is Array<*> -> this.toJsonArray()
      is List<*> -> this.toJsonArray()
      is Map<*, *> -> this.toJsonObject()
      is JsonElement -> this
      else -> throw IllegalArgumentException("Can't convert ${this::class} to JsonElement")
    }
  }

  private fun JsonElement.fromJsonElement(): Any? {
    return when (this) {
      JsonNull -> null
      is JsonArray -> this.map { it.fromJsonElement() }
      is JsonObject -> this.mapValues { it.value.fromJsonElement() }
      is JsonPrimitive -> booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
    }
  }
}

/**
 * Converts [KType] (kotlin representation) to [TypeReference] (jackson representation)
 */
internal fun <T : Any> KType.toTypeReference(): TypeReference<T> = object : TypeReference<T>() {
  override fun getType(): Type = this@toTypeReference.javaType
}