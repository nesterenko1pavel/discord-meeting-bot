package latecomer.task

import extension.getGregorianCalendar
import extension.parseStringDate
import extension.setupForNearestMeetingDay
import latecomer.MeetingsUtil
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

    fun scheduleAll(meetings: List<MeetingObject>) {
        meetings.forEach { schedule(it) }
    }

    fun schedule(meeting: MeetingObject) {
        parseStringDate(
            stringTime = meeting.nearestMeetingTime.orEmpty(),
            onSuccess = { calendar ->
                val nowCalendar = getGregorianCalendar()
                val isTimeOverdue = nowCalendar >= calendar

                if (isTimeOverdue) {
                    MeetingsUtil.updateNextMeetingTime(meeting.name, nextTime = null)
                    schedule(meeting, initialCalendar = null)
                } else {
                    schedule(meeting, calendar)
                }
            },
            onError = { schedule(meeting, initialCalendar = null) }
        )
    }

    fun reschedule(meetingName: String, initialCalendar: Calendar, meetingStringDate: String) {
        MeetingsUtil.provideMeetingByName(meetingName)?.let { meeting ->
            schedule(meeting, initialCalendar)
            MeetingsUtil.updateNextMeetingTime(meetingName, meetingStringDate)
        }
    }

    fun reschedule(meetingName: String) {
        MeetingsUtil.provideMeetingByName(meetingName)?.let { meetingObject ->
            schedule(meetingObject)
        }
    }

    private fun schedule(meetingObject: MeetingObject, initialCalendar: Calendar?) {
        val actualMeetingObject = MeetingsUtil.provideMeetingByName(meetingObject.name) ?: return

        val reportingTextChannel = bot.getTextChannelById(actualMeetingObject.reportingTextChannel) ?: return
        val verifiableVoiceChannel = bot.getVoiceChannelById(actualMeetingObject.verifiableVoiceChannel) ?: return
        val verifiableRole = bot.roles.firstOrNull { role -> role.id == actualMeetingObject.verifiableRoleId } ?: return

        val calendar = initialCalendar
            ?: getGregorianCalendar().apply {
                setupForNearestMeetingDay(actualMeetingObject.availableDays)
            }
        val universalTimerTask = UniversalTimerTask(
            reportingTextChannel, verifiableVoiceChannel, verifiableRole, actualMeetingObject
        )

        timer.schedule(universalTimerTask, calendar.time)
        Logger.logMeetingScheduled(calendar, actualMeetingObject.name)
        TaskManager.putTask(actualMeetingObject.name, universalTimerTask)
    }
}