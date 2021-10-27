package ga.shanpark.services.coroutine

import ga.shanpark.services.signal.Signal
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeoutOrNull

/**
 * event loop를 구현하는 coroutine task 객체.
 * generic으로 구현되어 event 객체의 타입은 어떤 타입이든 될 수 있다.
 *
 * stop()이 호출되어도 이벤트 대기 timeout이 발생할 때 까지는 종료되지 않는다.
 * timeout이 발생해서 stopSignal로 stop 요청이 되었음을 인지하면 더 이상 loop를 계속하지 않고 빠져나온다.
 * 이 때 queue에 남아있는 event는 모두 버려진다.
 *
 * 이 stop된 task는 재사용 가능하다.
 *
 * @param eventHandler event가 수신되면 호출될 handler function. handler의 parameter로 event객체가 넘어온다.
 * @param timeoutMillis 이 파라미터로 지정된 시간(milliseconds)동안 아무런 이벤트가 수신되지 않으면 idleHandler function이
 *                      호출된다. 또한 이 때 event를 wait하는 상태가 풀리고 stopSignal을 검사하게 되므로 너무 큰 값을 지정하면
 *                      stop()이 오래걸리게 되므로 1000ms정도가 적당하다. (default: 1000ms)
 *                      timeout 시간을 길게 할 경우 stop()을 호출하고나서 적당히 무시할 수 있는 event를 보내주면 즉시 종료시킬 수
 *                      있으므로 무시할 수 있는 적당한 종료 event를 정해서 사용하면 된다.
 * @param idleHandler 일정 시간(timeoutMillis) 동안 이벤트가 수신되지 않으면 호출되는 handler function.
 */
class EventLoopCoTask<T>(private val eventHandler: suspend (T) -> Unit, private val timeoutMillis: Long = 1000, private val idleHandler: suspend () -> Unit = {}): CoTask {
    private var queue: Channel<T> = Channel(Channel.UNLIMITED)

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

            if (event != null)
                eventHandler(event)
            else
                idleHandler()
        }
    }

    override suspend fun uninit() {
        val oldQueue = queue
        queue = Channel(Channel.UNLIMITED) // 재사용 가능하도록 channel을 새로 할당한다.
        oldQueue.close()
    }
}