package latecomer.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeetingDate(
    val weekDay: Int,
    val hour: Int,
    val minute: Int = 0
)