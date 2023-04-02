package extension

import latecomer.model.AvailableDays
import latecomer.model.DeltaMeetingDate
import latecomer.model.MeetingDate
import latecomer.model.MonthDayDate
import latecomer.model.SimpleMeetingDate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

private const val LANGUAGE_TAG = "ru-RU"
private const val TIME_ZONE = "Europe/Moscow"

private const val ONE_DAY = 1
private const val COUNT_DAYS_IN_WEEK = 7

object CalendarPattern {

    const val COMMON = "dd-MM-yyyy HH:mm"
    const val FULL = "dd-MM-yyyy HH:mm:ss"
    const val SHORT = "dd-MM-yyyy"
}

fun parseStringDate(
    stringTime: String,
    pattern: String = CalendarPattern.COMMON
): Calendar? {
    val format = createSimpleDateFormat(pattern)
    val data = try {
        format.parse(stringTime)
    } catch (ignore: ParseException) {
        null
    }
    val calendar = if (data != null) {
        getGregorianCalendar().apply {
            time = data
        }
    } else {
        null
    }
    return calendar
}

fun parseStringDate(
    stringTime: String,
    onSuccess: (Calendar) -> Unit,
    onError: () -> Unit = {}
) {
    val format = createSimpleDateFormat(CalendarPattern.COMMON)
    val data = try {
        format.parse(stringTime)
    } catch (ignore: ParseException) {
        null
    }
    val calendar = if (data != null) {
        getGregorianCalendar().apply {
            time = data
        }
    } else {
        null
    }
    if (calendar != null) {
        onSuccess(calendar)
    } else {
        onError()
    }
}

fun getGregorianCalendar(): Calendar {
    val locale = Locale.forLanguageTag(LANGUAGE_TAG)
    val timeZone = TimeZone.getTimeZone(TIME_ZONE)
    return GregorianCalendar.getInstance(timeZone, locale)
}

fun createSimpleDateFormat(pattern: String): SimpleDateFormat {
    val format = SimpleDateFormat(pattern)
    format.timeZone = TimeZone.getTimeZone(TIME_ZONE)
    return format
}

fun Calendar.setupForNearestMeetingDay(
    availableWeekDays: AvailableDays
): Calendar {
    val currentDayOfMonth = get(Calendar.DAY_OF_MONTH)

    val deltaWeekDay = when (availableWeekDays) {
        is AvailableDays.AvailableAllWorkingDays -> getDaysDeltaOrZero(
            availableWeekDays.hour, availableWeekDays.minute
        )
        is AvailableDays.AvailableEveryWeekDays -> getDaysDeltaOrZero(
            availableWeekDays.meetingDays
        )
        is AvailableDays.AvailableEveryTwoWeekDay -> getDaysDeltaOrZero(
            availableWeekDays.meetingDate, availableWeekDays.startFrom, COUNT_DAYS_IN_WEEK * 2
        )
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

fun Calendar.getDaysDeltaOrZero(
    meetingDay: SimpleMeetingDate,
    startFrom: MonthDayDate,
    period: Int
): DeltaMeetingDate {
    val startDate = getGregorianCalendar().apply {
        set(Calendar.YEAR, startFrom.year)
        set(Calendar.MONTH, startFrom.month)
        set(Calendar.DAY_OF_MONTH, startFrom.monthDay)
    }
    val daysBetween = daysBetween(startDate, this)
    val numberIntPeriods = daysBetween / period
    val numberOfDayInPeriod = daysBetween - (period * numberIntPeriods)

    val delta = if (numberOfDayInPeriod == 0) {
        if (isMeetingExpired(meetingDay.hour, meetingDay.minute)) {
            period
        } else {
            0
        }
    } else {
        period - numberOfDayInPeriod
    }

    return DeltaMeetingDate(
        hour = meetingDay.hour,
        minute = meetingDay.minute,
        delta = delta
    )
}

private fun daysBetween(startCalendar: Calendar, endCalendar: Calendar): Int {
    return ChronoUnit.DAYS.between(startCalendar.toInstant(), endCalendar.toInstant()).toInt()
}