package extension

import net.dv8tion.jda.api.entities.Member
import java.io.File
import java.util.Properties

fun doubleDuplicatedListOf(vararg elements: Int): List<Int> {
    return if (elements.isNotEmpty()) {
        (elements + elements).asList()
    } else {
        emptyList()
    }
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