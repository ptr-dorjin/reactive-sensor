package pd.sensor.reactive.server.controller

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onStart
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.service.SensorDataService
import reactor.core.publisher.Hooks
import javax.annotation.PostConstruct

@Controller
@MessageMapping("api.v2.sensors")
class SensorDataController(val sensorDataService: SensorDataService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        Hooks.onErrorDropped {
            log.warn("Error dropped: ${it.message}")
        }
    }

    @FlowPreview
    @MessageMapping("stream")
    suspend fun write(@Payload inboundFlow: Flow<SensorData>) =
        sensorDataService.write(inboundFlow)

    @MessageMapping("stream")
    fun read(): Flow<SensorData> = sensorDataService
        .stream()
        .onStart {
            emitAll(sensorDataService.read())
        }
}