package latecomer.meeting

import config.FilesConfig
import extension.saveMembersIdToFile
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.io.File

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
        reportingTextChannel: TextChannel,
        verifiableVoiceChannel: VoiceChannel,
        botUserId: String,
        shouldSaveStats: Boolean = true,
        onNotEmptyLatecomers: (String) -> Unit
    ) {
        val latecomers = getLatecomers(reportingTextChannel, verifiableVoiceChannel, botUserId)

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
        reportingTextChannel: TextChannel,
        verifiableVoiceChannel: VoiceChannel,
        botUserId: String
    ): List<Member> {
        return reportingTextChannel.members.filterNot { member ->
            verifiableVoiceChannel.members.contains(member) || member.id == botUserId
        }
    }
}