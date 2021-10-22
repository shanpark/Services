package ga.shanpark.services

import ga.shanpark.services.signal.AtomicSignal
import ga.shanpark.services.task.Task
import java.util.concurrent.atomic.AtomicBoolean

class SyncService: Service {
    private val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
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

    override fun stop() {
        stopSignal.signal() // stop을 요청하는 signal을 설정한다. 이후 service의 실행 종료는 task의 구현에 따라 결정된다.
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