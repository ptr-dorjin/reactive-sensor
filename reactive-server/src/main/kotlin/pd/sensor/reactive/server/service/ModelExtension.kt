package pd.sensor.reactive.server.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pd.sensor.domain.SensorData
import pd.sensor.reactive.server.repository.SensorDataEntity

fun SensorDataEntity.toDomain() = SensorData(temperature, location, instant, id)
fun SensorData.toEntity() = SensorDataEntity(temperature, location, instant, id)

fun Flow<SensorDataEntity>.mapToDomain(): Flow<SensorData> = map { it.toDomain() }
fun Flow<SensorData>.mapToEntity(): Flow<SensorDataEntity> = map { it.toEntity() }