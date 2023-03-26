package latecomer

import com.squareup.moshi.Moshi
import config.FilesConfig
import latecomer.model.MeetingObject
import latecomer.model.MeetingsObject
import java.io.File

object MeetingsUtil {

    fun provideMeetings(): List<MeetingObject> {
        val meetingsData = File(FilesConfig.MEETINGS_CONFIG).readText()
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(MeetingsObject::class.java)
        return adapter.fromJson(meetingsData)?.meetings ?: emptyList()
    }

    fun updateRescheduledMeeting(meetingName: String, nextTime: String) {
        val currentMeetings = provideMeetings()

        val meetings = mutableListOf<MeetingObject>()

        val filteredMeetings = currentMeetings.filter { it.name != meetingName }
        meetings.addAll(filteredMeetings)

        val changingMeeting = currentMeetings.find { it.name == meetingName }

        changingMeeting?.let { meeting ->
            val newMeeting = meeting.copy(nearestMeetingTime = nextTime)
            meetings.add(newMeeting)

            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(MeetingsObject::class.java)
                .indent("    ")
            val json = adapter.toJson(MeetingsObject(meetings))

            File(FilesConfig.MEETINGS_CONFIG).writeText(json)
        }
    }
}