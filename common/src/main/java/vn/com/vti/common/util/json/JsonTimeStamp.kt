package vn.com.vti.common.util.json

import android.os.Parcelable
import com.google.gson.*
import kotlinx.parcelize.Parcelize
import vn.com.vti.common.util.extension.DateTimeXs
import vn.com.vti.common.util.extension.DateTimeXs.TIMEZONE_UTC
import vn.com.vti.common.util.extension.DateTimeXs.getSimpleDateFormat
import vn.com.vti.common.util.extension.DateTimeXs.toTimeString
import java.io.Serializable
import java.lang.reflect.Type

@Parcelize
data class JsonTimeStamp(val timeInMillis: Long) : Parcelable, Serializable {

    companion object {

        fun now() = JsonTimeStamp(System.currentTimeMillis())

        private val sLocalInstantFormatter =
            getSimpleDateFormat(DateTimeXs.FORMAT_DATE_TIME_ISO_LOCAL)

        private val sUtcInstantFormatter =
            getSimpleDateFormat(DateTimeXs.FORMAT_DATE_TIME_ISO, TIMEZONE_UTC)
    }

    fun prettify(pattern: String) = timeInMillis.toTimeString(pattern)

    fun localInstant() = timeInMillis.toTimeString(sLocalInstantFormatter)

    fun utcInstant() = timeInMillis.toTimeString(sUtcInstantFormatter)
}

class JsonTimeStampTypeAdapter(pattern: String) : JsonDeserializer<JsonTimeStamp?>,
    JsonSerializer<JsonTimeStamp?> {

    private val formatter = getSimpleDateFormat(pattern = pattern, timeZone = TIMEZONE_UTC)

    override fun deserialize(
        json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?
    ): JsonTimeStamp? {
        return when (json) {
            null -> null
            is JsonNull -> null
            is JsonPrimitive -> (json.asJsonPrimitive.asString).let { jsonString ->
                if (jsonString.isNullOrEmpty()) null
                else {
                    // BUGS: from BE, some date fields are not in the correct pattern,
                    // they miss 'UTC-timezone' (Z) at the end
                    // so we need to check and append it before parse
                    if (jsonString.endsWith('Z')) formatter.parse(jsonString)
                        ?.let { JsonTimeStamp(it.time) }
                    else formatter.parse(jsonString + 'Z')?.let { JsonTimeStamp(it.time) }
                        ?: throw JsonParseException("Cannot parse from ${formatter.toPattern()} of $json")
                }
            }
            else -> throw JsonParseException("UtcTime must be JsonPrimitive but $json")
        }
    }

    override fun serialize(
        src: JsonTimeStamp?, typeOfSrc: Type?, context: JsonSerializationContext?
    ): JsonElement = if (src == null) JsonNull.INSTANCE
    else JsonPrimitive(src.timeInMillis.toTimeString(formatter))
}