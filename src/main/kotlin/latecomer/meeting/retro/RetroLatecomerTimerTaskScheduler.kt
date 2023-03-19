package latecomer.meeting.retro

import extension.getGregorianCalendar
import extension.setupForNearestMeetingDay
import latecomer.TaskManager
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
    ) {
        val calendar = getGregorianCalendar()
        calendar.setupForNearestMeetingDay(availableWeekDays = MeetingsConfig.Retro.availableWeekDay)
        val retroLatecomerTimerTask = RetroTimerTask(
            timer = timer,
            botUserId = botUserId,
            verifiableVoiceChannel = verifiableVoiceChannel,
            reportingTextChannel = reportingTextChannel,
        )
        timer.schedule(retroLatecomerTimerTask, calendar.time)
        Logger.logRetroScheduled(calendar)
        TaskManager.putTask(MeetingsConfig.Retro, retroLatecomerTimerTask)
    }
}