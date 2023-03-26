package latecomer.meeting.universal

import latecomer.AvailableDays
import latecomer.BaseTimerTask
import latecomer.meeting.LatecomerUtil
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

class UniversalTimerTask(
    private val timer: Timer,
    private val botUserId: String,
    private val verifiableVoiceChannel: VoiceChannel,
    private val reportingTextChannel: TextChannel,
    private val availableDays: AvailableDays,
    private val meetingName: String,
) : BaseTimerTask() {

    override fun scheduleNext() {
        UniversalTimerTaskScheduler.schedule(
            timer, botUserId, verifiableVoiceChannel, reportingTextChannel, availableDays, meetingName
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