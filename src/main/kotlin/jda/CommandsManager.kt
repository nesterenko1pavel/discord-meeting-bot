package jda

import core.BotConfigs
import extension.CalendarPattern
import extension.createSimpleDateFormat
import extension.getGregorianCalendar
import extension.parseStringDate
import latecomer.LatecomerUtil
import latecomer.MeetingsUtil
import latecomer.task.TaskScheduler
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

private enum class Command(
    val commandName: String,
    val commandDescription: String,
) {
    MAIN("pavel", "Bot Pavel");
}

private enum class SubCommand(
    val commandName: String,
    val commandDescription: String
) {
    HEY("hey", "Get welcome by bot"),
    STATS("stats", "Latecomer's stats"),
    RESCHEDULE("reschedule", "Rescheduling nearest meeting"),
    LATECOME("latecome", "Notify bot when you're late"),
    ABSENCE("absence", "Warn the bot about the absence")
}

private enum class CommandOption(
    val optionName: String,
    val optionDescription: String
) {
    MEETING_NAME("name", "Meeting name"),
    MEETING_DATE("date", "Rescheduled meeting date with pattern \'dd-MM-yyyy HH:mm\'"),
    ABSENCE_START_DATE("date-start", "Enter first or only date of absence with patter \'dd-MM-yyyy\'"),
    ABSENCE_END_DATE("date-end", "Enter last date of absence with patter \'dd-MM-yyyy\'"),
    MEMBER("member", "Enter the member for which the command applies")

}

class CommandsManager : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == Command.MAIN.commandName) {
            when (event.subcommandName) {
                SubCommand.HEY.commandName -> onInfoCommand(event)
                SubCommand.STATS.commandName -> onStatsCommand(event)
                SubCommand.RESCHEDULE.commandName -> onRescheduleCommand(event)
                SubCommand.LATECOME.commandName -> onLatecomeCommand(event)
                SubCommand.ABSENCE.commandName -> onAbsenceCommand(event)
            }
        }
    }

    private fun onInfoCommand(event: SlashCommandInteractionEvent) {
        event.fastReplay("Hello meeeen. I'm Bot Pavel version ${BotConfigs.VERSION}")
    }

    private fun onStatsCommand(event: SlashCommandInteractionEvent) {
        val filteredMembers = event.guild?.members?.filter { member ->
            member.roles.any { role ->
                event.member?.roles?.contains(role) ?: false
            }
        }
        event.fastReplay(LatecomerUtil.createLatecomersStats(filteredMembers))
    }

    private fun onRescheduleCommand(event: SlashCommandInteractionEvent) {
        val meetingOption = event.getOption(CommandOption.MEETING_NAME.optionName)?.asString
        val dateOption = event.getOption(CommandOption.MEETING_DATE.optionName)?.asString

        if (meetingOption != null && dateOption != null) {
            processRescheduleCommand(event, meetingOption, dateOption)
        } else {
            event.fastReplay("Internal error")
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
                    TaskScheduler.reschedule(meetingOption, calendar, dateOption)
                    val format = createSimpleDateFormat(CalendarPattern.COMMON)
                    val formattedTime = format.format(calendar.time)
                    event.fastReplay("$meetingOption rescheduled for $formattedTime")
                } else {
                    event.fastReplay("You enter a date in the past")
                }
            },
            onError = {
                event.fastReplay("Wrong date")
            }
        )
    }

    private fun onLatecomeCommand(event: SlashCommandInteractionEvent) {
        val meetingOption = event.getOption(CommandOption.MEETING_NAME.optionName)?.asString.orEmpty()
        val warnedMemberId = event.member?.id.orEmpty()
        MeetingsUtil.updateWarnedMembersList(meetingOption, warnedMemberId)
        TaskScheduler.reschedule(meetingOption)
        event.fastReplay("${event.member?.effectiveName} will be late for the $meetingOption")
    }

    private fun onAbsenceCommand(event: SlashCommandInteractionEvent) {
        val dateStartOption = event.getOption(CommandOption.ABSENCE_START_DATE.optionName)?.asString.orEmpty()
        val dateEndOption = event.getOption(CommandOption.ABSENCE_END_DATE.optionName)?.asString
        val memberOption = event.getOption(CommandOption.MEMBER.optionName)?.asMember ?: event.member

        MeetingsUtil.updateAbsenceMember(memberOption?.id.orEmpty(), dateStartOption, dateEndOption)
        TaskScheduler.scheduleAll(MeetingsUtil.provideMeetings())
        event.fastReplay("Absence: $dateStartOption${
            if (dateEndOption != null) {
                "-$dateEndOption"
            } else {
                ""
            }
        } for ${memberOption?.effectiveName}")
    }

    private fun registerCommands(event: GenericGuildEvent) {
        val meetingNamesOptionData = makeOptionData(
            commandOption = CommandOption.MEETING_NAME,
            choices = MeetingsUtil.provideMeetings().map { it.name }
        )
        val commandData = listOf(
            Commands.slash(Command.MAIN.commandName, Command.MAIN.commandDescription)
                .addSubcommands(SubCommand.HEY)
                .addSubcommands(SubCommand.STATS)
                .addSubcommands(
                    subcommand = SubCommand.RESCHEDULE,
                    options = listOf(
                        meetingNamesOptionData,
                        makeOptionData(CommandOption.MEETING_DATE)
                    )
                )
                .addSubcommands(
                    subcommand = SubCommand.LATECOME,
                    options = listOf(meetingNamesOptionData)
                )
                .addSubcommands(
                    subcommand = SubCommand.ABSENCE,
                    options = listOf(
                        makeOptionData(CommandOption.ABSENCE_START_DATE),
                        makeOptionData(CommandOption.ABSENCE_END_DATE, isRequired = false),
                        makeOptionData(CommandOption.MEMBER, type = OptionType.USER, isRequired = false)
                    )
                )
        )
        event.guild.updateCommands().addCommands(commandData).queue()
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        registerCommands(event)
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        registerCommands(event)
    }
}

private fun SlashCommandData.addSubcommands(
    subcommand: SubCommand,
    options: List<OptionData> = emptyList()
): SlashCommandData {
    return addSubcommands(SubcommandData(subcommand.commandName, subcommand.commandDescription).addOptions(options))
}

private fun makeOptionData(
    commandOption: CommandOption,
    type: OptionType = OptionType.STRING,
    choices: List<String> = emptyList(),
    isRequired: Boolean = true
): OptionData {
    val optionData = OptionData(type, commandOption.optionName, commandOption.optionDescription, isRequired)
    choices.forEach { value -> optionData.addChoice(value, value) }
    return optionData
}

private fun SlashCommandInteractionEvent.fastReplay(message: String) {
    reply(message).queue()
}