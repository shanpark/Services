import ga.shanpark.services.ThreadService
import ga.shanpark.services.task.EventQueueTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.StringBuilder

internal class TaskTest {

    @Test
    @DisplayName("EventQueueTask 테스트")
    internal fun eventQueueTaskTest() {
        val sb = StringBuilder()

        val task = EventQueueTask<String>(
            { event -> sb.append(event) }, 1000
        )
        val service = ThreadService()

        service.start(task)

        Thread { // 1.5초 뒤 stop을 요청하는 thread.
            Thread.sleep(100)
            task.sendEvent("Hello")
            Thread.sleep(100)
            task.sendEvent("World")
        }.start()

        Thread.sleep(1000)
        service.stopAndAwait()

        assertThat(sb.toString()).isEqualTo(("HelloWorld"))
    }
}