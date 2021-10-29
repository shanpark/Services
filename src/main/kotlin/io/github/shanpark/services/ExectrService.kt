package io.github.shanpark.services

import io.github.shanpark.services.task.Task
import io.github.shanpark.services.signal.AtomicSignal
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * java의 concurrent 패키지에 Executor를 이용하는 service 클래스이다.
 * ExecutorService의 thread에서 task를 수행한다.
 *
 * 동작 자체는 ThreadService와 동일하다.
 * concurrent 패키지에서 제공하는 ExecutorService와 이름이 중복되기 때문에 의도적으로 ExectrService라고 명명하였다.
 */
class ExectrService(private val executor: ExecutorService) : Service {
    override val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
    private val future = AtomicReference<Future<*>>()

    override fun start(task: Task) {
        if (!future.compareAndSet(null, executor.submit { run(task) }))
            throw IllegalStateException("The service has already been started.")
    }

    override fun isRunning(): Boolean {
        return (future.get() != null) // signal과 상관없이 thread의 실행 여부가 running 상태를 결정한다.
    }

    override fun await(millis: Long) {
        if (millis == 0L)
            future.get()?.get()
        else
            future.get()?.get(millis, TimeUnit.MILLISECONDS)
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
            future.set(null)
            stopSignal.reset()
        }
    }
}
