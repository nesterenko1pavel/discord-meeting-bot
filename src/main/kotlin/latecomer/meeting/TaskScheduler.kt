package latecomer.meeting

import extension.parseStringDate
import latecomer.MeetingsUtil
import latecomer.TaskManager
import latecomer.meeting.universal.UniversalTimerTaskScheduler
import latecomer.model.MeetingObject
import net.dv8tion.jda.api.JDA
import java.util.Calendar
import java.util.Timer

object TaskScheduler {

    private lateinit var bot: JDA

    private val timer = Timer()

    fun init(bot: JDA) {
        this.bot = bot
    }

    fun scheduleAll(meetings: List<MeetingObject>) {
        meetings.forEach { schedule(it) }
    }

    private fun schedule(meeting: MeetingObject) {
        parseStringDate(
            stringTime = meeting.nearestMeetingTime.orEmpty(),
            onSuccess = { calendar -> schedule(meeting, calendar) },
            onError = { schedule(meeting, null) }
        )
    }

    private fun schedule(meeting: MeetingObject, calendar: Calendar?) {
        val verifiableVoiceChannel = bot.getVoiceChannelById(meeting.verifiableVoiceChannel) ?: return
        val reportingTextChannel = bot.getTextChannelById(meeting.reportingTextChannel) ?: return

        UniversalTimerTaskScheduler.schedule(
            timer,
            bot.selfUser.id,
            verifiableVoiceChannel,
            reportingTextChannel,
            meeting.availableDays,
            meeting.name,
            calendar
        )
    }

    fun reschedule(meetingName: String, initialCalendar: Calendar, meetingStringDate: String) {
        MeetingsUtil.provideMeetings()
            .find { it.name == meetingName }
            ?.let { meeting ->
                TaskManager.cancel(meetingName)
                schedule(meeting, initialCalendar)
                MeetingsUtil.updateRescheduledMeeting(meetingName, meetingStringDate)
            }
    }
}