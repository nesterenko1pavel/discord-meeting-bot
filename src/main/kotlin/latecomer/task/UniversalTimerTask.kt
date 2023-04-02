package latecomer.task

import latecomer.LatecomerUtil
import latecomer.MeetingsUtil
import latecomer.model.MeetingObject
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel

class UniversalTimerTask(
    private val reportingTextChannel: TextChannel,
    private val verifiableVoiceChannel: VoiceChannel,
    private val verifiableRole: Role,
    private val meetingObject: MeetingObject
) : BaseTimerTask() {

    override fun scheduleNext() {
        MeetingsUtil.deleteWarnedMembersIds(meetingObject.name)
        MeetingsUtil.updateAbsenceObject()
        TaskScheduler.schedule(meetingObject)
    }

    override fun onRunTask() {
        val membersForVerification = reportingTextChannel.members.filter { member ->
            member.roles.contains(verifiableRole)
        }

        LatecomerUtil.verifyLatecomers(
            membersInVerifiableChannel = verifiableVoiceChannel.members,
            membersForVerification = membersForVerification,
            warnedMembersIds = meetingObject.warnedMembersIds
        ) { reportMessage ->
            reportingTextChannel.sendMessage(reportMessage)
                .queue()
        }
    }
}