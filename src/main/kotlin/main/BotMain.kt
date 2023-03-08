package main

import config.FilesConfig
import extension.getFirstTextChannelByName
import extension.getFirstVoiceChannelByName
import extension.getProperty
import jda.JDADefaultBuilder
import latecomer.meeting.daily.DailyLatecomerTimerTaskScheduler
import latecomer.meeting.pbr.PbrLatecomerTimerTaskScheduler
import java.io.File
import java.util.Timer

fun main() {
    val projectPropertiesFile = File(FilesConfig.PROJECT_PROPERTIES_FILE)

    val bot = JDADefaultBuilder
        .build(projectPropertiesFile.getProperty("token"))
        .awaitReady()

    val verifiableVoiceChannel = bot.getFirstVoiceChannelByName(
        projectPropertiesFile.getProperty("monitored_voice_channel")
    )
    val reportingTextChannel = bot.getFirstTextChannelByName(
        projectPropertiesFile.getProperty("messages_channel")
    )

    val timer = Timer()

    DailyLatecomerTimerTaskScheduler.schedule(
        timer, bot.selfUser.id, verifiableVoiceChannel, reportingTextChannel
    )

    PbrLatecomerTimerTaskScheduler.schedule(
        timer, bot.selfUser.id, verifiableVoiceChannel, reportingTextChannel
    )
}