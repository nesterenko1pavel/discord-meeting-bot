package latecomer

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel
import latecomer.model.MeetingDate
import latecomer.model.MonthDayDate
import latecomer.model.SimpleMeetingDate

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class AvailableDays(
    open val type: AvailableDaysType
) {

    enum class AvailableDaysType {
        ALL_WORKING_DAYS,
        EVERY_WEEK_DAYS,
        EVERY_TWO_WEEK_DAY
    }

    @TypeLabel("ALL_WORKING_DAYS")
    @JsonClass(generateAdapter = true)
    data class AvailableAllWorkingDays(
        override val type: AvailableDaysType = AvailableDaysType.ALL_WORKING_DAYS,
        val hour: Int,
        val minute: Int = 0
    ) : AvailableDays(type)

    @TypeLabel("EVERY_WEEK_DAYS")
    @JsonClass(generateAdapter = true)
    data class AvailableEveryWeekDays(
        override val type: AvailableDaysType = AvailableDaysType.EVERY_WEEK_DAYS,
        val meetingDays: List<MeetingDate>
    ) : AvailableDays(type)

    @TypeLabel("EVERY_TWO_WEEK_DAY")
    @JsonClass(generateAdapter = true)
    data class AvailableEveryTwoWeekDay(
        override val type: AvailableDaysType = AvailableDaysType.EVERY_TWO_WEEK_DAY,
        val meetingDate: SimpleMeetingDate,
        val startFrom: MonthDayDate
    ) : AvailableDays(type)
}