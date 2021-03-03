package pd.reactive.sensor.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.time.temporal.ChronoUnit

interface SensorDataRepository : CoroutineSortingRepository<SensorData, String> {

    @Query(
        """
        select *
        from sensor_data
        where instant > :since
        order by instant desc         
    """
    )
    fun findLatest(@Param("since") since: Instant = Instant.now().truncatedTo(ChronoUnit.DAYS)): Flow<SensorData>
}