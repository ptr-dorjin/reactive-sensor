package pd.sensor.reactive.device

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(ServerProperties::class)
@EnableScheduling
class SensorDeviceApplication

fun main(args: Array<String>) {
    runApplication<SensorDeviceApplication>(*args)
}
