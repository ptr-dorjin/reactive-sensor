package pd.sensor.reactive.server.repository

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import pd.sensor.domain.SensorData

@TestConfiguration
@Import(RedisConfig::class)
class RepositoryTestConfig {

    @Bean
    fun sensorDataRedisRepository(
        reactiveRedisTemplate: ReactiveRedisTemplate<String, SensorData>,
        streamReceiver: StreamReceiver<String, MapRecord<String, String, String>>
    ) = SensorDataRedisRepository(reactiveRedisTemplate, streamReceiver)
}