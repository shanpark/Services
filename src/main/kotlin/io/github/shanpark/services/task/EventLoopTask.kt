package io.github.shanpark.services.task

import io.github.shanpark.services.signal.Signal
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * event loop를 구현하는 task 객체.
 * generic으로 구현되어 event 객체의 타입은 어떤 타입이든 될 수 있다.
 *
 * stop()이 호출되면 queue에 남아있는 event는 모두 버려진다.
 *
 * 이 stop된 task는 재사용 가능하다.
 *
 * @param eventHandler event가 수신되면 호출될 handler function. handler의 parameter로 event객체가 넘어온다.
 * @param timeoutMillis 이 파라미터로 지정된 시간(milliseconds)동안 아무런 이벤트가 수신되지 않으면 idleHandler function이
 *                      호출된다. (default: Long.MAX_VALUE ms)
 * @param idleHandler 일정 시간(timeoutMillis) 동안 이벤트가 수신되지 않으면 호출되는 handler function.
 * @param errorHandler task 실행 중 error 발생 시 호출되는 handler이다.
 */
class EventLoopTask<T: Any>(
    private val eventHandler: (T) -> Unit,
    private val timeoutMillis: Long = Long.MAX_VALUE,
    private val idleHandler: () -> Unit = {},
    private val errorHandler: (Throwable) -> Unit = { it.printStackTrace() }
): Task {
    private val queue: LinkedBlockingQueue<Any> = LinkedBlockingQueue()

    /**
     * EventQueueTask로 Event를 하나 보낸다.
     * 이 Event가 처리 순서가 되면 즉시 eventHandler가 호출될 것이다.
     *
     * @param event queue로 보내는 event 객체.
     */
    fun sendEvent(event: T) {
        queue.add(event)
    }

    override fun run(stopSignal: Signal) {
        var event: Any?
        while (true) {
            event = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS)
            if (stopSignal.isSignalled()) // poll에서 빠져나오면 즉시 stopSignal부터 검사
                break

            @Suppress("UNCHECKED_CAST")
            if (event == null)
                idleHandler()
            else
                eventHandler(event as T)
        }
    }

    override fun uninit() {
        queue.clear()
    }

    override fun stopRequested() {
        queue.add(Any()) // event queue의 receive()가 반환되어야 stopSignal을 검사하므로 즉시 반환되도록 dummy event를 전송
    }

    override fun onError(e: Throwable) {
        errorHandler(e)
    }
}