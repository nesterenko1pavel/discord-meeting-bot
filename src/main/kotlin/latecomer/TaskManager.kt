package latecomer

import latecomer.meeting.MeetingsConfig
import java.util.concurrent.ConcurrentHashMap

object TaskManager {

    private val tasksMap = ConcurrentHashMap<String, BaseTimerTask>()

    fun putTask(meetingConfig: MeetingsConfig, task: BaseTimerTask) {
        tasksMap[meetingConfig.javaClass.name] = task
    }

    fun cancel(taskName: String) {
        tasksMap[taskName]?.cancel()
    }
}