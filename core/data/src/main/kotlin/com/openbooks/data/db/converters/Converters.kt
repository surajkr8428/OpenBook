package com.openbooks.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.time.Instant
import java.util.*

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun toInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun fromStringList(value: String?): List<String> = value?.let {
        gson.fromJson(it, stringListType)
    } ?: emptyList()

    @TypeConverter
    fun toStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun fromMap(value: String?): Map<String, String> = value?.let {
        gson.fromJson(it, mapType)
    } ?: emptyMap()

    @TypeConverter
    fun toMap(map: Map<String, String>): String = gson.toJson(map)

    companion object {
        private val stringListType: Type = object : TypeToken<List<String>>() {}.type
        private val mapType: Type = object : TypeToken<Map<String, String>>() {}.type
    }
}