package ga.shanpark.services

import ga.shanpark.services.task.Task

/**
 * Task를 실행하는 클래스.
 * 한 번 시작된 Service 객체는 종료될 때까지 다시 시작될 수 없다.
 * 지정된 Task와는 별개로 running 상태가 아닌 idle 상태의 Service는 얼마든지 재사용 될 수 있다.
 */
interface Service {
    /**
     * Task의 실행을 시작 시킨다.
     * 이미 시작된 Service를 다시 시작시키면 IllegalStateException을 발생시킨다.
     * 이미 종료된 Service는 다시 재사용이 가능하다. 다른 Task를 이용해서 다시 시작시킬 수 있다.
     *
     * @param task 이 Service가 실행할 task.
     * @throws IllegalStateException 이미 start가 된 상태의 Service를 다시 start 시키면 발생한다.
     */
    fun start(task: Task)

    /**
     * Service의 실행을 중지 요청한다.
     * 실행 중지를 요청할 뿐 강제로 종료하지는 못한다. 요청에 따른 실제 중지 여부는 Task의 구현에 따라 다르다.
     * Task에서는 stopSignal이 signalled상태가 되면 가능한 빨리 실행을 종료하도록 구현되어야 한다.
     */
    fun stop()

    /**
     * Service의 실행 상태를 반환한다.
     *
     * @return 실행중이면 true, 아니면 false를 반환한다.
     */
    fun isRunning(): Boolean

    /**
     * Service가 실행 종료될 때 까지 대기한다.
     * 만약 아직 Service가 시작되지 않았거나 이미 종료된 상태라면 즉시 리턴한다.
     *
     * @param millis 최대 대기 시간(milliseconds). 0이면 종료될 때까지 무제한 대기한다.
     */
    fun await(millis: Long = 0)
}