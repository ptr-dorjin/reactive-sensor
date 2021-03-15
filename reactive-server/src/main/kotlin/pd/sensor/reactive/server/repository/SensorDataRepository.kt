package pd.sensor.reactive.server.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.temporal.ChronoUnit

interface SensorDataRepository : CoroutineCrudRepository<SensorDataEntity, String> {

    @Query(
        """
        select *
        from sensor_data
        where instant > :since
        order by instant
    """
    )
    fun findLatest(@Param("since") since: Instant = Instant.now().truncatedTo(ChronoUnit.DAYS)): Flow<SensorDataEntity>
}