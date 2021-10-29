package io.github.shanpark.services

import io.github.shanpark.services.signal.AtomicSignal
import io.github.shanpark.services.task.Task
import java.util.concurrent.atomic.AtomicReference

/**
 * 새로운 thread를 생성하여 그 thread에서 task를 수행한다.
 *
 * 서비스를 시작하면 매 번 새로운 thread를 생성하지만 한 service는 중첩해서 실행될 수 없기 떄문에
 * Service가 실행되는 동안에는 1개의 thread만 존재한다.
 */
class ThreadService: Service {
    override val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
    private val thread = AtomicReference<Thread>()

    override fun start(task: Task): Service {
        if (thread.compareAndSet(null, Thread { run(task) } )) {
            thread.get().start()
            return this
        } else {
            throw IllegalStateException("The service has already been started.")
        }
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
        } catch (e: Exception) {
            task.onError(e)
        } finally {
            clear(task) // clear()는 반드시 호출되어야 한다. 여기서 uninit()도 호출된다.
        }
    }

    private fun clear(task: Task) {
        try {
            task.uninit() // task의 uninit() 코드가 먼저 호출되어야 한다.
        } catch (e: Exception) {
            task.onError(e)
        } finally {
            thread.set(null)
            stopSignal.reset()
        }
    }
}
