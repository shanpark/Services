import ga.shanpark.services.ThreadService
import ga.shanpark.services.signal.Signal
import ga.shanpark.services.task.Task
import ga.shanpark.services.util.await
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class UtilTest {

    internal class SomeTask: Task {
        override fun run(stopSignal: Signal) {
            while (true) {
                Thread.sleep(100)
                if (stopSignal.isSignalled())
                    break
            }
        }
    }

    @Test
    @DisplayName("await() 테스트")
    internal fun awaitTest() {
        val task = SomeTask()
        val service1 = ThreadService()
        val service2 = ThreadService()
        val service3 = ThreadService()
        val service4 = ThreadService()

        service1.start(task)
        service2.start(task)
        service3.start(task)
        service4.start(task)

        Thread {
            Thread.sleep(1000)
            service1.stop()
            Thread.sleep(1000)
            service2.stop()
            Thread.sleep(1000)
            service3.stop()
            Thread.sleep(1000)
            service4.stop()
        }.start()

        val startTime = System.currentTimeMillis()
        await(service3, service1, service4, service2)
        val elapsedTime = System.currentTimeMillis() - startTime
        println("elapsed: $elapsedTime")

        assertThat(service1.isRunning()).isFalse
        assertThat(service2.isRunning()).isFalse
        assertThat(service3.isRunning()).isFalse
        assertThat(service4.isRunning()).isFalse

        assertThat(elapsedTime).isCloseTo(4000, Percentage.withPercentage(3.0))
    }
}