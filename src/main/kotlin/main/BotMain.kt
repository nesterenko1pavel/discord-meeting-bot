package main

import config.FilesConfig
import core.BotRuntime
import extension.getProperty
import jda.JDADefaultBuilder
import latecomer.MeetingsUtil
import latecomer.task.TaskScheduler
import logging.Logger
import org.brunocvcunha.jiphy.Jiphy
import java.io.File


fun main() {
    val projectPropertiesFile = File(FilesConfig.PROJECT_PROPERTIES_FILE)

    val jiphy = Jiphy.builder()
        .apiKey(projectPropertiesFile.getProperty("jiphy_token"))
        .build()

    val bot = JDADefaultBuilder()
        .addJiphy(jiphy)
        .build(projectPropertiesFile.getProperty("jda_token"))
        .awaitReady()

    Logger.logBotStartup()
    BotRuntime.registerFinishListener(Logger::logBotFinish)

    TaskScheduler.init(bot)

    TaskScheduler.scheduleAllGuildsMeetings(MeetingsUtil.provideGuilds())
}