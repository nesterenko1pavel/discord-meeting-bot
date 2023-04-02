package latecomer.model

sealed class AvailableDays {

    data class AvailableAllWorkingDays(
        val hour: Int,
        val minute: Int = 0
    ) : AvailableDays()

    data class AvailableEveryWeekDays(
        val meetingDays: List<MeetingDate>
    ) : AvailableDays()

    data class AvailableEveryTwoWeekDay(
        val meetingDate: SimpleMeetingDate,
        val startFrom: MonthDayDate
    ) : AvailableDays()
}