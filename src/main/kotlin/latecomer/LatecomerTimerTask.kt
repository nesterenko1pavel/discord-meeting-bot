package latecomer

import config.FilesConfig
import extension.saveMembersIdToFile
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.TimerTask

abstract class LatecomerTimerTask(
    private val botUserId: String,
    private val verifiableVoiceChannel: VoiceChannel,
    private val reportingTextChannel: TextChannel,
) : TimerTask() {

    override fun run() {
        sendLatecomersReport()
        scheduleNext()
    }

    protected abstract fun scheduleNext()

    private fun sendLatecomersReport() {
        val latecomers = getLatecomers()
        createLatecomersReport(latecomers)?.let { message ->
            latecomers.saveMembersIdToFile(FilesConfig.LATECOMERS_STATS_FILE)
            reportingTextChannel.sendMessage(message)
                .queue()
        }
    }

    private fun getLatecomers(): List<Member> {
        return reportingTextChannel.members.filterNot { member ->
            verifiableVoiceChannel.members.contains(member) || member.id == botUserId
        }
    }

    private fun createLatecomersReport(latecomers: List<Member>): String? {
        return if (latecomers.isNotEmpty()) {
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
    }
}