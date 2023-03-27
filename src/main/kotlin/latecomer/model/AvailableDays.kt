package latecomer.model

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class AvailableDays {

    @TypeLabel("ALL_WORKING_DAYS")
    @JsonClass(generateAdapter = true)
    data class AvailableAllWorkingDays(
        val hour: Int,
        val minute: Int = 0
    ) : AvailableDays()

    @TypeLabel("EVERY_WEEK_DAYS")
    @JsonClass(generateAdapter = true)
    data class AvailableEveryWeekDays(
        val meetingDays: List<MeetingDate>
    ) : AvailableDays()

    @TypeLabel("EVERY_TWO_WEEK_DAY")
    @JsonClass(generateAdapter = true)
    data class AvailableEveryTwoWeekDay(
        val meetingDate: SimpleMeetingDate,
        val startFrom: MonthDayDate
    ) : AvailableDays()
}