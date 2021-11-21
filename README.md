# Services

[![](https://jitpack.io/v/shanpark/services.svg)](https://jitpack.io/#shanpark/services)

### SyncService example

```kotlin
val task = task { /* long running task */ }
val service = SyncService()

service.start(task) // SyncService는 시작하면 종료될 때 까지 block
// start()가 리턴되면 service는 종료됨.

    ...

// 다른 스레드에서 stop()을 요청.
service.stop()
```
### ThreadService example

```kotlin
val task = task { /* long running task */ }
val service = ThreadService()

service.start(task) // TheradService이므로 새로우 thread가 생성된다.

if (service.isRunning()) {
    // do something
}

service.await() // service가 종료될 때 까지 현재 스레드를 block. 

    ...


// 다른 스레드에서 stop()을 요청.
service.stop()
```

### ExectrService example

```kotlin
val task = task { /* long running task */ }
val service = ExectrService(Executors.newSingleThreadExecutor())

service.start(task) // TheradService이므로 새로우 thread가 생성된다.

if (service.isRunning()) {
    // do something
}

service.await() // service가 종료될 때 까지 현재 스레드를 block. 

    ...


// 다른 스레드에서 stop()을 요청.
service.stop()
```

### CoroutineService example

```kotlin
val task = coTask { /* long running task */ }
val service = CoroutineService()

service.start(task) // CoroutineService이므로 새로운 coroutine이 생성된다.

if (service.isRunning()) {
    // do something
}

service.await() // service가 종료될 때 까지 현재 스레드를 block. 

    ...


// 다른 coroutine(suspend function)에서 stop()을 요청.
service.stop()
```

## Install

To install the library add: 

* Gradle

```gradle
repositories {
    ...
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.shanpark:services:0.0.8'
}
```

* Gradle(Kotlin)

```gradle
repositories { 
    ...
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation('com.github.shanpark:services:0.0.8')
}
```