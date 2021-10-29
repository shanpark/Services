import io.github.shanpark.services.SyncService
import io.github.shanpark.services.ThreadService
import io.github.shanpark.services.coroutine.CoroutineService
import io.github.shanpark.services.coroutine.EventLoopCoTask
import io.github.shanpark.services.task.EventLoopTask
import io.github.shanpark.services.util.task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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

    @Test
    @DisplayName("Task ErrorHandler 테스트")
    internal fun taskErrorHandlerTest() {
        val sb = StringBuilder()
        val task = task({
            sb.append("Init ")
        }, {
            throw RuntimeException("Error ")
        }, {
            sb.append("Uninit ")
        }, {
            sb.append(it.message)
        })
        SyncService().start(task)

        assertThat(sb.toString()).isEqualTo("Init Error Uninit ")
    }
}