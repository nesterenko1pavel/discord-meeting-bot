package extension

import latecomer.AvailableAllWorkingDays
import latecomer.AvailableDays
import latecomer.AvailableEveryWeekDays
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
    meetingHour: Int,
    meetingMinute: Int,
    availableWeekDays: AvailableDays = AvailableAllWorkingDays
): Calendar {
    val currentDayOfMonth = get(Calendar.DAY_OF_MONTH)

    val daysDelta = when (availableWeekDays) {
        AvailableAllWorkingDays -> getDaysDeltaOrZero(meetingHour, meetingMinute)
        is AvailableEveryWeekDays -> getDaysDeltaOrZero(
            meetingHour,
            meetingMinute,
            availableWeekDays.weekDays
        )
    }
    val monthDayOfNearestMeeting = currentDayOfMonth + daysDelta

    set(Calendar.DAY_OF_MONTH, monthDayOfNearestMeeting)
    set(Calendar.HOUR_OF_DAY, meetingHour)
    set(Calendar.MINUTE, meetingMinute)
    set(Calendar.SECOND, 0)
    return this
}

fun Calendar.getDaysDeltaOrZero(
    meetingHour: Int,
    meetingMinute: Int,
): Int {

    return when (get(Calendar.DAY_OF_WEEK)) {
        Calendar.FRIDAY -> ONE_DAY * 3
        Calendar.SATURDAY -> ONE_DAY * 2
        Calendar.SUNDAY -> ONE_DAY
        else -> getDayDeltaIfMeetingExpiredOrZero(meetingHour, meetingMinute)
    }
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
    meetingHour: Int,
    meetingMinute: Int,
    availableWeekDays: List<Int>
): Int {

    val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)

    val listDoubleWeek = doubleDuplicatedListOf(
        Calendar.MONDAY, Calendar.TUESDAY,
        Calendar.WEDNESDAY, Calendar.THURSDAY,
        Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    )
    val indexOfCurrentWeekDay = listDoubleWeek.indexOfFirst { it == currentDayOfWeek }

    val deltas = availableWeekDays.map { availableElement ->

        val predicate: (Int, Int) -> Boolean = { i, item ->
            item == availableElement && i >= indexOfCurrentWeekDay
        }

        var indexOfNextAvailableWeekDay = 0
        for ((i, item) in listDoubleWeek.withIndex()) {
            if (predicate(i, item))
                break
            indexOfNextAvailableWeekDay++
        }
        indexOfNextAvailableWeekDay - indexOfCurrentWeekDay
    }

    val sortedDeltas = deltas.sorted()
    println(sortedDeltas)
    val finalDelta = if (
        isMeetingExpired(meetingHour, meetingMinute) &&
        availableWeekDays.contains(currentDayOfWeek)
    ) {
        sortedDeltas.second()
    } else {
        sortedDeltas.first()
    }

    return finalDelta
}