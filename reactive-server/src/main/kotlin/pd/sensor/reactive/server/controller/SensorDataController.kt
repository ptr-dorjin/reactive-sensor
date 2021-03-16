package pd.sensor.reactive.server.controller

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onStart
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.service.SensorDataService

@Controller
@MessageMapping("api.v1.sensors")
class SensorDataController(val sensorDataService: SensorDataService) {

    @MessageMapping("stream")
    suspend fun receive(@Payload inboundFlow: Flow<SensorData>) =
        sensorDataService.post(inboundFlow)

    @MessageMapping("stream")
    fun send(): Flow<SensorData> = sensorDataService
        .stream()
        .onStart {
            emitAll(sensorDataService.latest())
        }
}