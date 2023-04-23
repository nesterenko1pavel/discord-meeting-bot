package jda

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.brunocvcunha.jiphy.Jiphy

class JDADefaultBuilder {

    private lateinit var jiphy: Jiphy

    fun addJiphy(jiphy: Jiphy): JDADefaultBuilder {
        this.jiphy = jiphy
        return this
    }

    fun build(token: String): JDA {
        return JDABuilder.createDefault(token)
            .setActivity(Activity.playing("IntelliJ IDEA"))

            .enableIntents(GatewayIntent.GUILD_MEMBERS)
            .setChunkingFilter(ChunkingFilter.ALL)
            .setMemberCachePolicy(MemberCachePolicy.ALL)

            .addEventListeners(CommandsManager(jiphy))

            .build()
    }
}