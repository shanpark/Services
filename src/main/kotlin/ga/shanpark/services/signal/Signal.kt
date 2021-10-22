package ga.shanpark.services.signal

interface Signal {
    /**
     * 특정 상태가 되었음을 표시하도록 flag를 설정한다.
     */
    fun signal()

    /**
     * 현재 상태를 다시 초기 상태로 되돌린다.
     */
    fun reset()

    /**
     * signal()이 호출되어 특정 상태가 되었는지 여부를 반환한다.
     *
     * @return 상태값 반환.
     */
    fun isSignalled(): Boolean
}