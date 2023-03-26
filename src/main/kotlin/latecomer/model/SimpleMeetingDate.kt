package latecomer.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimpleMeetingDate(
    val hour: Int,
    val minute: Int = 0
)