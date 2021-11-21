import com.github.shanpark.services.ThreadService
import com.github.shanpark.services.coroutine.CoPool
import com.github.shanpark.services.coroutine.CoroutineService
import com.github.shanpark.services.util.Pool
import com.github.shanpark.services.util.await
import com.github.shanpark.services.util.coTask
import com.github.shanpark.services.util.task
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class UtilTest {

    @Test
    @DisplayName("await() 테스트")
    internal fun awaitTest() {
        val task = task {
            while (true) {
                Thread.sleep(100)
                if (it.isSignalled())
                    break
            }
        }

        val service1 = ThreadService()
        val service2 = ThreadService()
        val service3 = ThreadService()
        val service4 = ThreadService()

        service1.start(task)
        service2.start(task)
        service3.start(task)
        service4.start(task)

        Thread {
            Thread.sleep(300)
            service1.stop()
            Thread.sleep(300)
            service2.stop()
            Thread.sleep(300)
            service3.stop()
            Thread.sleep(300)
            service4.stop()
        }.start()

        val startTime = System.currentTimeMillis()
        await(service3, service1, service4, service2)
        val elapsedTime = System.currentTimeMillis() - startTime

        assertThat(service1.isRunning()).isFalse
        assertThat(service2.isRunning()).isFalse
        assertThat(service3.isRunning()).isFalse
        assertThat(service4.isRunning()).isFalse

        assertThat(elapsedTime).isCloseTo(1200, Percentage.withPercentage(10.0))
    }

    @Test
    @DisplayName("task factory함수 테스트")
    internal fun taskFactoryTest() {
        val sb = StringBuilder()
        val task = task({
            sb.append("Init")
        }, {
            sb.append(" Run ")
        }, {
            sb.append("Uninit")
        })

        ThreadService().start(task).await()

        assertThat(sb.toString()).isEqualTo("Init Run Uninit")
    }

    @Test
    @DisplayName("coTask factory함수 테스트")
    internal fun coTaskFactoryTest() {
        val sb = StringBuilder()
        val task = coTask({
            sb.append("Init")
        }, {
            sb.append(" Run ")
        }, {
            sb.append("Uninit")
        })

        CoroutineService().start(task).await()

        assertThat(sb.toString()).isEqualTo("Init Run Uninit")
    }

    class Event(var id: Int) {
        companion object {
            fun new(): Event {
                return Event(0)
            }
        }
    }

    @Test
    @DisplayName("EventPool 테스트")
    internal fun eventPoolTest() {
        val pool = Pool(Event::new)

        var event = pool.get()
        event.id = 100
        pool.ret(event)

        event = pool.get()
        assertThat(event.id).isEqualTo(100)

        val coPool = CoPool(Event::new)

        event = coPool.get()
        event.id = 100
        coPool.ret(event)

        event = coPool.get()
        assertThat(event.id).isEqualTo(100)
    }
}