package latecomer.meeting

import latecomer.AvailableAllWorkingDays
import latecomer.AvailableEveryTwoWeekDay
import latecomer.AvailableEveryWeekDays
import latecomer.model.MeetingDate
import latecomer.model.MonthDayDate
import latecomer.model.SimpleMeetingDate
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

    object Retro {
        val availableWeekDay = AvailableEveryTwoWeekDay(
            meetingDate = SimpleMeetingDate(hour = 14),
            startFrom = MonthDayDate(monthDay = 3, month = Calendar.MARCH, 2023)
        )
    }
}