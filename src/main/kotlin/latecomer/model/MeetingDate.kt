package latecomer.model

data class MeetingDate(
    val weekDay: Int,
    val hour: Int,
    val minute: Int = 0
)