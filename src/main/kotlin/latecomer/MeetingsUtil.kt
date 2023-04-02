package latecomer

import com.google.gson.GsonBuilder
import config.FilesConfig
import extension.CalendarPattern
import extension.getGregorianCalendar
import extension.parseStringDate
import latecomer.model.AbsenceMemberObject
import latecomer.model.AbsenceObject
import latecomer.model.AvailableDays
import latecomer.model.MeetingObject
import latecomer.model.MeetingsConfigObject
import java.io.File

object MeetingsUtil {

    private val meetingConfigFile = File(FilesConfig.MEETINGS_CONFIG)

    private val adapter = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapterFactory(SealedTypeAdapterFactory.of(AvailableDays::class))
        .create()
        .getAdapter(MeetingsConfigObject::class.java)

    fun provideMeetingsObject(): MeetingsConfigObject? {
        val meetingsData = meetingConfigFile.readText()
        return adapter.fromJson(meetingsData)
    }

    fun provideMeetingByName(meetingName: String): MeetingObject? {
        return provideMeetings().find { it.name == meetingName }
    }

    fun provideMeetings(): List<MeetingObject> {
        val meetingsData = meetingConfigFile.readText()
        return adapter.fromJson(meetingsData)?.meetings ?: emptyList()
    }

    fun updateNextMeetingTime(meetingName: String, nextTime: String? = null) {
        val (meetings, changingMeeting) = excludeMeeting(meetingName)

        changingMeeting?.let { meeting ->
            val newMeeting = meeting.copy(nearestMeetingTime = nextTime)
            meetings.add(newMeeting)

            val json = adapter.toJson(MeetingsConfigObject(meetings))
            meetingConfigFile.writeText(json)
        }
    }

    fun updateWarnedMembersList(meetingName: String, warnedMemberId: String) {
        val (meetings, changingMeeting) = excludeMeeting(meetingName)

        changingMeeting?.let { meeting ->

            val newWarnedMembersIds = mutableListOf<String>()
            newWarnedMembersIds.addAll(meeting.warnedMembersIds)

            if (!newWarnedMembersIds.contains(warnedMemberId)) {
                newWarnedMembersIds.add(warnedMemberId)

                val newMeeting = meeting.copy(warnedMembersIds = newWarnedMembersIds)
                meetings.add(newMeeting)
                writeMeetingsObjectToFile(meetings)
            }
        }
    }

    fun deleteWarnedMembersIds(meetingName: String) {
        val (meetings, changingMeeting) = excludeMeeting(meetingName)

        changingMeeting?.let { meeting ->
            val newMeeting = meeting.copy(warnedMembersIds = listOf())
            meetings.add(newMeeting)

            writeMeetingsObjectToFile(meetings)
        }
    }

    private fun excludeMeeting(meetingName: String): Pair<MutableList<MeetingObject>, MeetingObject?> {
        val currentMeetings = provideMeetings()

        val meetings = mutableListOf<MeetingObject>()

        val filteredMeetings = currentMeetings.filter { it.name != meetingName }
        meetings.addAll(filteredMeetings)

        return meetings to currentMeetings.find { it.name == meetingName }
    }

    private fun writeMeetingsObjectToFile(meetings: List<MeetingObject>) {
        val json = adapter.toJson(MeetingsConfigObject(meetings, provideMeetingsObject()?.absence))
        meetingConfigFile.writeText(json)
    }

    fun updateAbsenceMember(memberId: String, dateStartOption: String, dateEndOption: String?) {
        val meetingsObject = provideMeetingsObject() ?: return

        val newAbsenceMemberObjectList = mutableListOf<AbsenceMemberObject>()
        meetingsObject.absence?.let { newAbsenceMemberObjectList.addAll(it.absenceMembersList) }

        newAbsenceMemberObjectList.add(AbsenceMemberObject(memberId, dateStartOption, dateEndOption))

        val json = adapter.toJson(MeetingsConfigObject(meetingsObject.meetings, AbsenceObject(newAbsenceMemberObjectList)))
        meetingConfigFile.writeText(json)
    }

    fun updateAbsenceObject() {
        val currentCalendar = getGregorianCalendar()

        val absenceObject = provideMeetingsObject()?.absence ?: return

        val newAbsenceMemberObjectList = mutableListOf<AbsenceMemberObject>()

        absenceObject.absenceMembersList.forEach { absenceMember ->
            val dateEnd = absenceMember.dataEnd?.let { parseStringDate(it, CalendarPattern.SHORT) }
            val dateStart = parseStringDate(absenceMember.dataStart)
            if (dateEnd != null && currentCalendar < dateEnd) {
                newAbsenceMemberObjectList.add(absenceMember)
            } else if (dateEnd == null && currentCalendar < dateStart) {
                newAbsenceMemberObjectList.add(absenceMember)
            }
        }

        val json = adapter.toJson(MeetingsConfigObject(provideMeetings(), AbsenceObject(newAbsenceMemberObjectList)))
        meetingConfigFile.writeText(json)
    }
}