package latecomer.task

import java.util.TimerTask

abstract class BaseTimerTask : TimerTask() {

    override fun run() {
        onRunTask()
        scheduleNext()
    }

    protected abstract fun scheduleNext()

    protected abstract fun onRunTask()
}