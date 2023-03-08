package extension

import latecomer.AvailableAllWorkingDays
import latecomer.AvailableDays
import latecomer.AvailableEveryWeekDays
import latecomer.model.DeltaMeetingDate
import latecomer.model.MeetingDate
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private const val ONE_DAY = 1

fun getGregorianCalendar(): Calendar {
    val locale = Locale.forLanguageTag("ru-RU")
    val calendar = GregorianCalendar.getInstance(locale)
    calendar.timeZone = TimeZone.getTimeZone("Europe/Moscow")
    return calendar
}

fun Calendar.setupForNearestMeetingDay(
    availableWeekDays: AvailableDays
): Calendar {
    val currentDayOfMonth = get(Calendar.DAY_OF_MONTH)

    val deltaWeekDay = when (availableWeekDays) {
        is AvailableAllWorkingDays -> getDaysDeltaOrZero(availableWeekDays.hour, availableWeekDays.minute)
        is AvailableEveryWeekDays -> getDaysDeltaOrZero(availableWeekDays.meetingDays)
    }
    val monthDayOfNearestMeeting = currentDayOfMonth + deltaWeekDay.delta

    set(Calendar.DAY_OF_MONTH, monthDayOfNearestMeeting)
    set(Calendar.HOUR_OF_DAY, deltaWeekDay.hour)
    set(Calendar.MINUTE, deltaWeekDay.minute)
    set(Calendar.SECOND, 0)
    return this
}

fun Calendar.getDaysDeltaOrZero(
    meetingHour: Int,
    meetingMinute: Int,
): DeltaMeetingDate {

    val delta = when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.FRIDAY -> ONE_DAY * 3
        Calendar.SATURDAY -> ONE_DAY * 2
        Calendar.SUNDAY -> ONE_DAY
        else -> getDayDeltaIfMeetingExpiredOrZero(meetingHour, meetingMinute)
    }
    return DeltaMeetingDate(
        hour = meetingHour,
        minute = meetingMinute,
        delta = delta
    )
}

fun Calendar.getDayDeltaIfMeetingExpiredOrZero(
    meetingHour: Int,
    meetingMinute: Int,
): Int {
    return if (isMeetingExpired(meetingHour, meetingMinute)) {
        ONE_DAY
    } else {
        0
    }
}

fun Calendar.isMeetingExpired(
    meetingHour: Int,
    meetingMinute: Int,
): Boolean {
    return get(Calendar.HOUR_OF_DAY) >= meetingHour && get(Calendar.MINUTE) >= meetingMinute
}

fun Calendar.getDaysDeltaOrZero(
    availableMeetingDates: List<MeetingDate>
): DeltaMeetingDate {

    val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)

    val listDoubleWeek = doubleDuplicatedListOf(
        Calendar.MONDAY, Calendar.TUESDAY,
        Calendar.WEDNESDAY, Calendar.THURSDAY,
        Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    )
    val indexOfCurrentWeekDay = listDoubleWeek.indexOfFirst { it == currentDayOfWeek }

    val deltaMeetingDates = availableMeetingDates.map { availableElement ->

        val predicate: (Int, Int) -> Boolean = { i, item ->
            item == availableElement.weekDay && i >= indexOfCurrentWeekDay
        }

        var indexOfNextAvailableWeekDay = 0
        for ((i, item) in listDoubleWeek.withIndex()) {
            if (predicate(i, item))
                break
            indexOfNextAvailableWeekDay++
        }
        DeltaMeetingDate(
            hour = availableElement.hour,
            minute = availableElement.minute,
            delta = indexOfNextAvailableWeekDay - indexOfCurrentWeekDay
        )
    }

    val sortedDeltas = deltaMeetingDates.sortedBy { it.delta }
    val firstDelta = sortedDeltas.first()
    val finalDelta = if (
        isMeetingExpired(firstDelta.hour, firstDelta.minute) &&
        availableMeetingDates.any { it.weekDay == currentDayOfWeek }
    ) {
        sortedDeltas.second()
    } else {
        sortedDeltas.first()
    }

    return finalDelta
}