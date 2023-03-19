package latecomer

import extension.getSimpleClassName
import latecomer.meeting.MeetingsConfig
import java.util.concurrent.ConcurrentHashMap

object TaskManager {

    private val tasksMap = ConcurrentHashMap<String, BaseTimerTask>()

    fun putTask(meetingConfig: MeetingsConfig, task: BaseTimerTask) {
        tasksMap[meetingConfig.getSimpleClassName()] = task
    }

    fun cancel(taskName: String) {
        tasksMap[taskName]?.cancel()
    }
}