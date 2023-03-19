package latecomer.meeting

import config.FilesConfig
import extension.getFirstTextChannelByName
import extension.getFirstVoiceChannelByName
import extension.getProperty
import extension.getSimpleClassName
import latecomer.TaskManager
import latecomer.meeting.daily.DailyLatecomerTimerTaskScheduler
import latecomer.meeting.pbr.PbrLatecomerTimerTaskScheduler
import latecomer.meeting.planning.PlanningLatecomerTimerTaskScheduler
import latecomer.meeting.retro.RetroLatecomerTimerTaskScheduler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.io.File
import java.util.Calendar
import java.util.Timer

object TaskScheduler {

    private lateinit var verifiableVoiceChannel: VoiceChannel
    private lateinit var reportingTextChannel: TextChannel
    private lateinit var botSelfUserId: String

    private val timer = Timer()

    fun init(bot: JDA) {
        val projectPropertiesFile = File(FilesConfig.PROJECT_PROPERTIES_FILE)

        verifiableVoiceChannel = bot.getFirstVoiceChannelByName(
            projectPropertiesFile.getProperty("monitored_voice_channel")
        )
        reportingTextChannel = bot.getFirstTextChannelByName(
            projectPropertiesFile.getProperty("messages_channel")
        )
        botSelfUserId = bot.selfUser.id
    }

    fun scheduleDaily(initialCalendar: Calendar? = null) {
        DailyLatecomerTimerTaskScheduler.schedule(
            timer, botSelfUserId, verifiableVoiceChannel, reportingTextChannel, initialCalendar
        )
    }

    fun schedulePbr(initialCalendar: Calendar? = null) {
        PbrLatecomerTimerTaskScheduler.schedule(
            timer, botSelfUserId, verifiableVoiceChannel, reportingTextChannel, initialCalendar
        )
    }

    fun scheduleRetro(initialCalendar: Calendar? = null) {
        RetroLatecomerTimerTaskScheduler.schedule(
            timer, botSelfUserId, verifiableVoiceChannel, reportingTextChannel, initialCalendar
        )
    }

    fun schedulePlanning(initialCalendar: Calendar? = null) {
        PlanningLatecomerTimerTaskScheduler.schedule(
            timer, botSelfUserId, verifiableVoiceChannel, reportingTextChannel, initialCalendar
        )
    }

    fun rescheduleMeeting(meetingName: String, initialCalendar: Calendar) {
        TaskManager.cancel(meetingName)
        when (meetingName) {
            MeetingsConfig.Daily.getSimpleClassName() -> scheduleDaily(initialCalendar)
            MeetingsConfig.Pbr.getSimpleClassName() -> schedulePbr(initialCalendar)
            MeetingsConfig.Retro.getSimpleClassName() -> scheduleRetro(initialCalendar)
            MeetingsConfig.Planning.getSimpleClassName() -> schedulePlanning(initialCalendar)
        }
    }
}