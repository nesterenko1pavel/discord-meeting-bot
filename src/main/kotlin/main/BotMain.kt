package main

import config.FilesConfig
import core.BotRuntime
import extension.getFirstTextChannelByName
import extension.getFirstVoiceChannelByName
import extension.getProperty
import jda.JDADefaultBuilder
import latecomer.meeting.daily.DailyLatecomerTimerTaskScheduler
import latecomer.meeting.pbr.PbrLatecomerTimerTaskScheduler
import latecomer.meeting.retro.RetroLatecomerTimerTaskScheduler
import logging.Logger
import java.io.File
import java.util.Timer

fun main() {
    val projectPropertiesFile = File(FilesConfig.PROJECT_PROPERTIES_FILE)

    val bot = JDADefaultBuilder
        .build(projectPropertiesFile.getProperty("token"))
        .awaitReady()

    Logger.logBotStartup()
    BotRuntime.registerFinishListener(Logger::logBotFinish)

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

    RetroLatecomerTimerTaskScheduler.schedule(
        timer, bot.selfUser.id, verifiableVoiceChannel, reportingTextChannel
    )
}