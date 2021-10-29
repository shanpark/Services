import io.github.shanpark.services.ThreadService
import io.github.shanpark.services.coroutine.CoroutineService
import io.github.shanpark.services.coroutine.EventLoopCoTask
import io.github.shanpark.services.task.EventLoopTask
import io.github.shanpark.services.task.Task
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.lang.StringBuilder

internal class TaskTest {

    @Test
    @DisplayName("EventLoopTask 테스트")
    internal fun eventQueueTaskTest() {
        val sb = StringBuilder()

        val task = EventLoopTask<String>(
            { event -> sb.append(event) }, 500, { sb.append("Idle") }
        )
        val service = ThreadService()

        service.start(task)

        Thread {
            Thread.sleep(100)
            task.sendEvent("Hello")
            Thread.sleep(600)
            task.sendEvent("World")
        }.start()

        Thread.sleep(800)
        service.stopAndAwait()

        assertThat(sb.toString()).isEqualTo(("HelloIdleWorld"))
    }

    @Test
    @DisplayName("EventLoopCoTask 테스트")
    internal fun eventLoopCoTaskTest() {
        val sb = StringBuilder()

        val task = EventLoopCoTask<String>(
            { event -> sb.append(event) }, 500, { sb.append("Idle") }
        )
        val service = CoroutineService()

        service.start(task)

        CoroutineScope(Dispatchers.Default).launch {
            delay(100)
            task.sendEvent("Hello")
            delay(600)
            task.sendEvent("World")
        }

        Thread.sleep(800)
        service.stopAndAwait()

        assertThat(sb.toString()).isEqualTo(("HelloIdleWorld"))
    }
}