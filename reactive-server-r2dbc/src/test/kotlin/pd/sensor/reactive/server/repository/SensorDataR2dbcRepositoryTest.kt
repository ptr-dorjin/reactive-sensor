package pd.sensor.reactive.server.repository

import app.cash.turbine.test
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
import kotlin.time.ExperimentalTime


@ExperimentalTime
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataR2dbcTest
class SensorDataR2dbcRepositoryTest @Autowired constructor(
    private val sensorDataR2dbcRepository: SensorDataR2dbcRepository,
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
            sensorDataR2dbcRepository.deleteAll()
        }
    }

    @Test
    fun `When findLatest then return SensorData`() {
        runBlocking {
            val expected = sensorDataR2dbcRepository.save(
                SensorDataEntity(15.6, "room", now)
            )

            sensorDataR2dbcRepository.findLatest()
                .test {
                    assertThat(expectItem().testVersion())
                        .isEqualTo(expected.testVersion())

                    expectComplete()
                }
        }
    }

    @Test
    fun `When findLatest then return multiple SensorData sorted`() {
        runBlocking {
            sensorDataR2dbcRepository.saveAll(
                listOf(
                    SensorDataEntity(5.78, "garage", now.minusSeconds(1)),
                    SensorDataEntity(6.89, "garage", now)
                )
            ).toList()

            sensorDataR2dbcRepository.findLatest()
                .test {
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorDataEntity(5.78, "garage", now.minusSeconds(1).noMillis()))
                    assertThat(expectItem().testVersion())
                        .isEqualTo(SensorDataEntity(6.89, "garage", now.noMillis()))

                    expectComplete()
                }
        }
    }
}