package latecomer.meeting.daily

import extension.getGregorianCalendar
import extension.setupForNearestMeetingDay
import latecomer.LatecomerTimerTask
import latecomer.meeting.MeetingsConfig
import logging.Logger
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

object DailyLatecomerTimerTaskScheduler {

    fun schedule(
        timer: Timer,
        botUserId: String,
        verifiableVoiceChannel: VoiceChannel,
        reportingTextChannel: TextChannel,
    ): LatecomerTimerTask {
        val calendar = getGregorianCalendar()
        calendar.setupForNearestMeetingDay(MeetingsConfig.Daily.availableWeekDays)
        val dailyLatecomerTimerTask = DailyTimerTask(
            timer = timer,
            botUserId = botUserId,
            verifiableVoiceChannel = verifiableVoiceChannel,
            reportingTextChannel = reportingTextChannel,
        )
        timer.schedule(dailyLatecomerTimerTask, calendar.time)
        Logger.logDailyScheduled(calendar)
        return dailyLatecomerTimerTask
    }
}