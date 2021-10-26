package ga.shanpark.services.task

import ga.shanpark.services.signal.Signal
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * event loop를 구현하는 task 객체.
 * generic으로 구현되어 event 객체의 타입은 어떤 타입이든 될 수 있다.
 *
 * @param eventHandler event가 수신되면 호출될 handler function. handler의 parameter로 event객체가 넘어온다.
 * @param timeoutMillis 이 파라미터로 지정된 시간(milliseconds)동안 아무런 이벤트가 수신되지 않으면 idleHandler function이
 *                      호출된다. 또한 이 때 event를 wait하는 상태가 풀리고 stopSignal을 검사하게 되므로 너무 큰 값을 지정하면
 *                      stop()이 오래걸리게 되므로 1000ms정도가 적당하다. (default: 1000ms)
 *                      timeout 시간을 길게 할 경우 stop()을 호출하고나서 적당히 무시할 수 있는 event를 보내주면 즉시 종료시킬 수
 *                      있으므로 무시할 수 있는 적당한 종료 event를 정해서 사용하면 된다.
 * @param idleHandler 일정 시간(timeoutMillis) 동안 이벤트가 수신되지 않으면 호출되는 handler function.
 */
class EventQueueTask<T>(private val eventHandler: (T) -> Unit, private val timeoutMillis: Long = 1000, private val idleHandler: () -> Unit = {}) : Task {
    private val queue: LinkedBlockingQueue<T> = LinkedBlockingQueue()

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
        var event: T?
        while (!stopSignal.isSignalled()) {
            event = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS)
            if (event == null)
                idleHandler()
            else
                eventHandler(event)
        }
    }

    override fun uninit() {
        queue.clear()
    }
}