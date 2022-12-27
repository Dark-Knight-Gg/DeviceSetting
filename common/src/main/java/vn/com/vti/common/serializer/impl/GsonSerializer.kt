package vn.com.vti.common.serializer.impl

import com.google.gson.Gson
import vn.com.vti.common.serializer.Serializer
import java.lang.reflect.Type
import javax.inject.Inject

class GsonSerializer @Inject constructor(private val gson: Gson) : Serializer {

    override fun serialize(source: Any): String {
        return gson.toJson(source)
    }

    override fun <T> deserialize(source: String, clazz: Class<T>): T {
        return gson.fromJson(source, clazz)
    }

    override fun <T> deserialize(source: String, typeOfT: Type): T {
        return gson.fromJson(source, typeOfT)
    }

}