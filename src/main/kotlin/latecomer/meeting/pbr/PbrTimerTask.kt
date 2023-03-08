package latecomer.meeting.pbr

import latecomer.LatecomerTimerTask
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

class PbrTimerTask(
    private val timer: Timer,
    private val botUserId: String,
    private val verifiableVoiceChannel: VoiceChannel,
    private val reportingTextChannel: TextChannel,
) : LatecomerTimerTask(botUserId, verifiableVoiceChannel, reportingTextChannel) {

    override fun scheduleNext() {
        PbrLatecomerTimerTaskScheduler.schedule(
            timer, botUserId, verifiableVoiceChannel, reportingTextChannel
        )
    }
}