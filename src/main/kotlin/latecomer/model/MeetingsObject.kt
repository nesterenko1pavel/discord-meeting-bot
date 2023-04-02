package latecomer.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MeetingsObject(
    val meetings: List<MeetingObject>,
    val absence: AbsenceObject? = null
)

@JsonClass(generateAdapter = true)
data class MeetingObject(
    val name: String,
    val availableDays: AvailableDays,
    val verifiableVoiceChannel: Long,
    val reportingTextChannel: Long,
    val nearestMeetingTime: String? = null,
    val verifiableRoleId: String,
    val warnedMembersIds: List<String> = listOf()
)

@JsonClass(generateAdapter = true)
data class AbsenceObject(
    val absenceMembersList: List<AbsenceMemberObject>
)

@JsonClass(generateAdapter = true)
data class AbsenceMemberObject(
    val memberId: String,
    val dataStart: String,
    val dataEnd: String?
)