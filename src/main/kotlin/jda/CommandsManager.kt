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
import org.brunocvcunha.jiphy.Jiphy
import org.brunocvcunha.jiphy.requests.JiphySearchRequest

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
    RESCHEDULE_NEAREST("reschedule-nearest", "Rescheduling nearest meeting"),
    LATECOME("latecome", "Notify bot when you're late"),
    ABSENCE("absence", "Warn the bot about the absence"),
    GIF("gif", "Ask bot to send GIF")
}

private enum class CommandOption(
    val optionName: String,
    val optionDescription: String
) {
    MEETING_NAME("name", "Meeting name"),
    MEETING_DATE("date", "Rescheduled meeting date with pattern \'dd-MM-yyyy HH:mm\'"),
    ABSENCE_START_DATE("date-start", "Enter first or only date of absence with patter \'dd-MM-yyyy\'"),
    ABSENCE_END_DATE("date-end", "Enter last date of absence with patter \'dd-MM-yyyy\'"),
    MEMBER("member", "Enter the member for which the command applies. By default applies to you"),
    GIF_NAME("gif-name", "Enter name of the gif you want to see")
}

class CommandsManager(
    private val jiphy: Jiphy
) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == Command.MAIN.commandName) {
            when (event.subcommandName) {
                SubCommand.HEY.commandName -> onInfoCommand(event)
                SubCommand.STATS.commandName -> onStatsCommand(event)
                SubCommand.RESCHEDULE_NEAREST.commandName -> onRescheduleCommand(event)
                SubCommand.LATECOME.commandName -> onLatecomeCommand(event)
                SubCommand.ABSENCE.commandName -> onAbsenceCommand(event)
                SubCommand.GIF.commandName -> onGifCommand(event)
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
        val parsedCalendar = parseStringDate(dateOption)
        if (parsedCalendar != null) {
            val nowCalendar = getGregorianCalendar()
            val isTimeOverdue = nowCalendar > parsedCalendar

            if (isTimeOverdue.not()) {
                TaskScheduler.reschedule(event.guild?.id.orEmpty(), meetingOption, parsedCalendar, dateOption)
                val format = createSimpleDateFormat(CalendarPattern.COMMON)
                val formattedTime = format.format(parsedCalendar.time)
                event.fastReplay("$meetingOption rescheduled for $formattedTime")
            } else {
                event.fastReplay("You enter a date in the past")
            }
        } else {
            event.fastReplay("Wrong date")
        }
    }

    private fun onLatecomeCommand(event: SlashCommandInteractionEvent) {
        val meetingOption = event.getOption(CommandOption.MEETING_NAME.optionName)?.asString.orEmpty()
        val warnedMemberId = event.member?.id.orEmpty()
        MeetingsUtil.updateWarnedMembersList(event.guild?.id.orEmpty(), meetingOption, warnedMemberId)
        TaskScheduler.reschedule(event.guild?.id.orEmpty(), meetingOption)
        event.fastReplay("${event.member?.effectiveName} will be late for the $meetingOption")
    }

    private fun onAbsenceCommand(event: SlashCommandInteractionEvent) {
        val dateStartOption = event.getOption(CommandOption.ABSENCE_START_DATE.optionName)?.asString.orEmpty()
        val dateEndOption = event.getOption(CommandOption.ABSENCE_END_DATE.optionName)?.asString
        val memberOption = event.getOption(CommandOption.MEMBER.optionName)?.asMember ?: event.member

        MeetingsUtil.updateAbsenceMember(event.guild?.id.orEmpty(), memberOption?.id.orEmpty(), dateStartOption, dateEndOption)
        TaskScheduler.scheduleAll(event.guild?.id.orEmpty(), MeetingsUtil.provideMeetingsByGuildId(event.guild?.id.orEmpty()))
        event.fastReplay("Absence: $dateStartOption${
            if (dateEndOption != null) {
                "-$dateEndOption"
            } else {
                ""
            }
        } for ${memberOption?.effectiveName}")
    }

    private fun onGifCommand(event: SlashCommandInteractionEvent) {
        val gifName = event.getOption(CommandOption.GIF_NAME.optionName)?.asString.orEmpty()
        val actualGifName = gifName.replace(" ", "")
        val entities = jiphy.sendRequest(JiphySearchRequest(actualGifName))
        event.fastReplay(entities.data.random().url)
    }

    private fun registerCommands(event: GenericGuildEvent) {
        val meetingNamesOptionData = makeOptionData(
            commandOption = CommandOption.MEETING_NAME,
            choices = MeetingsUtil.provideMeetingsByGuildId(event.guild.id).map { it.name }
        )
        val commandData = listOf(
            Commands.slash(Command.MAIN.commandName, Command.MAIN.commandDescription)
                .addSubcommands(SubCommand.HEY)
                .addSubcommands(SubCommand.STATS)
                .addSubcommands(
                    subcommand = SubCommand.RESCHEDULE_NEAREST,
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
                .addSubcommands(
                    subcommand = SubCommand.GIF,
                    options = listOf(makeOptionData(CommandOption.GIF_NAME))
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