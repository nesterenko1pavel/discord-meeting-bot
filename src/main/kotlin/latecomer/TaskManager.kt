package latecomer

import java.util.concurrent.ConcurrentHashMap

object TaskManager {

    private val tasksMap = ConcurrentHashMap<String, BaseTimerTask>()

    fun putTask(meetingName: String, task: BaseTimerTask) {
        tasksMap[meetingName] = task
    }

    fun cancel(taskName: String) {
        tasksMap[taskName]?.cancel()
    }
}