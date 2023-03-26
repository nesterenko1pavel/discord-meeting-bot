package latecomer.meeting.universal

import extension.getGregorianCalendar
import extension.setupForNearestMeetingDay
import latecomer.AvailableDays
import latecomer.TaskManager
import logging.Logger
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Calendar
import java.util.Timer

object UniversalTimerTaskScheduler {

    fun schedule(
        timer: Timer,
        botUserId: String,
        verifiableVoiceChannel: VoiceChannel,
        reportingTextChannel: TextChannel,
        availableDays: AvailableDays,
        meetingName: String,
        initialCalendar: Calendar? = null
    ) {
        val calendar = initialCalendar
            ?: getGregorianCalendar().apply {
                setupForNearestMeetingDay(availableDays)
            }
        val universalTimerTask = UniversalTimerTask(
            timer = timer,
            botUserId = botUserId,
            verifiableVoiceChannel = verifiableVoiceChannel,
            reportingTextChannel = reportingTextChannel,
            availableDays = availableDays,
            meetingName = meetingName,

        )
        timer.schedule(universalTimerTask, calendar.time)
        Logger.logMeetingScheduled(calendar, meetingName)
        TaskManager.putTask(meetingName, universalTimerTask)
    }
}