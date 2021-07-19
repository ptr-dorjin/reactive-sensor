package pd.sensor.reactive.server

import app.cash.turbine.test
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.dataWithType
import org.springframework.messaging.rsocket.retrieveFlow
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataRedisRepository
import java.net.URI
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@FlowPreview
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SensorRedisApplicationTests(
    @Autowired val rsocketBuilder: RSocketRequester.Builder,
    @Autowired val sensorDataRedisRepository: SensorDataRedisRepository,
    @LocalServerPort val serverPort: Int
) {

    val now: Instant = Instant.now()

    @BeforeAll
    fun beforeAll() {
        runBlocking { sensorDataRedisRepository.deleteAll() }
    }

    @BeforeEach
    fun setUp() {
        runBlocking {
            val secondBeforeNow = now.minusSeconds(1)
            val twoSecondBeforeNow = now.minusSeconds(2)
            val bodyFlow = flow {
                emit(SensorData(21.2, "room", twoSecondBeforeNow))
                emit(SensorData(20.6, "room", secondBeforeNow))
                emit(SensorData(19.3, "room", now))
            }
            sensorDataRedisRepository.saveAll(bodyFlow).toList()
        }
    }

    @AfterEach
    fun afterEach() {
        runBlocking { sensorDataRedisRepository.deleteAll() }
    }


    @FlowPreview
    @ExperimentalTime
    @Test
    fun `test inbound data stream`() {
        runBlocking {
            val sensorData = SensorData(18.1, "garage", now.plusSeconds(1))
            launch {
                val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

                rSocketRequester.route("api.v2.sensors.stream")
                    .dataWithType(flow {
                        emit(sensorData)
                    })
                    .retrieveFlow<Void>()
                    .collect()
            }

            delay(Duration.seconds(1))

            sensorDataRedisRepository.readAll()
                .first { it.location == "garage" }
                .apply {
                    assertThat(this)
                        .isEqualTo(sensorData)
                }
        }
    }

    @ExperimentalTime
    @Test
    fun `test outbound data stream`() {
        runBlocking {
            val rSocketRequester = rsocketBuilder.websocket(URI("ws://localhost:${serverPort}/rsocket"))

            rSocketRequester
                .route("api.v2.sensors.stream")
                .retrieveFlow<SensorData>()
                .test {
                    assertThat(expectItem())
                        .isEqualTo(SensorData(21.2, "room", now.minusSeconds(2)))
                    assertThat(expectItem())
                        .isEqualTo(SensorData(20.6, "room", now.minusSeconds(1)))
                    assertThat(expectItem())
                        .isEqualTo(SensorData(19.3, "room", now))

                    expectNoEvents()

                    launch {
                        rSocketRequester
                            .route("api.v2.sensors.stream")
                            .dataWithType(flow {
                                emit(SensorData(11.22, "backyard", now.plusSeconds(1)))
                            })
                            .retrieveFlow<Void>()
                            .collect()
                    }

                    assertThat(expectItem())
                        .isEqualTo(SensorData(11.22, "backyard", now.plusSeconds(1)))

                    cancelAndIgnoreRemainingEvents()
                }
        }
    }

}
