package latecomer

import com.squareup.moshi.Moshi
import config.FilesConfig
import latecomer.model.MeetingObject
import latecomer.model.MeetingsObject
import java.io.File

private const val INDENT_SPACES = "    "

object MeetingsUtil {

    private val meetingConfigFile = File(FilesConfig.MEETINGS_CONFIG)

    private val adapter = Moshi.Builder()
        .build()
        .adapter(MeetingsObject::class.java)
        .indent(INDENT_SPACES)

    fun provideMeetings(): List<MeetingObject> {
        val meetingsData = meetingConfigFile.readText()
        return adapter.fromJson(meetingsData)?.meetings ?: emptyList()
    }


    fun updateNextMeetingTime(meetingName: String, nextTime: String? = null) {
        val currentMeetings = provideMeetings()

        val meetings = mutableListOf<MeetingObject>()

        val filteredMeetings = currentMeetings.filter { it.name != meetingName }
        meetings.addAll(filteredMeetings)

        val changingMeeting = currentMeetings.find { it.name == meetingName }

        changingMeeting?.let { meeting ->
            val newMeeting = meeting.copy(nearestMeetingTime = nextTime)
            meetings.add(newMeeting)

            val json = adapter.toJson(MeetingsObject(meetings))

            meetingConfigFile.writeText(json)
        }
    }
}