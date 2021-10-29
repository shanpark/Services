package io.github.shanpark.services.coroutine

import io.github.shanpark.services.signal.AtomicSignal
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/**
 * 주어진 CoroutineScope에서 coroutine을 생성하고 task를 실행한다.
 *
 * 다른 service 구현과 마찬가지로 stop()이 호출되어도 작업 중단을 요청할 뿐 실제 작업의 중단은
 * task의 구현에 달려있으며 coroutine의 job은 cancel되지 않는다.
 * 만약 cancel()을 호출하는 stop()의 구현을 원한다면 stop() 메소드를 override하여 구현할 수 있다.
 *
 * @param coroutineScope 이 서비스가 실행될 coroutine scope. default로 `Dispatchers.Default`에서 실행된다.
 */
class CoroutineService(private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)): CoService {
    override val stopSignal = AtomicSignal() // stop을 요청하는 signal일 뿐이다.
    private val atomicJob = AtomicReference<Job>()

    constructor(context: CoroutineContext): this(CoroutineScope(context))

    override fun start(task: CoTask): CoService {
        if (!atomicJob.compareAndSet(null, coroutineScope.launch { run(task) })) {
            throw IllegalStateException("The service has already been started.")
        }
        return this
    }

    override fun isRunning(): Boolean {
        return (atomicJob.get() != null)
    }

    override fun await(millis: Long) {
        runBlocking {
            if (millis == 0L) {
                atomicJob.get()?.join()
            } else {
                withTimeoutOrNull(millis) {
                    atomicJob.get()?.join()
                }
            }
        }
    }

    private suspend fun run(task: CoTask) {
        try {
            task.init()
            task.run(stopSignal)
        } catch (e: CancellationException) { // 부모 context에서 cancel()될 수 있으며 이 경우 정상적인 동작으로 간주한다.
            // Normal termination.
        } catch (e: Exception) {
            task.onError(e)
        } finally {
            clear(task) // clear()는 반드시 호출되어야 한다. 여기서 uninit()도 호출된다.
        }
    }

    private suspend fun clear(task: CoTask) {
        try {
            task.uninit() // task의 uninit() 코드가 먼저 호출되어야 한다.
        } catch (e: Exception) {
            task.onError(e)
        } finally {
            atomicJob.set(null)
            stopSignal.reset()
        }
    }
}