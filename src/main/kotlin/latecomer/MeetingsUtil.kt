package latecomer

import com.google.gson.GsonBuilder
import config.FilesConfig
import core.SealedTypeAdapterFactory
import extension.CalendarPattern
import extension.getGregorianCalendar
import extension.parseStringDate
import latecomer.model.AbsenceMemberObject
import latecomer.model.AbsenceObject
import latecomer.model.AvailableDays
import latecomer.model.GuildObject
import latecomer.model.MeetingObject
import latecomer.model.MeetingsConfigObject
import java.io.File

object MeetingsUtil {

    private val meetingConfigFile = File(FilesConfig.MEETINGS_CONFIG)
    private val lock = Any()

    private val adapter = GsonBuilder()
        .registerTypeAdapterFactory(SealedTypeAdapterFactory.of(AvailableDays::class))
        .setPrettyPrinting()
        .create()

    fun provideGuildObjectByGuildId(guildId: String): GuildObject? {
        val meetingsData = meetingConfigFile.synchronizedReadText(lock)
        return adapter.fromJson(meetingsData, MeetingsConfigObject::class.java).guilds[guildId]
    }

    fun provideMeetingByName(guildId: String, meetingName: String): MeetingObject? {
        return provideMeetingsByGuildId(guildId).find { it.name == meetingName }
    }

    fun provideMeetingsByGuildId(guildId: String): List<MeetingObject> {
        val meetingsData = meetingConfigFile.synchronizedReadText(lock)
        return adapter.fromJson(meetingsData, MeetingsConfigObject::class.java).guilds[guildId]?.meetings ?: emptyList()
    }

    fun provideGuilds(): Map<String, GuildObject> {
        val meetingsData = meetingConfigFile.synchronizedReadText(lock)
        return adapter.fromJson(meetingsData, MeetingsConfigObject::class.java).guilds
    }

    fun updateNextMeetingTime(guildId: String, meetingName: String, nextTime: String? = null) {
        val (meetings, changingMeeting) = excludeMeeting(guildId, meetingName)

        changingMeeting?.let { meeting ->
            val newMeeting = meeting.copy(nearestDelayedMeetingTime = nextTime)
            meetings.add(newMeeting)

            val guilds = provideGuilds()
            val currentGuild = guilds[guildId]

            val mutableGuilds = mutableMapOf<String, GuildObject>()
            mutableGuilds.putAll(guilds)
            mutableGuilds[guildId] = GuildObject(meetings, currentGuild?.absence)

            val json = adapter.toJson(MeetingsConfigObject(mutableGuilds))
            meetingConfigFile.synchronizedWriteText(lock, json)
        }
    }

    fun updateWarnedMembersList(guildId: String, meetingName: String, warnedMemberId: String) {
        val (meetings, changingMeeting) = excludeMeeting(guildId, meetingName)

        changingMeeting?.let { meeting ->

            val newWarnedMembersIds = mutableListOf<String>()
            newWarnedMembersIds.addAll(meeting.warnedMembersIds)

            if (!newWarnedMembersIds.contains(warnedMemberId)) {
                newWarnedMembersIds.add(warnedMemberId)

                val newMeeting = meeting.copy(warnedMembersIds = newWarnedMembersIds)
                meetings.add(newMeeting)

                val guilds = provideGuilds()
                val currentGuild = guilds[guildId]

                val mutableGuilds = mutableMapOf<String, GuildObject>()
                mutableGuilds.putAll(guilds)
                mutableGuilds[guildId] = GuildObject(meetings, currentGuild?.absence)

                val json = adapter.toJson(MeetingsConfigObject(mutableGuilds))
                meetingConfigFile.synchronizedWriteText(lock, json)
            }
        }
    }

    fun deleteWarnedMembersIds(guildId: String, meetingName: String) {
        val (meetings, changingMeeting) = excludeMeeting(guildId, meetingName)

        changingMeeting?.let { meeting ->
            val newMeeting = meeting.copy(warnedMembersIds = listOf())
            meetings.add(newMeeting)

            val guilds = provideGuilds()
            val currentGuild = guilds[guildId]

            val mutableGuilds = mutableMapOf<String, GuildObject>()
            mutableGuilds.putAll(guilds)
            mutableGuilds[guildId] = GuildObject(meetings, currentGuild?.absence)

            val json = adapter.toJson(MeetingsConfigObject(mutableGuilds))
            meetingConfigFile.synchronizedWriteText(lock, json)
        }
    }

    private fun excludeMeeting(guildId: String, meetingName: String): Pair<MutableList<MeetingObject>, MeetingObject?> {
        val currentMeetings = provideMeetingsByGuildId(guildId)

        val meetings = mutableListOf<MeetingObject>()

        val filteredMeetings = currentMeetings.filter { it.name != meetingName }
        meetings.addAll(filteredMeetings)

        return meetings to currentMeetings.find { it.name == meetingName }
    }

    fun updateAbsenceMember(guildId: String, memberId: String, dateStartOption: String, dateEndOption: String?) {
        val meetingsObject = provideGuildObjectByGuildId(guildId) ?: return

        val newAbsenceMemberObjectList = mutableListOf<AbsenceMemberObject>()
        meetingsObject.absence?.let { newAbsenceMemberObjectList.addAll(it.absenceMembersList) }

        newAbsenceMemberObjectList.add(AbsenceMemberObject(memberId, dateStartOption, dateEndOption))

        val guilds = provideGuilds()
        val currentGuild = guilds[guildId]

        val mutableGuilds = mutableMapOf<String, GuildObject>()
        mutableGuilds.putAll(guilds)
        mutableGuilds[guildId] = GuildObject(currentGuild?.meetings ?: emptyList(), AbsenceObject(newAbsenceMemberObjectList))

        val json = adapter.toJson(MeetingsConfigObject(mutableGuilds))
        meetingConfigFile.synchronizedWriteText(lock, json)
    }

    fun updateAbsenceObject(guildId: String) {
        val currentCalendar = getGregorianCalendar()

        val absenceObject = provideGuildObjectByGuildId(guildId)?.absence ?: return

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

        val guilds = provideGuilds()
        val currentGuild = guilds[guildId]

        val mutableGuilds = mutableMapOf<String, GuildObject>()
        mutableGuilds.putAll(guilds)
        mutableGuilds[guildId] = GuildObject(currentGuild?.meetings ?: emptyList(), AbsenceObject(newAbsenceMemberObjectList))

        val json = adapter.toJson(MeetingsConfigObject(mutableGuilds))
        meetingConfigFile.synchronizedWriteText(lock, json)
    }
}

private fun File.synchronizedReadText(lock: Any): String {
    return synchronized(lock) {
        readText()
    }
}

private fun File.synchronizedWriteText(lock: Any, text: String) {
    synchronized(lock) {
        writeText(text)
    }
}