package com.github.shanpark.services.coroutine

import com.github.shanpark.services.signal.Signal

/**
 * CoTask 구현 객체로서 생성할 때 init, run, uninit, onError 함수를 받아서 task의 init, run, uninit, onError 함수에서
 * 호출해주는 단순한 CoTask의 wrapper이다.
 *
 * 내부 상태(property)를 갖는 Task를 구현할 수 없기 때문에 단순히 action만을 갖는 task를 정의할 때 간단하게 사용할 수 있다.
 *
 * @param init init() 메소드에서 실행될 함수 객체.
 * @param run run() 메소드에서 실행될 함수 객체.
 * @param uninit uninit() 메소드에서 실행될 함수 객체.
 * @param stopRequested stopRequested() 메소드에서 실행될 함수 객체.
 * @param onError onError() 메소드에서 실행될 함수 객체.
 */
class SimpleCoTask(
    private val init: suspend () -> Unit = {},
    private val run: suspend (Signal) -> Unit,
    private val uninit: suspend () -> Unit = {},
    private val stopRequested: suspend () -> Unit = {},
    private val onError: suspend (Throwable) -> Unit = { it.printStackTrace() }
) : CoTask {
    override suspend fun init() {
        init.invoke() // name 충돌로 invoke()로 호출할 것.
    }

    override suspend fun run(stopSignal: Signal) {
        run.invoke(stopSignal) // name 충돌로 invoke()로 호출할 것.
    }

    override suspend fun uninit() {
        uninit.invoke() // name 충돌로 invoke()로 호출할 것.
    }

    override suspend fun stopRequested() {
        stopRequested.invoke() // name 충돌로 invoke()로 호출할 것.
    }

    override suspend fun onError(e: Throwable) {
        onError.invoke(e) // name 충돌로 invoke()로 호출할 것.
    }
}