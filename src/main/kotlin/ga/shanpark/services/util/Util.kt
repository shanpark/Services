package ga.shanpark.services.util

import ga.shanpark.services.Service

/**
 * 파라미터로 받은 모든 service들이 종료될 때 까지 대기한다.
 *
 * @param services 종료를 기다릴 service들.
 */
fun await(vararg services: Service) {
    for (service in services)
        service.await()
}
