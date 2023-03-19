package latecomer.meeting

import config.FilesConfig
import extension.saveMembersIdToFile
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel

object LatecomerUtil {

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