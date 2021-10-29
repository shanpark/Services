package io.github.shanpark.services.task

import io.github.shanpark.services.signal.Signal

/**
 * Task 구현 객체로서 생성할 때 init, run, uninit 함수를 받아서 task의 init, run, uninit 함수에서
 * 호출해주는 단순한 Task의 wrapper이다.
 *
 * property를 갖는 Task를 구현할 수 없기 때문에 단순히 action만을 갖는 task를 정의할 때 간단하게 사용할 수 있다.
 *
 * @param init init() 메소드에서 실행될 함수 객체.
 * @param run run() 메소드에서 실행될 함수 객체.
 * @param uninit uninit() 메소드에서 실행될 함수 객체.
 */
class SimpleTask(private val init: (() -> Unit)? = null, private val run: (Signal) -> Unit, private val uninit: (() -> Unit)? = null): Task {
    override fun init() {
        init?.invoke()
    }

    override fun run(stopSignal: Signal) {
        run.invoke(stopSignal)
    }

    override fun uninit() {
        uninit?.invoke()
    }
}