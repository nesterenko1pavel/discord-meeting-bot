package latecomer.meeting.planning

import extension.getGregorianCalendar
import extension.setupForNearestMeetingDay
import latecomer.TaskManager
import latecomer.meeting.MeetingsConfig
import logging.Logger
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.util.Calendar
import java.util.Timer

object PlanningLatecomerTimerTaskScheduler {

    fun schedule(
        timer: Timer,
        botUserId: String,
        verifiableVoiceChannel: VoiceChannel,
        reportingTextChannel: TextChannel,
        initialCalendar: Calendar? = null
    ) {
        val calendar = initialCalendar
            ?: getGregorianCalendar().apply {
                setupForNearestMeetingDay(availableWeekDays = MeetingsConfig.Planning.availableWeekDay)
            }
        val planningLatecomerTimerTask = PlanningTimerTask(
            timer = timer,
            botUserId = botUserId,
            verifiableVoiceChannel = verifiableVoiceChannel,
            reportingTextChannel = reportingTextChannel,
        )
        timer.schedule(planningLatecomerTimerTask, calendar.time)
        Logger.logPlanningScheduled(calendar)
        TaskManager.putTask(MeetingsConfig.Planning, planningLatecomerTimerTask)
    }
}