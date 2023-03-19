package latecomer.meeting.retro

import extension.getGregorianCalendar
import extension.setupForNearestMeetingDay
import latecomer.BaseTimerTask
import latecomer.meeting.MeetingsConfig
import logging.Logger
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Timer

object RetroLatecomerTimerTaskScheduler {

    fun schedule(
        timer: Timer,
        botUserId: String,
        verifiableVoiceChannel: VoiceChannel,
        reportingTextChannel: TextChannel,
    ): BaseTimerTask {
        val calendar = getGregorianCalendar()
        calendar.setupForNearestMeetingDay(availableWeekDays = MeetingsConfig.Retro.availableWeekDay)
        val dailyLatecomerTimerTask = RetroTimerTask(
            timer = timer,
            botUserId = botUserId,
            verifiableVoiceChannel = verifiableVoiceChannel,
            reportingTextChannel = reportingTextChannel,
        )
        timer.schedule(dailyLatecomerTimerTask, calendar.time)
        Logger.logRetroScheduled(calendar)
        return dailyLatecomerTimerTask
    }
}