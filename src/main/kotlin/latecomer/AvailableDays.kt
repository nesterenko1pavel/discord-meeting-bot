package latecomer

sealed interface AvailableDays

object AvailableAllWorkingDays : AvailableDays

data class AvailableEveryWeekDays(
    val weekDays: List<Int>
): AvailableDays