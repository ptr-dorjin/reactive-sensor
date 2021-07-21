package pd.sensor.reactive.server.repository

import app.cash.turbine.test
import com.palantir.docker.compose.DockerComposeExtension
import com.palantir.docker.compose.connection.waiting.HealthChecks.toHaveAllPortsOpen
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
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.test.context.ContextConfiguration
import pd.sensor.domain.SensorData
import java.time.Instant
import kotlin.time.ExperimentalTime

private val now: Instant = Instant.now()

@FlowPreview
@ExperimentalTime
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataRedisTest
@ContextConfiguration(classes = [RepositoryTestConfig::class])
class SensorDataRedisRepositoryTest @Autowired constructor(
    private val sensorDataRedisRepository: SensorDataRedisRepository
) {
    companion object {
        @JvmField
        @RegisterExtension
        val docker: DockerComposeExtension = DockerComposeExtension.builder()
            .file("src/test/resources/docker-compose.yml")
            .waitingForService("docker-redis-for-tests", toHaveAllPortsOpen())
            .build()
    }

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