package pd.sensor.reactive.server.repository

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.addAndAwait
import org.springframework.data.redis.core.trimAndAwait
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Repository
import pd.sensor.domain.SensorData

const val STREAM_KEY = "sensor-data"

@Repository
class SensorDataRedisRepository(
    val reactiveRedisTemplate: ReactiveRedisTemplate<String, SensorData>,
    val streamReceiver: StreamReceiver<String, MapRecord<String, String, String>>
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @FlowPreview
    fun saveAll(entityStream: Flow<SensorData>): Flow<RecordId> =
        entityStream
            .map { toMapRecord(it) }
            .flatMapConcat {
                log.debug("Saving record: $it")
                reactiveRedisTemplate
                    .opsForStream<String, SensorData>()
                    .add(it)
                    .asFlow()
            }

    private fun toMapRecord(sensorData: SensorData): MapRecord<String, String, String> =
        StreamRecords.newRecord()
            .`in`(STREAM_KEY)
            .ofMap(sensorData.toMap())

    fun readAll(): Flow<SensorData> =
        streamReceiver
            .receive(StreamOffset.fromStart(STREAM_KEY))
            .asFlow()
            .onEach { log.debug("Received stream record: $it") }
            .map { it.value.fromMap() }

    /**
     * For tests only. Saves a single item. Main flow works with coroutines Flow
     */
    suspend fun save(sensorData: SensorData): RecordId {
        val record = toMapRecord(sensorData)
        log.debug("Saving record: $record")
        return reactiveRedisTemplate
            .opsForStream<String, SensorData>()
            .addAndAwait(record)
    }

    /**
     * For tests only
     */
    suspend fun deleteAll() {
        reactiveRedisTemplate
            .opsForStream<String, SensorData>()
            .trimAndAwait(STREAM_KEY, 0)
    }
}