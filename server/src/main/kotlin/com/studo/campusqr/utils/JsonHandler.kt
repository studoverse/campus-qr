package com.studo.campusqr.utils

 import com.fasterxml.jackson.core.json.JsonReadFeature
 import com.fasterxml.jackson.databind.DeserializationFeature
 import com.fasterxml.jackson.databind.MapperFeature
 import com.fasterxml.jackson.databind.ObjectMapper
 import com.fasterxml.jackson.databind.json.JsonMapper
 import com.fasterxml.jackson.module.kotlin.KotlinModule
 import com.moshbit.katerbase.JsonHandler.constructCollectionType
 import com.moshbit.katerbase.JsonHandler.fromTree
 import java.text.SimpleDateFormat
 import kotlin.reflect.KClass

object JsonHandler {
  private val clientJacksonMapper: ObjectMapper = JsonMapper.builder()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // Don't fail when JSON has new/additional values
    .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER) // Jackson doesn't ignore Transient members, so make it ignore them
    .enable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
    .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
    .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS) // Used for Double.INFINITY
    .addModule(KotlinModule())
    .defaultDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX"))!! // BaseSettings.java: Threadsafe, because "The configured [date] format object will be cloned once per deserialization process"
    .build()

  fun <T : Any> toJsonString(payload: T): String = clientJacksonMapper.writeValueAsString(payload)

  fun <T : Any> fromJson(json: String, clazz: KClass<T>): T =
    clientJacksonMapper.copy().fromTree(clientJacksonMapper.copy().readTree(json), clazz)

  fun <T : Any> fromJson(json: String, clazz: Class<T>): T = clientJacksonMapper.readValue(json, clazz)!!

  inline fun <reified T : Any> fromJson(json: String): T = fromJson(json, T::class)

  fun toJsonMap(json: String): Map<String, Any?> = fromJson(json)

  fun <T : Any> fromJsonList(json: String, clazz: KClass<T>): List<T> =
    clientJacksonMapper.readValue(json, clientJacksonMapper.constructCollectionType(clazz))!!

  inline fun <reified T : Any> fromJsonList(json: String): List<T> = fromJsonList(json, T::class)

  fun <T : Any> toJsonMap(payload: T): Map<String, Any?> {
    @Suppress("UNCHECKED_CAST")
    return clientJacksonMapper.convertValue(payload, Map::class.java) as Map<String, Any?>
  }
}