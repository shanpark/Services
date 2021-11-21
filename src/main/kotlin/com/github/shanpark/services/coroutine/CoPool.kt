package com.github.shanpark.services.coroutine

import kotlinx.coroutines.channels.Channel

/**
 * event로 사용되는 객체의 garbage가 양산되는 현상을 막기 위해서 event 객체의 pool을 제공한다.
 * 최대 maxSize 만큼의 event 객체를 보관하고 있다가 필요할 때 새로 할당하지 않고 보관된 객체를 즉시 제공한다.
 *
 * coroutines 패키지의 Channel 클래스를 이용하여 구현되었기 때문에 coroutine 에서 사용될 수 있도록 구현했지만
 * 실제로 get(), ret() 메소드는 suspend가 아니다. 내부 구현도 역시 Channel의 suspend 함수는 사용하지 않기 때문에
 * suspend가 발생하는 로직은 없다.
 *
 * @param factory event 객체를 생성하는 factory 함수.
 * @param maxSize pool이 최대롤 보관하는 event 객체의 수.
 */
class CoPool<T>(private val factory: () -> T, private val maxSize: Int = 10) {
    private val pool = Channel<T>(maxSize)

    /**
     * 보관된 event 객체를 하나 반환한다. 보관된 event 객체가 없는 경우에는 새로 할당하여 반환한다.
     * 새로 객체를 할당할 때는 생성자에서 지정한 factory 함수를 이용하여 할당한다.
     * 여기서 반환해주는 event 객체는 이전에 사용했던 객체이므로 당연히 다시 초기화를 해서 사용해야 한다.
     *
     * @return 새로 할당되었거나 pool에 보관되어 있던 event 객체.
     */
    fun get(): T {
        val result = pool.tryReceive()
        if (result.isSuccess)
            return result.getOrThrow()
        return factory()
    }

    /**
     * 사용이 끝난 event 객체를 pool로 반환한다.
     * 만약 재사용 가능한 event 객체가 충분하다면 객체는 보관되지 않고 버려질 것이다.
     *
     * @param event 사용을 끝내고 pool로 반환하는 event 객체.
     */
    fun ret(event: T) {
        pool.trySend(event) // result not used.
    }
}