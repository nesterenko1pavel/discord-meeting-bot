package main

import config.FilesConfig
import core.BotRuntime
import extension.getProperty
import jda.JDADefaultBuilder
import latecomer.meeting.TaskScheduler
import logging.Logger
import java.io.File

fun main() {
    val projectPropertiesFile = File(FilesConfig.PROJECT_PROPERTIES_FILE)

    val bot = JDADefaultBuilder
        .build(projectPropertiesFile.getProperty("token"))
        .awaitReady()

    Logger.logBotStartup()
    BotRuntime.registerFinishListener(Logger::logBotFinish)

    TaskScheduler.init(bot)

    TaskScheduler.scheduleDaily()
    TaskScheduler.schedulePbr()
    TaskScheduler.scheduleRetro()
    TaskScheduler.schedulePlanning()
}