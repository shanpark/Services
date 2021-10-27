package io.github.shanpark.services.signal

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Multi threaded 환경에서 사용할 때 동기화 문제가 없는 단순한 Signal 클래스.
 */
class AtomicSignal: Signal {
    private val signal: AtomicBoolean = AtomicBoolean(false)

    override fun signal() {
        signal.compareAndSet(false, true)
    }

    override fun reset() {
        signal.set(false)
    }

    override fun isSignalled(): Boolean {
        return signal.get()
    }
}