package pd.sensor.reactive.server.repository

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.data.redis.stream.StreamReceiver.StreamReceiverOptions
import pd.sensor.domain.SensorData

@Configuration
class RedisConfig {

    /**
     * For writing to Redis Stream
     */
    @Bean
    fun reactiveRedisTemplate(
        factory: ReactiveRedisConnectionFactory,
        serializationContext: RedisSerializationContext<String, SensorData>
    ): ReactiveRedisTemplate<String, SensorData> = ReactiveRedisTemplate(
        factory,
        serializationContext
    )

    /**
     * For reading from Redis Stream
     */
    @Bean
    fun streamReceiver(
        factory: ReactiveRedisConnectionFactory,
        serializationContext: RedisSerializationContext<String, SensorData>
    ): StreamReceiver<String, MapRecord<String, String, String>> {
        return StreamReceiver.create(
            factory,
            StreamReceiverOptions.builder()
                .serializer(serializationContext)
                .build()
        )
    }

    @Bean
    fun serializationContext(): RedisSerializationContext<String, SensorData> =
        RedisSerializationContext.newSerializationContext<String, SensorData>(
            RedisSerializer.string()
        ).build()
}