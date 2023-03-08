package latecomer.meeting.daily

import latecomer.LatecomerTimerTask
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

class DailyTimerTask(
    private val timer: Timer,
    private val botUserId: String,
    private val verifiableVoiceChannel: VoiceChannel,
    private val reportingTextChannel: TextChannel,
) : LatecomerTimerTask(botUserId, verifiableVoiceChannel, reportingTextChannel) {

    override fun scheduleNext() {
        DailyLatecomerTimerTaskScheduler.schedule(
            timer, botUserId, verifiableVoiceChannel, reportingTextChannel
        )
    }
}