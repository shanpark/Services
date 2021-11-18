package com.github.shanpark.services.util

import com.github.shanpark.services.Service
import com.github.shanpark.services.coroutine.CoTask
import com.github.shanpark.services.coroutine.SimpleCoTask
import com.github.shanpark.services.signal.Signal
import com.github.shanpark.services.task.SimpleTask
import com.github.shanpark.services.task.Task

/**
 * 파라미터로 받은 모든 service들이 종료될 때 까지 대기한다.
 *
 * @param services 종료를 기다릴 service들.
 */
fun await(vararg services: Service) {
    for (service in services)
        service.await()
}

/**
 * run() 에서 실행될 함수객체를 받아서 Task 객체를 생성하여 반환.
 * task의 init()와, uninit() 에서는 아무 일도 하지 않는다.
 *
 * @param run 생성되는 task의 run 메소드에서 실행될 함수 객체.
 *
 * @return 생성된 task 객체를 반환한다.
 */
fun task(run: (Signal) -> Unit): Task {
    return task({}, run) // run만 넘기면 재귀호출이 된다.
}

/**
 * init(), run(), uninit() 에서 각각 실행될 함수객체를 받아서 Task 객체를 생성하여 반환.
 * init와 uninit는 optional이므로 구현하지 않으려면 null을 지정한다.
 *
 * @param init 생성되는 task의 init 메소드에서 실행될 함수 객체. default는 null이다.
 * @param run 생성되는 task의 run 메소드에서 실행될 함수 객체.
 * @param uninit 생성되는 task의 uninit 메소드에서 실행될 함수 객체. default는 null이다.
 *
 * @return 생성된 task 객체를 반환한다.
 */
fun task(
    init: () -> Unit = {},
    run: (Signal) -> Unit,
    uninit: () -> Unit = {},
    stopRequested: () -> Unit = {},
    onError: (Throwable) -> Unit = { it.printStackTrace() }
): Task {
    return SimpleTask(init, run, uninit, stopRequested, onError)
}

/**
 * run() 에서 실행될 함수객체를 받아서 Task 객체를 생성하여 반환.
 * task의 init()와, uninit() 에서는 아무 일도 하지 않는다.
 *
 * @param run 생성되는 task의 run 메소드에서 실행될 함수 객체.
 *
 * @return 생성된 task 객체를 반환한다.
 */
fun coTask(run: suspend (Signal) -> Unit): CoTask {
    return coTask({}, run) // run만 넘기면 재귀호출이 된다.
}

/**
 * init(), run(), uninit() 에서 각각 실행될 함수객체를 받아서 Task 객체를 생성하여 반환.
 * init와 uninit는 optional이므로 구현하지 않으려면 null을 지정한다.
 *
 * @param init 생성되는 task의 init 메소드에서 실행될 함수 객체. default는 null이다.
 * @param run 생성되는 task의 run 메소드에서 실행될 함수 객체.
 * @param uninit 생성되는 task의 uninit 메소드에서 실행될 함수 객체. default는 null이다.
 *
 * @return 생성된 task 객체를 반환한다.
 */
fun coTask(
    init: suspend () -> Unit = {},
    run: suspend (Signal) -> Unit,
    uninit: suspend () -> Unit = {},
    stopRequested: suspend () -> Unit = {},
    onError: suspend (Throwable) -> Unit = { it.printStackTrace() }
): CoTask {
    return SimpleCoTask(init, run, uninit, stopRequested, onError)
}