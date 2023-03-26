package latecomer

import com.squareup.moshi.Moshi
import config.FilesConfig
import latecomer.model.MeetingObject
import latecomer.model.MeetingsObject
import java.io.File

object MeetingsConfigProvider {

    fun provideMeetings(): List<MeetingObject> {
        val meetingsData = File(FilesConfig.MEETINGS_CONFIG).readText()
        val moshi = Moshi.Builder().build()
        return moshi.adapter(MeetingsObject::class.java).fromJson(meetingsData)?.meetings ?: emptyList()
    }
}