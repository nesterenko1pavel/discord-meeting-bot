package latecomer.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeetingsObject(
    val meetings: List<MeetingObject>
)

@JsonClass(generateAdapter = true)
data class MeetingObject(
    val name: String,
    val availableDays: AvailableDays,
    val verifiableVoiceChannel: Long,
    val reportingTextChannel: Long,
    val nearestMeetingTime: String? = null,
    val verifiableRoleId: String
)