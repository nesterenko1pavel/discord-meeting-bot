package latecomer.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MonthDayDate(
    val monthDay: Int,
    val month: Int,
    val year: Int
)