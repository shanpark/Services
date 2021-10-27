package io.github.shanpark.services

import io.github.shanpark.services.signal.AtomicSignal
import io.github.shanpark.services.task.Task
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 핸재 thread에서 task를 실행하는 service이다. 일단 start()가 호출되면 service가 종료될 때까지 block된다.
 *
 * service가 스스로 작업을 끝내고 종료되거나 다른 스레드에서 stop()이 호출되어 service가 종료되면
 * block되었던 스레드는 block이 해제된다.
 */
class SyncService: Service {
    override val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
    private val running = AtomicBoolean(false)

    override fun start(task: Task) {
        if (running.compareAndSet(false, true)) {
            synchronized(stopSignal) { // 여기서 stopSignal은 단순히 lock의 역할일 뿐이다.
                run(task)
            }
        } else {
            throw IllegalStateException("The service has already been started.")
        }
    }

    override fun isRunning(): Boolean {
        return running.get() // signal과 상관없이 running 상태를 따로 관리된다.
    }

    override fun await(millis: Long) {
        synchronized(stopSignal) {} // 여기서 stopSignal은 단순히 lock의 역할일 뿐이다.
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
            running.compareAndSet(true, false)
            stopSignal.reset()
        }
    }
}