package ga.shanpark.services.task

import ga.shanpark.services.signal.Signal
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * event loop를 구현하는 task 객체.
 * generic으로 구현되어 event 객체의 타입은 어떤 타입이든 될 수 있다.
 *
 * @param eventHandler event가 수신되면 호출될 handler function. handler의 parameter로 event객체가 넘어온다.
 * @param idleHandler 일정 시간(timeoutMillis) 동안 이벤트가 수신되지 않으면 호출되는 handler function.
 * @param timeoutMillis 이 파라미터로 지정된 시간(milliseconds)동안 아무런 이벤트가 수신되지 않으면 idleHandler function이 호출된다.
 */
class EventQueueTask<T>(private val eventHandler: (T) -> Unit, private val idleHandler: () -> Unit = {}, private val timeoutMillis: Long = Long.MAX_VALUE) : Task {
    private lateinit var queue: LinkedBlockingQueue<T>

    /**
     * EventQueueTask로 Event를 하나 보낸다.
     * 이 Event가 처리 순서가 되면 즉시 eventHandler가 호출될 것이다.
     *
     * @param event queue로 보내는 event 객체.
     */
    fun sendEvent(event: T) {
        queue.add(event)
    }

    override fun init() {
        queue = LinkedBlockingQueue()
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