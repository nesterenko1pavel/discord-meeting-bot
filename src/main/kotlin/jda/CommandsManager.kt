package jda

import core.BotConfigs
import extension.CalendarPattern
import extension.createSimpleDateFormat
import extension.getGregorianCalendar
import extension.getSimpleClassName
import extension.parseStringDate
import jda.CommandsConfigs.MAIN_COMMAND
import jda.CommandsConfigs.MAIN_COMMAND_DESCRIPTION
import jda.CommandsConfigs.MAIN_SUBCOMMAND_INFO
import jda.CommandsConfigs.MAIN_SUBCOMMAND_INFO_DESCRIPTION
import jda.CommandsConfigs.MAIN_SUBCOMMAND_RESCHEDULING
import jda.CommandsConfigs.MAIN_SUBCOMMAND_RESCHEDULING_DESCRIPTION
import jda.CommandsConfigs.MAIN_SUBCOMMAND_STATS
import jda.CommandsConfigs.MAIN_SUBCOMMAND_STATS_DESCRIPTION
import jda.CommandsConfigs.OPTION_MEETING_DATE
import jda.CommandsConfigs.OPTION_MEETING_DATE_DESCRIPTION
import jda.CommandsConfigs.OPTION_MEETING_NAME
import jda.CommandsConfigs.OPTION_MEETING_NAME_DESCRIPTION
import latecomer.meeting.LatecomerUtil
import latecomer.meeting.MeetingsConfig
import latecomer.meeting.TaskScheduler
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

private object CommandsConfigs {

    const val MAIN_COMMAND = "pavel"
    const val MAIN_COMMAND_DESCRIPTION = "Bot Pavel"

    const val MAIN_SUBCOMMAND_INFO = "hey"
    const val MAIN_SUBCOMMAND_INFO_DESCRIPTION = "Get welcome by bot"

    const val MAIN_SUBCOMMAND_STATS = "stats"
    const val MAIN_SUBCOMMAND_STATS_DESCRIPTION = "Latecomer's stats"

    const val MAIN_SUBCOMMAND_RESCHEDULING = "reschedule"
    const val MAIN_SUBCOMMAND_RESCHEDULING_DESCRIPTION = "Rescheduling nearest meeting"
    const val OPTION_MEETING_NAME = "name"
    const val OPTION_MEETING_NAME_DESCRIPTION = "Meeting name"
    const val OPTION_MEETING_DATE = "date"
    const val OPTION_MEETING_DATE_DESCRIPTION = "Rescheduled meeting date with pattern \'dd-MM-yyyy HH:mm\'"
}

class CommandsManager : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == MAIN_COMMAND) {
            when (event.subcommandName) {
                MAIN_SUBCOMMAND_INFO -> onInfoCommand(event)
                MAIN_SUBCOMMAND_STATS -> onStatsCommand(event)
                MAIN_SUBCOMMAND_RESCHEDULING -> onRescheduleCommand(event)
            }
        }
    }

    private fun onInfoCommand(event: SlashCommandInteractionEvent) {
        event.reply("Hello meeeen. I'm Bot Pavel version ${BotConfigs.VERSION}")
            .queue()
    }

    private fun onStatsCommand(event: SlashCommandInteractionEvent) {
        val filteredMembers = event.guild?.members?.filter { member ->
            member.roles.any { role ->
                event.member?.roles?.contains(role) ?: false
            }
        }
        event.reply(LatecomerUtil.createLatecomersStats(filteredMembers))
            .queue()
    }

    private fun onRescheduleCommand(event: SlashCommandInteractionEvent) {
        val meetingOption = event.getOption(OPTION_MEETING_NAME)?.asString
        val dateOption = event.getOption(OPTION_MEETING_DATE)?.asString

        if (meetingOption != null && dateOption != null) {
            processRescheduleCommand(event, meetingOption, dateOption)
        } else {
            event.reply("Internal error")
                .queue()
        }
    }

    private fun processRescheduleCommand(
        event: SlashCommandInteractionEvent,
        meetingOption: String,
        dateOption: String
    ) {
        parseStringDate(
            stringTime = dateOption,
            onSuccess = { calendar ->
                val nowCalendar = getGregorianCalendar()
                val isTimeOverdue = nowCalendar > calendar

                if (isTimeOverdue.not()) {
                    TaskScheduler.rescheduleMeeting(meetingOption, calendar)
                    val format = createSimpleDateFormat(CalendarPattern.FULL)
                    val formattedTime = format.format(calendar.time)
                    event.reply("$meetingOption rescheduled for $formattedTime")
                        .queue()
                } else {
                    event.reply("You enter a date in the past")
                        .queue()
                }
            },
            onError = {
                event.reply("Wrong date")
                    .queue()
            }
        )
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        registerCommands(event)
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        registerCommands(event)
    }

    private fun registerCommands(event: GenericGuildEvent) {
        val commandData = listOf(
            Commands.slash(MAIN_COMMAND, MAIN_COMMAND_DESCRIPTION)
                .addSubcommands(SubcommandData(MAIN_SUBCOMMAND_INFO, MAIN_SUBCOMMAND_INFO_DESCRIPTION))
                .addSubcommands(SubcommandData(MAIN_SUBCOMMAND_STATS, MAIN_SUBCOMMAND_STATS_DESCRIPTION))
                .addSubcommands(
                    SubcommandData(MAIN_SUBCOMMAND_RESCHEDULING, MAIN_SUBCOMMAND_RESCHEDULING_DESCRIPTION)
                        .addOptions(
                            OptionData(OptionType.STRING, OPTION_MEETING_NAME, OPTION_MEETING_NAME_DESCRIPTION, true)
                                .addChoice(MeetingsConfig.Daily.getSimpleClassName())
                                .addChoice(MeetingsConfig.Pbr.getSimpleClassName())
                                .addChoice(MeetingsConfig.Retro.getSimpleClassName())
                                .addChoice(MeetingsConfig.Planning.getSimpleClassName()),
                            OptionData(OptionType.STRING, OPTION_MEETING_DATE, OPTION_MEETING_DATE_DESCRIPTION, true)
                        )
                )
        )
        event.guild.updateCommands().addCommands(commandData).queue()
    }
}

private fun OptionData.addChoice(nameValue: String): OptionData {
    addChoice(nameValue, nameValue)
    return this
}