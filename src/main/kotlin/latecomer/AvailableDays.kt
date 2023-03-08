package latecomer

import latecomer.model.MeetingDate

sealed interface AvailableDays

data class AvailableAllWorkingDays(
    val hour: Int,
    val minute: Int = 0
) : AvailableDays

data class AvailableEveryWeekDays(
    val meetingDays: List<MeetingDate>
): AvailableDays