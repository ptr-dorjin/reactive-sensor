package pd.reactive.sensor.server.controller

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onStart
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Controller
import pd.reactive.sensor.server.repository.SensorData
import pd.reactive.sensor.server.service.SensorDataService

@Controller
@MessageMapping("api.v1.sensorData")
class SensorDataResource(val sensorDataService: SensorDataService) {

    @MessageMapping("stream")
    suspend fun receive(@Payload inboundData: Flow<SensorData>) =
        sensorDataService.save(inboundData)

    @MessageMapping("stream")
    suspend fun send(): Flow<SensorData> = sensorDataService
        .stream()
        .onStart {
            emitAll(sensorDataService.latest())
        }
}