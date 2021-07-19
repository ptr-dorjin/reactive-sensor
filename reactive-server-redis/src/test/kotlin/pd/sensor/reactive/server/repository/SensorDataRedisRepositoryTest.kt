package pd.sensor.reactive.server.repository

import app.cash.turbine.test
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pd.sensor.domain.SensorData
import java.time.Instant
import kotlin.time.ExperimentalTime


@FlowPreview
@ExperimentalTime
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class SensorDataRedisRepositoryTest @Autowired constructor(
    private val sensorDataRedisRepository: SensorDataRedisRepository
) {
    private val now: Instant = Instant.now()

    @BeforeAll
    fun beforeAll() {
        runBlocking { sensorDataRedisRepository.deleteAll() }
    }

    @AfterEach
    fun afterEach() {
        runBlocking { sensorDataRedisRepository.deleteAll() }
    }

    @Test
    fun `When saving one item should read one SensorData`() {
        runBlocking {
            // when
            val sensorData = SensorData(15.6, "room", now)
            sensorDataRedisRepository.save(sensorData)

            // then
            sensorDataRedisRepository.readAll().take(1)
                .test {
                    assertThat(expectItem())
                        .isEqualTo(sensorData)

                    expectComplete()
                }
        }
    }

    @Test
    fun `When saving Flow should read Flow of SensorData`() {
        runBlocking {
            // when
            sensorDataRedisRepository.saveAll(
                flowOf(
                    SensorData(5.78, "garage", now.minusSeconds(1)),
                    SensorData(6.89, "garage", now)
                )
            ).toList()

            // then
            sensorDataRedisRepository.readAll().take(2)
                .test {
                    assertThat(expectItem())
                        .isEqualTo(SensorData(5.78, "garage", now.minusSeconds(1)))
                    assertThat(expectItem())
                        .isEqualTo(SensorData(6.89, "garage", now))

                    expectComplete()
                }
        }
    }
}