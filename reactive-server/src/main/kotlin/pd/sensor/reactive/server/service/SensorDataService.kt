package pd.sensor.reactive.server.service

import kotlinx.coroutines.flow.*
import org.springframework.stereotype.Service
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.metrics.MetricsIndicator
import pd.sensor.reactive.server.repository.SensorDataRepository
import java.time.Instant

@Service
class SensorDataService(
    val sensorDataRepository: SensorDataRepository
) {

    private val sender: MutableSharedFlow<SensorData> = MutableSharedFlow()

    fun stream(): Flow<SensorData> = sender

    fun latest(): Flow<SensorData> =
        sensorDataRepository.findLatest().mapToDomain()

    fun after(since: Instant): Flow<SensorData> =
        sensorDataRepository.findLatest(since).mapToDomain()

    suspend fun post(inboundFlow: Flow<SensorData>) =
        inboundFlow
            .onEach { sender.emit(it) }
            .map { it.toEntity() }
            .let { sensorDataRepository.saveAll(it) }
            .collect()
}