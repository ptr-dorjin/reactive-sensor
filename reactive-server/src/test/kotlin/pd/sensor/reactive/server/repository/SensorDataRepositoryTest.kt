package pd.sensor.reactive.server.repository

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.util.StreamUtils
import pd.sensor.reactive.server.noMillis
import pd.sensor.reactive.server.testVersion
import java.nio.charset.Charset
import java.time.Instant


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataR2dbcTest
class SensorDataRepositoryTest @Autowired constructor(
    private val sensorDataRepository: SensorDataRepository,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) {
    private val now: Instant = Instant.now()

    @BeforeAll
    fun setup() {
        val schema = StreamUtils.copyToString(
            ClassPathResource("sql/schema.sql").inputStream,
            Charset.defaultCharset()
        )
        r2dbcEntityTemplate.databaseClient.sql(schema).fetch().rowsUpdated().block()
    }

    @AfterEach
    fun tearDown() {
        runBlocking {
            sensorDataRepository.deleteAll()
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When findLatest then return SensorData`() {
        runBlocking {
            val expected = sensorDataRepository.save(
                SensorDataEntity(15, "room", now)
            )

            sensorDataRepository.findLatest()
                .test {
                    assertThat(expectItem().testVersion())
                        .isEqualTo(expected.testVersion())

                    expectComplete()
                }
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `When findLatest then return multiple SensorData sorted`() {
        runBlocking {
            sensorDataRepository.saveAll(
                listOf(
                    SensorDataEntity(5, "garage", now.minusSeconds(1)),
                    SensorDataEntity(6, "garage", now)
                )
            ).toList()

            sensorDataRepository.findLatest()
                .test {
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorDataEntity(5, "garage", now.minusSeconds(1).noMillis()))
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorDataEntity(6, "garage", now.noMillis()))

                    expectComplete()
                }
        }
    }
}