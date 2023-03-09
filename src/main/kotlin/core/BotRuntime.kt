package core

object BotRuntime {

    private val runtime = Runtime.getRuntime()

    fun registerFinishListener(action: () -> Unit) {
        runtime.addShutdownHook(Thread(action))
    }
}