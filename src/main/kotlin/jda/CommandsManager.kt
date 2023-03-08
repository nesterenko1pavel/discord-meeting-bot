package jda

import config.FilesConfig
import jda.CommandsConfigs.MAIN_COMMAND
import jda.CommandsConfigs.MAIN_COMMAND_DESCRIPTION
import jda.CommandsConfigs.MAIN_SUBCOMMAND_INFO
import jda.CommandsConfigs.MAIN_SUBCOMMAND_INFO_DESCRIPTION
import jda.CommandsConfigs.MAIN_SUBCOMMAND_STATS
import jda.CommandsConfigs.MAIN_SUBCOMMAND_STATS_DESCRIPTION
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.GenericGuildEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.io.File

private object CommandsConfigs {

    const val MAIN_COMMAND = "pavel"
    const val MAIN_COMMAND_DESCRIPTION = "Bot Pavel"

    const val MAIN_SUBCOMMAND_INFO = "hey"
    const val MAIN_SUBCOMMAND_INFO_DESCRIPTION = "Get welcome by bot"

    const val MAIN_SUBCOMMAND_STATS = "stats"
    const val MAIN_SUBCOMMAND_STATS_DESCRIPTION = "Latecomer's stats"
}

class CommandsManager : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == MAIN_COMMAND) {
            if (event.subcommandName == MAIN_SUBCOMMAND_INFO) {
                onInfoCommand(event)
            } else if (event.subcommandName == MAIN_SUBCOMMAND_STATS) {
                onStatsCommand(event)
            }
        }
    }

    private fun onInfoCommand(event: SlashCommandInteractionEvent) {
        event.reply("Hello meeeen. I'm Bot Pavel!")
            .queue()
    }

    private fun onStatsCommand(event: SlashCommandInteractionEvent) {
        val filteredMembers = event.guild?.members?.filter { member ->
            member.roles.any { role ->
                event.member?.roles?.contains(role) ?: false
            }
        }
        event.reply(createLatecomersMessage(filteredMembers))
            .queue()
    }

    private fun createLatecomersMessage(members: List<Member>?): String {
        val file = File(FilesConfig.LATECOMERS_STATS_FILE)

        val latecomersMap = mutableMapOf<String, Int>()
        members?.forEach { member ->
            latecomersMap[member.id] = 0
        }

        file.readLines().forEach { line ->
            latecomersMap[line] = latecomersMap[line]?.plus(1) ?: -1
        }

        val message = StringBuilder()
        latecomersMap.forEach { (id, count) ->
            val member = members?.firstOrNull { member -> member.id == id }
            message.appendLine("${member?.user?.name} - $count")
        }
        return message.toString()
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
                .addSubcommands(SubcommandData(MAIN_SUBCOMMAND_STATS, MAIN_SUBCOMMAND_STATS_DESCRIPTION)),
        )
        event.guild.updateCommands().addCommands(commandData).queue()
    }
}