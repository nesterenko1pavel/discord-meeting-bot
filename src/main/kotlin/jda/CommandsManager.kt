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
    LATECOME("latecome", "Notify bot when you're late")
}

private enum class CommandOption(
    val optionName: String,
    val optionDescription: String
) {
    MEETING_NAME("name", "Meeting name"),
    MEETING_DATE("date", "Rescheduled meeting date with pattern \'dd-MM-yyyy HH:mm\'")
}

class CommandsManager : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == Command.MAIN.commandName) {
            when (event.subcommandName) {
                SubCommand.HEY.commandName -> onInfoCommand(event)
                SubCommand.STATS.commandName -> onStatsCommand(event)
                SubCommand.RESCHEDULE.commandName -> onRescheduleCommand(event)
                SubCommand.LATECOME.commandName -> onLatecomeCommand(event)
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
        val meetingOption = event.getOption(CommandOption.MEETING_NAME.optionName)?.asString
        val dateOption = event.getOption(CommandOption.MEETING_DATE.optionName)?.asString

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
                    TaskScheduler.reschedule(meetingOption, calendar, dateOption)
                    val format = createSimpleDateFormat(CalendarPattern.COMMON)
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

    private fun onLatecomeCommand(event: SlashCommandInteractionEvent) {
        // todo: добавить в MeetingObject объект с датой ближайшей встречи и
        // todo: списком membersId, кто опоздает, чтобы их не тегало.
        // todo: список зануляется только при переносе встречи или установке новой даты встречи.
        // todo: по команде расширять список membersId, которые опоздают
    }

    override fun onGuildReady(event: GuildReadyEvent) {
        registerCommands(event)
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        registerCommands(event)
    }

    private fun registerCommands(event: GenericGuildEvent) {
        val meetingNamesOptionData = makeOptionData(
            type = OptionType.STRING,
            commandOption = CommandOption.MEETING_NAME,
            choices = MeetingsUtil.provideMeetings().map { it.name }
        )

        val commandData = listOf(
            Commands.slash(Command.MAIN.commandName, Command.MAIN.commandDescription)
                .addSubcommands(subcommand = SubCommand.HEY)
                .addSubcommands(subcommand = SubCommand.STATS)
                .addSubcommands(
                    subcommand = SubCommand.RESCHEDULE,
                    options = listOf(
                        meetingNamesOptionData,
                        makeOptionData(
                            type = OptionType.STRING,
                            commandOption = CommandOption.MEETING_DATE
                        )
                    )
                )
                .addSubcommands(
                    subcommand = SubCommand.LATECOME,
                    options = listOf(meetingNamesOptionData)
                )
        )
        event.guild.updateCommands().addCommands(commandData).queue()
    }
}

private fun SlashCommandData.addSubcommands(
    subcommand: SubCommand,
    options: List<OptionData> = emptyList()
): SlashCommandData {
    return addSubcommands(SubcommandData(subcommand.commandName, subcommand.commandDescription).addOptions(options))
}

private fun makeOptionData(
    type: OptionType,
    commandOption: CommandOption,
    choices: List<String> = emptyList(),
    isRequired: Boolean = true
): OptionData {
    val optionData = OptionData(type, commandOption.optionName, commandOption.optionDescription, isRequired)
    choices.forEach { value -> optionData.addChoice(value, value) }
    return optionData
}