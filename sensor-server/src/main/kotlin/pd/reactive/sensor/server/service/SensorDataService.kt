package pd.reactive.sensor.server.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.springframework.stereotype.Service
import pd.reactive.sensor.server.repository.SensorData
import pd.reactive.sensor.server.repository.SensorDataRepository
import java.time.Instant

@Service
class SensorDataService(val sensorDataRepository: SensorDataRepository) {

    private val sender: MutableSharedFlow<SensorData> = MutableSharedFlow()

    fun stream(): Flow<SensorData> = sender

    fun latest(): Flow<SensorData> =
        sensorDataRepository.findLatest()

    fun after(since: Instant): Flow<SensorData> =
        sensorDataRepository.findLatest(since)

    suspend fun post(sensorData: Flow<SensorData>) =
        sensorData
            .onEach(sender::emit)
            .let { sensorDataRepository.saveAll(it) }
            .collect()
}