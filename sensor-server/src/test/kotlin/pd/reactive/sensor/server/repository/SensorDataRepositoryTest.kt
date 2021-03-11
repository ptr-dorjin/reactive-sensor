package pd.reactive.sensor.server.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.util.StreamUtils
import java.nio.charset.Charset
import java.time.Instant
import java.time.temporal.ChronoUnit


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DataR2dbcTest
class SensorDataRepositoryTest @Autowired constructor(
    private val sensorDataRepository: SensorDataRepository,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate
) {

    @BeforeAll
    fun setup() {
        val schema = StreamUtils.copyToString(
            ClassPathResource("schema.sql").inputStream,
            Charset.defaultCharset()
        )
        r2dbcEntityTemplate.databaseClient.sql(schema).fetch().rowsUpdated().block()
    }

    @Test
    fun `When findLatest then return SensorData`() {
        runBlocking {
            val expected = sensorDataRepository.save(
                SensorData(5, "room", Instant.now().truncatedTo(ChronoUnit.MILLIS))
            )

            sensorDataRepository.findLatest()
                .first()
                .apply {
                    assertThat(this).isEqualTo(expected)
                }
        }
    }
}