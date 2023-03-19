package extension

import latecomer.meeting.MeetingsConfig
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import java.io.File
import java.util.Properties

fun doubleDuplicatedListOf(vararg elements: Int): List<Int> {
    return if (elements.isNotEmpty()) {
        (elements + elements).asList()
    } else {
        emptyList()
    }
}

fun JDA.getFirstTextChannelByName(name: String): TextChannel {
    return getTextChannelsByName(name, true).first()
}

fun JDA.getFirstVoiceChannelByName(name: String): VoiceChannel {
    return getVoiceChannelsByName(name, true).first()
}

fun List<Member>.saveMembersIdToFile(fileName: String) {
    val file = File(fileName)
    forEach { member: Member ->
        file.appendText(member.id)
        file.appendText("\n")
    }
}

fun <T> List<T>.second(): T = this[1]

fun <T : Any> File.getProperty(propertyName: String): T {
    val prop = Properties()
    prop.load(inputStream())
    @Suppress("UNCHECKED_CAST")
    return prop[propertyName] as T
}

fun MeetingsConfig.getSimpleClassName(): String = this.javaClass.simpleName