package latecomer

import config.FilesConfig
import extension.CalendarPattern
import extension.getGregorianCalendar
import extension.parseStringDate
import extension.saveMembersIdToFile
import net.dv8tion.jda.api.entities.Member
import java.io.File
import java.util.Calendar

object LatecomerUtil {

    fun createLatecomersStats(members: List<Member>?): String {
        val file = File(FilesConfig.LATECOMERS_STATS_FILE)

        val latecomersMap = mutableMapOf<String, Int>()
        members?.forEach { member ->
            latecomersMap[member.id] = 0
        }

        file.readLines().forEach { line ->
            latecomersMap[line] = latecomersMap[line]?.plus(1) ?: -1
        }

        val message = StringBuilder()
        latecomersMap.forEach { (id, count) ->
            val member = members?.firstOrNull { member -> member.id == id }
            message.appendLine("${member?.user?.name} - $count")
        }
        return message.toString()
    }

    fun verifyLatecomers(
        guildId: String,
        membersInVerifiableChannel: List<Member>,
        membersForVerification: List<Member>,
        warnedMembersIds: List<String>,
        shouldSaveStats: Boolean = true,
        onNotEmptyLatecomers: (String) -> Unit
    ) {
        val latecomers = getLatecomers(guildId, membersInVerifiableChannel, membersForVerification, warnedMembersIds)

        val reportMessage = if (latecomers.isNotEmpty()) {
            val latecomersMessage = StringBuilder()
            latecomers.forEach { member ->
                latecomersMessage.append("${member.asMention} ")
            }
            if (latecomers.count() == 1) {
                latecomersMessage.append("you are late")
            } else {
                latecomersMessage.append("you are all late")
            }
            latecomersMessage.toString()
        } else {
            null
        }

        reportMessage?.let { message ->
            if (shouldSaveStats) {
                latecomers.saveMembersIdToFile(FilesConfig.LATECOMERS_STATS_FILE)
            }
            onNotEmptyLatecomers(message)
        }
    }

    private fun getLatecomers(
        guildId: String,
        membersInVerifiableChannel: List<Member>,
        membersForVerification: List<Member>,
        warnedMembersIds: List<String>
    ): List<Member> {
        val absenceObject = MeetingsUtil.provideGuildObjectByGuildId(guildId)?.absence

        return membersForVerification.filterNot { member ->

            val isAbsence = absenceObject?.absenceMembersList?.firstOrNull { it.memberId == member.id }?.let { absenceMember ->
                val currentCalendar = getGregorianCalendar()

                val dateStart = parseStringDate(absenceMember.dataStart, CalendarPattern.SHORT)
                val dateEnd = absenceMember.dataEnd?.let { parseStringDate(it, CalendarPattern.SHORT) }

                if (dateEnd == null && dateStart != null) {
                    currentCalendar.get(Calendar.DAY_OF_MONTH) == dateStart.get(Calendar.DAY_OF_MONTH) &&
                    currentCalendar.get(Calendar.MONTH) == dateStart.get(Calendar.MONTH) &&
                    currentCalendar.get(Calendar.YEAR) == dateStart.get(Calendar.YEAR)
                } else if (dateEnd != null && dateStart != null) {
                    currentCalendar >= dateStart && currentCalendar <= dateEnd
                } else {
                    false
                }
            } ?: false

            val isMemberBot = member.roles.any { it.tags.isBot }
            val isMemberInVoiceChannel = membersInVerifiableChannel.contains(member)
            val isMemberWarned = warnedMembersIds.contains(member.id)

            isMemberInVoiceChannel || isMemberBot || isMemberWarned || isAbsence
        }
    }
}