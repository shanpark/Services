package com.github.shanpark.services.coroutine

import com.github.shanpark.services.signal.Signal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

/**
 * event loop를 구현하는 coroutine task 객체.
 * generic으로 구현되어 event 객체의 타입은 어떤 타입이든 될 수 있다.
 *
 * stop()이 호출되면 queue에 남아있는 event는 모두 버려진다.
 *
 * 이 stop된 task는 재사용 가능하다.
 *
 * @param eventHandler event가 수신되면 호출될 handler function. handler의 parameter로 event객체가 넘어온다.
 * @param timeoutMillis 이 파라미터로 지정된 시간(milliseconds)동안 아무런 이벤트가 수신되지 않으면 idleHandler function이
 *                      호출된다. (default: Long.MAX_VALUE ms)
 *                      timeout 시간을 길게 할 경우 stop()을 호출하고나서 적당히 무시할 수 있는 event를 보내주면 즉시 종료시킬 수
 *                      있으므로 무시할 수 있는 적당한 종료 event를 정해서 사용하면 된다.
 * @param idleHandler 일정 시간(timeoutMillis) 동안 이벤트가 수신되지 않으면 호출되는 handler function.
 * @param errorHandler task 실행 중 error 발생 시 호출되는 handler이다.
 */
class EventLoopCoTask<T: Any>(
    private val eventHandler: suspend (T) -> Unit,
    private val timeoutMillis: Long = Long.MAX_VALUE,
    private val idleHandler: suspend () -> Unit = {},
    private val errorHandler: suspend (Throwable) -> Unit = { it.printStackTrace() }
): CoTask {
    private var queue: Channel<Any> = Channel(Channel.UNLIMITED)

    /**
     * EventLoopCoTask로 Event를 하나 보낸다.
     * 이 Event가 처리 순서가 되면 즉시 eventHandler가 호출될 것이다.
     *
     * @param event queue로 보내는 event 객체.
     */
    suspend fun sendEvent(event: T) {
        queue.send(event)
    }

    override suspend fun run(stopSignal: Signal) {
        while (true) {
            val event = withTimeoutOrNull(timeoutMillis) { queue.receive() }
            if (stopSignal.isSignalled()) // receive에서 빠져나오면 즉시 stopSignal부터 검사
                break

            @Suppress("UNCHECKED_CAST")
            if (event == null)
                idleHandler()
            else
                eventHandler(event as T)
        }
    }

    override suspend fun uninit() {
        val oldQueue = queue
        queue = Channel(Channel.UNLIMITED) // 재사용 가능하도록 channel을 새로 할당한다.
        oldQueue.close()
    }

    override suspend fun stopRequested() {
        queue.send(Any()) // event queue의 receive()가 반환되어야 stopSignal을 검사하므로 즉시 반환되도록 dummy event를 전송
    }

    override suspend fun onError(e: Throwable) {
        errorHandler(e)
    }
}