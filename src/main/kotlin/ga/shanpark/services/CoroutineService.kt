package ga.shanpark.services

import ga.shanpark.services.signal.AtomicSignal
import ga.shanpark.services.task.Task
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicBoolean

class CoroutineService(private val coroutineScope: CoroutineScope): Service {
    private val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
    private val running = AtomicBoolean(false)
    private val mutex = Mutex()

    override fun start(task: Task) {
        if (running.compareAndSet(false, true)) {
            coroutineScope.launch {
                mutex.lock()
                try {
                    run(task)
                } finally {
                    mutex.unlock()
                }
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
        runBlocking { // 다른 coroutine에서 호출될 것이다.
            if (millis == 0L) {
                mutex.lock()
                mutex.unlock()
            } else {
                withTimeoutOrNull(millis) {
                    mutex.lock()
                    mutex.unlock()
                }
            }
        }
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