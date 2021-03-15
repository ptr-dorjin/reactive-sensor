package pd.sensor.reactive.device

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ServerProperties::class)
class SensorDeviceApplication

fun main(args: Array<String>) {
    runApplication<SensorDeviceApplication>(*args)
}
