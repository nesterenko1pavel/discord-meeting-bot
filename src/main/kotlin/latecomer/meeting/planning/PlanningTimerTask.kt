package latecomer.meeting.planning

import latecomer.BaseTimerTask
import latecomer.meeting.LatecomerUtil
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

class PlanningTimerTask(
    private val timer: Timer,
    private val botUserId: String,
    private val verifiableVoiceChannel: VoiceChannel,
    private val reportingTextChannel: TextChannel,
) : BaseTimerTask() {

    override fun scheduleNext() {
        PlanningLatecomerTimerTaskScheduler.schedule(
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