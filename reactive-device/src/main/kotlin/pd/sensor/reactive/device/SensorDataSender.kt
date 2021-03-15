package pd.sensor.reactive.device

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pd.sensor.domain.SensorData
import java.net.URI
import java.time.Instant
import javax.annotation.PostConstruct

@Component
class SensorDataSender(
    private val rsocketBuilder: RSocketRequester.Builder,
    private val serverProperties: ServerProperties
) {

    @PostConstruct
    fun send() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI(serverProperties.url))

            rSocketRequester.route("api.v1.sensors.stream")
                .dataWithType(flow {
                    emit(SensorData(31, "balcony", Instant.now()))
                })
                .retrieveFlow<Void>()
                .collect()
        }
    }
}