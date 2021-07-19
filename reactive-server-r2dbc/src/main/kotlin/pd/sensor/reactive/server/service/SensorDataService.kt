package pd.sensor.reactive.server.service

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataR2dbcRepository
import java.time.Instant

@Service
class SensorDataService(
    val sensorDataR2dbcRepository: SensorDataR2dbcRepository,
    meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(SensorDataService::class.java)
    private var inboundCounter = meterRegistry.counter("sensor.inbound.count")

    private val sender: MutableSharedFlow<SensorData> = MutableSharedFlow()

    fun stream(): Flow<SensorData> = sender

    fun latest(): Flow<SensorData> =
        sensorDataR2dbcRepository.findLatest().mapToDomain()

    fun after(since: Instant): Flow<SensorData> =
        sensorDataR2dbcRepository.findLatest(since).mapToDomain()

    suspend fun post(inboundFlow: Flow<SensorData>) =
        inboundFlow
            .onEach {
                inboundCounter.increment()
                log.debug("Received $it")
                sender.emit(it)
            }
            .map { it.toEntity() }
            .let { sensorDataR2dbcRepository.saveAll(it) }
            .collect()
}