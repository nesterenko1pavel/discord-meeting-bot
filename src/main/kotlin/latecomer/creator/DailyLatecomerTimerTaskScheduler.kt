package latecomer.creator

import extension.getGregorianCalendar
import extension.setupForNearestMeetingDay
import latecomer.LatecomerTimerTask
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

private const val DAILY_HOUR = 11
private const val DAILY_MINUTE = 0

object DailyLatecomerTimerTaskScheduler {

    fun schedule(
        timer: Timer,
        botUserId: String,
        verifiableVoiceChannel: VoiceChannel,
        reportingTextChannel: TextChannel,
    ): LatecomerTimerTask {
        val calendar = getGregorianCalendar()
        calendar.setupForNearestMeetingDay(meetingHour = DAILY_HOUR, meetingMinute = DAILY_MINUTE)
        val dailyLatecomerTimerTask = LatecomerTimerTask(
            timer = timer,
            calendar = calendar,
            botUserId = botUserId,
            verifiableVoiceChannel = verifiableVoiceChannel,
            reportingTextChannel = reportingTextChannel,
            meetingHour = DAILY_HOUR,
            meetingMinute = DAILY_MINUTE
        )
        timer.schedule(dailyLatecomerTimerTask, calendar.time)
        return dailyLatecomerTimerTask
    }
}