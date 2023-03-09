package logging

import core.BotConfigs
import extension.createSimpleDateFormat
import extension.getGregorianCalendar
import java.io.File
import java.util.Calendar

private const val LOGS_FILE = "logs.txt"

object Logger {

    private val logsFile = File(LOGS_FILE)
    private val formatter = createSimpleDateFormat("dd-MM-yyyy HH:mm:ss")

    fun logBotStartup() {
        log("Startup ${BotConfigs.VERSION}")
    }

    fun logBotFinish() {
        log("Finishing ${BotConfigs.VERSION}")
    }

    fun logDailyScheduled(meetingCalendar: Calendar) {
        logMeetingScheduled(meetingCalendar, "daily")
    }

    fun logPbrScheduled(meetingCalendar: Calendar) {
        logMeetingScheduled(meetingCalendar, "pbr")
    }

    private fun logMeetingScheduled(meetingCalendar: Calendar, meetingName: String) {
        val meetingTime = formatter.format(meetingCalendar.time)
        val message = "Next $meetingName meeting scheduled for $meetingTime"
        log(message)
    }

    private fun log(message: String) {
        val currentTime = formatter.format(getGregorianCalendar().time)
        logsFile.appendText("[$currentTime] $message\n")
    }
}