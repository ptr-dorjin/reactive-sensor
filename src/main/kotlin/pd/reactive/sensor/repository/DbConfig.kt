package pd.reactive.sensor.repository

import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.h2.H2ConnectionOption

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
@EnableR2dbcRepositories
class DbConfig {

    @Bean
    fun sensorConnectionFactory(): ConnectionFactory = H2ConnectionFactory
        .inMemory(
            "sensors-db",
            "sa",
            "^5LHo7@!qGKg",
            mapOf(H2ConnectionOption.DB_CLOSE_DELAY to "-1")
        )

    @Bean
    fun initializer(sensorConnectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
        val initializer = ConnectionFactoryInitializer()
        initializer.setConnectionFactory(sensorConnectionFactory)
        val populator = ResourceDatabasePopulator(ClassPathResource("schema.sql"))
        initializer.setDatabasePopulator(populator)
        return initializer
    }
}