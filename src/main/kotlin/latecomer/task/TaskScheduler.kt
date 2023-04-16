package latecomer.task

import extension.getGregorianCalendar
import extension.parseStringDate
import extension.setupForNearestMeetingDay
import latecomer.MeetingsUtil
import latecomer.model.GuildObject
import latecomer.model.MeetingObject
import logging.Logger
import net.dv8tion.jda.api.JDA
import java.util.Calendar
import java.util.Timer

object TaskScheduler {

    private lateinit var bot: JDA

    private val timer = Timer()

    fun init(bot: JDA) {
        TaskScheduler.bot = bot
    }

    fun scheduleAllGuildsMeetings(guilds: Map<String, GuildObject>) {
        guilds.forEach { guild ->
            scheduleAll(guild.key, guild.value.meetings)
        }
    }

    fun scheduleAll(guildId: String, meetings: List<MeetingObject>) {
        meetings.forEach { schedule(guildId, it) }
    }

    fun schedule(guildId: String, meeting: MeetingObject) {
        val parsedCalendar = meeting.nearestDelayedMeetingTime?.let { parseStringDate(it) }
        if (parsedCalendar != null) {
            val nowCalendar = getGregorianCalendar()
            val isTimeOverdue = nowCalendar >= parsedCalendar

            if (isTimeOverdue) {
                MeetingsUtil.updateNextMeetingTime(guildId, meeting.name, nextTime = null)
                schedule(guildId, meeting, initialCalendar = null)
            } else {
                schedule(guildId, meeting, parsedCalendar)
            }
        } else {
            schedule(guildId, meeting, initialCalendar = null)
        }
    }

    fun reschedule(guildId: String, meetingName: String, initialCalendar: Calendar, meetingStringDate: String) {
        MeetingsUtil.provideMeetingByName(guildId, meetingName)?.let { meeting ->
            schedule(guildId, meeting, initialCalendar)
            MeetingsUtil.updateNextMeetingTime(guildId, meetingName, meetingStringDate)
        }
    }

    fun reschedule(guildId: String, meetingName: String) {
        MeetingsUtil.provideMeetingByName(guildId, meetingName)?.let { meetingObject ->
            schedule(guildId, meetingObject)
        }
    }

    private fun schedule(guildId: String, meetingObject: MeetingObject, initialCalendar: Calendar?) {
        val actualMeetingObject = MeetingsUtil.provideMeetingByName(guildId, meetingObject.name) ?: return

        val currentGuild = bot.getGuildById(guildId) ?: return

        val reportingTextChannel = currentGuild.getTextChannelById(actualMeetingObject.reportingTextChannel) ?: return
        val verifiableVoiceChannel = currentGuild.getVoiceChannelById(actualMeetingObject.verifiableVoiceChannel) ?: return
        val verifiableRole = currentGuild.roles.firstOrNull { role -> role.id == actualMeetingObject.verifiableRoleId } ?: return

        val calendar = initialCalendar
            ?: getGregorianCalendar().apply {
                setupForNearestMeetingDay(actualMeetingObject.availableDays)
            }
        val universalTimerTask = UniversalTimerTask(
            guildId, reportingTextChannel, verifiableVoiceChannel, verifiableRole, actualMeetingObject
        )

        timer.schedule(universalTimerTask, calendar.time)
        Logger.logMeetingScheduled(currentGuild.name, calendar, actualMeetingObject.name)
        TaskManager.putTask(actualMeetingObject.name, universalTimerTask)
    }
}