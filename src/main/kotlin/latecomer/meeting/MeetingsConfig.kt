package latecomer.meeting

import latecomer.AvailableAllWorkingDays
import latecomer.AvailableEveryWeekDays
import latecomer.model.MeetingDate
import java.util.Calendar

object MeetingsConfig {

    object Daily {
        val availableWeekDays = AvailableAllWorkingDays(hour = 11)
    }

    object Pbr {
        val availableWeekDays = AvailableEveryWeekDays(
            meetingDays = listOf(
                MeetingDate(weekDay = Calendar.MONDAY, hour = 16),
                MeetingDate(weekDay = Calendar.WEDNESDAY, hour = 15)
            )
        )
    }
}