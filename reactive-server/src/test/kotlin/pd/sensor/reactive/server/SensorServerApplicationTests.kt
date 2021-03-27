package pd.sensor.reactive.server

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataEntity
import pd.sensor.reactive.server.repository.SensorDataRepository
import java.net.URI
import java.time.Instant
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
class SensorServerApplicationTests(
    @Autowired val rsocketBuilder: RSocketRequester.Builder,
    @Autowired val sensorDataRepository: SensorDataRepository,
    @LocalServerPort val serverPort: Int
) {

    val now: Instant = Instant.now()

    @BeforeEach
    fun setUp() {
        runBlocking {
            val secondBeforeNow = now.minusSeconds(1)
            val twoSecondBeforeNow = now.minusSeconds(2)
            sensorDataRepository.saveAll(
                listOf(
                    SensorDataEntity(21.2, "room", twoSecondBeforeNow),
                    SensorDataEntity(20.6, "room", secondBeforeNow),
                    SensorDataEntity(19.3, "room", now)
                )
            ).toList()
        }
    }


    @AfterEach
    fun tearDown() {
        runBlocking {
            sensorDataRepository.deleteAll()
        }
    }

    @ExperimentalTime
    @Test
    fun `test inbound data stream`() {
        runBlocking {
            launch {
                val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

                rSocketRequester.route("api.v1.sensors.stream")
                    .dataWithType(flow {
                        emit(SensorData(18.1, "garage", now.plusSeconds(1)))
                    })
                    .retrieveFlow<Void>()
                    .collect()
            }

            delay(1.seconds)

            sensorDataRepository.findAll()
                .first { it.location == "garage" }
                .apply {
                    assertThat(this.testVersion())
                        .isEqualTo(SensorDataEntity(18.1, "garage", now.plusSeconds(1).noMillis()))
                }
        }
    }

    @ExperimentalTime
    @ExperimentalCoroutinesApi
    @Test
    fun `test outbound data stream`() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

            rSocketRequester
                .route("api.v1.sensors.stream")
                .retrieveFlow<SensorData>()
                .test {
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorData(21.2, "room", now.minusSeconds(2).noMillis()))
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorData(20.6, "room", now.minusSeconds(1).noMillis()))
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorData(19.3, "room", now.noMillis()))

                    expectNoEvents()

                    launch {
                        rSocketRequester.route("api.v1.sensors.stream")
                            .dataWithType(flow {
                                emit(SensorData(11.22, "backyard", now.plusSeconds(1)))
                            })
                            .retrieveFlow<Void>()
                            .collect()
                    }

                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorData(11.22, "backyard", now.plusSeconds(1).noMillis()))

                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

}
