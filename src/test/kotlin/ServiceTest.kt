import ga.shanpark.services.ExectrService
import ga.shanpark.services.SyncService
import ga.shanpark.services.ThreadService
import ga.shanpark.services.coroutine.CoTask
import ga.shanpark.services.coroutine.CoroutineService
import ga.shanpark.services.signal.AtomicSignal
import ga.shanpark.services.signal.Signal
import ga.shanpark.services.task.Task
import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Fail
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

internal class TestTask: Task {
    var value: Int = 0

    override fun init() {
        value = 100
    }

    override fun run(stopSignal: Signal) {
        while (!stopSignal.isSignalled()) {
            Thread.sleep(1000)
            value++
        }
    }

    override fun uninit() {
        value = 200
    }
}

internal class TestCoTask: CoTask {
    var value: Int = 0

    override suspend fun init() {
        value = 100
    }

    override suspend fun run(stopSignal: Signal) {
        while (!stopSignal.isSignalled()) {
            delay(1000)
            value++
        }
    }

    override suspend fun uninit() {
        value = 200
    }
}

internal class ServiceTest {

    @Test
    @DisplayName("SyncService 테스트")
    internal fun syncServiceTest() {
        val task = TestTask()
        val service = SyncService()
        val errorSignal = AtomicSignal()

        Thread { // 1.5초 뒤 stop을 요청하는 thread.
            Thread.sleep(1500)
            service.stop()
        }.start()

        Thread { // 종료될 때까지 기다리는 thread.
            Thread.sleep(500)
            service.await()
            if (service.isRunning())
                errorSignal.signal()
        }.start()

        service.start(task) // 실제 service가 시작되고 종료될 때 까지 block.
        assertThat(service.isRunning()).isFalse

        assertThat(task.value).isEqualTo(200)

        Thread.sleep(100)
        assertThat(errorSignal.isSignalled()).isFalse

        try {
            service.await(0) // 에러없이 즉시 리턴되어야 함.
            service.stopAndAwait() // 에러없이 즉시 리턴되어야 함.
        } catch(e: Exception) {
            Fail.fail("stopAndWait() failed.")
        }
    }

    @Test
    @DisplayName("ThreadService 테스트")
    internal fun threadServiceTest() {
        val task = TestTask()
        val service = ThreadService()
        val errorSignal = AtomicSignal()

        Thread { // 1.5초 뒤 stop을 요청하는 thread.
            Thread.sleep(1500)
            service.stop()
        }.start()

        Thread { // 종료될 때까지 기다리는 thread.
            Thread.sleep(500)
            service.await()
            if (service.isRunning())
                errorSignal.signal()
        }.start()

        service.start(task) // 실제 service 시작.
        assertThat(service.isRunning()).isTrue
        Thread.sleep(500)
        assertThat(task.value).isEqualTo(100)

        Thread.sleep(1000)
        assertThat(task.value).isEqualTo(101)

        service.await()
        assertThat(service.isRunning()).isFalse

        assertThat(task.value).isEqualTo(200)

        Thread.sleep(100)
        assertThat(errorSignal.isSignalled()).isFalse

        try {
            service.await(0) // 에러없이 즉시 리턴되어야 함.
            service.stopAndAwait() // 에러없이 즉시 리턴되어야 함.
        } catch(e: Exception) {
            Fail.fail("stopAndWait() failed.")
        }
    }

    @Test
    @DisplayName("ExecutorService 테스트")
    internal fun executorServiceTest() {
        val executorService = Executors.newSingleThreadExecutor()
        val task = TestTask()
        val service = ExectrService(executorService)
        val errorSignal = AtomicSignal()

        Thread { // 1.5초 뒤 stop을 요청하는 thread.
            Thread.sleep(1500)
            service.stop()
        }.start()

        Thread { // 종료될 때까지 기다리는 thread.
            Thread.sleep(500)
            service.await()
            if (service.isRunning())
                errorSignal.signal()
        }.start()

        service.start(task) // 실제 service 시작.
        assertThat(service.isRunning()).isTrue
        Thread.sleep(500)
        assertThat(task.value).isEqualTo(100)

        Thread.sleep(1000)
        assertThat(task.value).isEqualTo(101)

        service.await()
        assertThat(service.isRunning()).isFalse

        assertThat(task.value).isEqualTo(200)

        Thread.sleep(100)
        assertThat(errorSignal.isSignalled()).isFalse

        try {
            service.await(0) // 에러없이 즉시 리턴되어야 함.
            service.stopAndAwait() // 에러없이 즉시 리턴되어야 함.
        } catch(e: Exception) {
            Fail.fail("stopAndWait() failed.")
        }
    }

    @Test
    @DisplayName("CoroutineService 테스트")
    internal fun coroutineServiceTest() {
        val task = TestCoTask()
        val service = CoroutineService(CoroutineScope(Dispatchers.Default + CoroutineName("test-coroutine")))
        val errorSignal = AtomicSignal()

        Thread { // 1.5초 뒤 stop을 요청하는 thread.
            println("stopThread started.")
            Thread.sleep(1500)
            service.stop()
            println("stopThread stopped.")
        }.start()

        Thread { // 종료될 때까지 기다리는 thread.
            println("waitThread started.")
            Thread.sleep(500)
            service.await()
            if (service.isRunning())
                errorSignal.signal()
            println("waitThread stopped.")
        }.start()

        println("service start >>>")
        service.start(task) // 실제 service 시작.
        assertThat(service.isRunning()).isTrue
        Thread.sleep(500)
        assertThat(task.value).isEqualTo(100)

        Thread.sleep(1000)
        assertThat(task.value).isEqualTo(101)

        service.await()
        assertThat(service.isRunning()).isFalse
        println("service stopped >>>")

        assertThat(task.value).isEqualTo(200)

        Thread.sleep(100)
        assertThat(errorSignal.isSignalled()).isFalse

        try {
            service.await(0) // 에러없이 즉시 리턴되어야 함.
            service.stopAndAwait() // 에러없이 즉시 리턴되어야 함.
        } catch(e: Exception) {
            Fail.fail("stopAndWait() failed.")
        }
    }
}