package io.github.shanpark.services.task

import io.github.shanpark.services.signal.Signal

/**
 * Service가 실행할 수 있는 Task 객체의 interface.
 * Service 객체를 시작시킬 때 파라미터로 넘겨서 실행하도록 한다.
 * Service에서는 init(), run(), uninit() 순서대로 실행을 할 것이다.
 */
interface Task {
    /**
     * Task의 초기화 작업을 수행하는 메소드이다.
     * run()이 실행되기 전에 실행된다.
     */
    fun init() {}

    /**
     * 실제 Task가 수행할 작업 구현한다.
     * 항상 init()가 먼저 호출되고 난 후에 실행된다.
     * 이 메소드의 실행이 완료된 후에 uninit() 메소드가 호출된다.
     *
     * @param stopSignal 외부(다른 스레드 등)에서 stop()을 호출하여 실행 중단 요청이 되었음을 알려주는 signal 객체.
     */
    fun run(stopSignal: Signal)

    /**
     * Task의 실행이 완료된 후에 정리 작업을 수행하는 메소드이다.
     * init() 또는 run()에서 할당된 자원을 해제하는 작업을 수행한다.
     * 실행 중 예외가 발생하더라도 항상 마지막에 호출된다.
     */
    fun uninit() {}

    /**
     * Task를 실행하는 중에 발생하는 예외 처리를 위한 메소드이다.
     * 어디서든 예외가 발생하면 즉시 호출되며 실행은 중단된다.
     * init(), run()에서 에러가 발생하여 이 메소드가 호출되더라도 uninit()는 호출된다.
     * uninit() 수행중 발생하는 에러에 대해서도 onError()는 호출된다.
     *
     * 기본 구현은 stack trace를 출력한다.
     *
     * @param e 발생한 에러의 throwable 객체
     */
    fun onError(e: Throwable) { e.printStackTrace() }
}
