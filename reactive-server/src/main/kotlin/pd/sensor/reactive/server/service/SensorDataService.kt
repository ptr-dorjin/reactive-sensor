package pd.sensor.reactive.server.service

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataRepository
import java.time.Instant

@Service
class SensorDataService(
    val sensorDataRepository: SensorDataRepository,
    meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(SensorDataService::class.java)
    private var inboundCounter = meterRegistry.counter("sensor.inbound.count")

    private val sender: MutableSharedFlow<SensorData> = MutableSharedFlow()

    fun stream(): Flow<SensorData> = sender

    fun latest(): Flow<SensorData> =
        sensorDataRepository.findLatest().mapToDomain()

    fun after(since: Instant): Flow<SensorData> =
        sensorDataRepository.findLatest(since).mapToDomain()

    suspend fun post(inboundFlow: Flow<SensorData>) =
        inboundFlow
            .onEach {
                inboundCounter.increment()
                log.debug("Received $it")
                sender.emit(it)
            }
            .map { it.toEntity() }
            .let { sensorDataRepository.saveAll(it) }
            .collect()
}