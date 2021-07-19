package pd.sensor.reactive.server.service

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataRedisRepository

@Service
class SensorDataService(
    val sensorDataRedisRepository: SensorDataRedisRepository,
    meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(SensorDataService::class.java)
    private var inboundCounter = meterRegistry.counter("sensor.inbound.count")

    private val sender: MutableSharedFlow<SensorData> = MutableSharedFlow()

    fun stream(): Flow<SensorData> = sender

    fun read(): Flow<SensorData> =
        sensorDataRedisRepository.readAll()

    @FlowPreview
    suspend fun write(inboundFlow: Flow<SensorData>) =
        inboundFlow
            .onEach {
                inboundCounter.increment()
                log.debug("Received $it")
                sender.emit(it)
            }
            .let { sensorDataRedisRepository.saveAll(it) }
            .collect()
}