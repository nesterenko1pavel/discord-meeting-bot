package latecomer.meeting.pbr

import latecomer.BaseTimerTask
import latecomer.meeting.LatecomerUtil
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

class PbrTimerTask(
    private val timer: Timer,
    private val botUserId: String,
    private val verifiableVoiceChannel: VoiceChannel,
    private val reportingTextChannel: TextChannel,
) : BaseTimerTask() {

    override fun scheduleNext() {
        PbrLatecomerTimerTaskScheduler.schedule(
            timer, botUserId, verifiableVoiceChannel, reportingTextChannel
        )
    }

    override fun onRunTask() {
        LatecomerUtil.verifyLatecomers(
            reportingTextChannel, verifiableVoiceChannel, botUserId
        ) { reportMessage ->
            reportingTextChannel.sendMessage(reportMessage)
                .queue()
        }
    }
}