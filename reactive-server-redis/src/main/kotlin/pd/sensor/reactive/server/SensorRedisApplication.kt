package pd.sensor.reactive.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SensorRedisApplication

fun main(args: Array<String>) {
    runApplication<SensorRedisApplication>(*args)
}
