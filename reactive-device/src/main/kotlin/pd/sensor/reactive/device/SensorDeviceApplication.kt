package pd.sensor.reactive.device

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import pd.sensor.reactive.device.prop.DeviceProperties
import pd.sensor.reactive.device.prop.ServerProperties

@SpringBootApplication
@EnableConfigurationProperties(
    ServerProperties::class,
    DeviceProperties::class
)
@EnableScheduling
class SensorDeviceApplication

fun main(args: Array<String>) {
    runApplication<SensorDeviceApplication>(*args)
}
