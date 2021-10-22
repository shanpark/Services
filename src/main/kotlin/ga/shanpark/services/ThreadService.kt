package ga.shanpark.services

import ga.shanpark.services.signal.AtomicSignal
import ga.shanpark.services.task.Task
import java.util.concurrent.atomic.AtomicReference

class ThreadService: Service {
    private val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
    private val thread = AtomicReference<Thread>()

    override fun start(task: Task) {
        if (thread.compareAndSet(null, Thread { run(task) } ))
            thread.get().start()
        else
            throw IllegalStateException("The service has already been started.")
    }

    override fun stop() {
        stopSignal.signal() // stop을 요청하는 signal을 설정한다. 이후 service의 실행 종료는 task의 구현에 따라 결정된다.
    }

    override fun isRunning(): Boolean {
        return (thread.get() != null) // signal과 상관없이 thread의 실행 여부가 running 상태를 결정한다.
    }

    override fun await(millis: Long) {
        thread.get()?.join(millis)
    }

    private fun run(task: Task) {
        try {
            task.init()
            task.run(stopSignal)
        } finally {
            clear(task) // clear()는 반드시 호출되어야 한다. 여기서 uninit()도 호출된다.
        }
    }

    private fun clear(task: Task) {
        try {
            task.uninit() // task의 uninit() 코드가 먼저 호출되어야 한다.
        } finally {
            thread.set(null)
            stopSignal.reset()
        }
    }
}
