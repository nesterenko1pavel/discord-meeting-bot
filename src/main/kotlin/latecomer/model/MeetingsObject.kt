package latecomer.model

data class MeetingsObject(
    val meetings: List<MeetingObject>,
    val absence: AbsenceObject? = null
)

data class MeetingObject(
    val name: String,
    val availableDays: AvailableDays,
    val verifiableVoiceChannel: Long,
    val reportingTextChannel: Long,
    val nearestMeetingTime: String? = null,
    val verifiableRoleId: String,
    val warnedMembersIds: List<String> = listOf()
)

data class AbsenceObject(
    val absenceMembersList: List<AbsenceMemberObject>
)

data class AbsenceMemberObject(
    val memberId: String,
    val dataStart: String,
    val dataEnd: String?
)