package latecomer.task

import java.util.concurrent.ConcurrentHashMap

object TaskManager {

    private val tasksMap = ConcurrentHashMap<String, BaseTimerTask>()

    fun putTask(meetingName: String, task: BaseTimerTask) {
        remove(meetingName)
        tasksMap[meetingName] = task
    }

    private fun remove(taskName: String) {
        tasksMap[taskName]?.cancel()
        tasksMap.remove(taskName)
    }
}